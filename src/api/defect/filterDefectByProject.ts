import apiClient from "../../lib/api";

function mapBackendDefect(d: any) {
  const defectNum = d.projectDefectNumber || d.defectId;
  return {
    id: d.defectId,
    defectId: defectNum ? `DEF-${String(defectNum).padStart(4, '0')}` : `DEF-${d.defectId}`,
    projectDefectId: d.projectDefectNumber ? `DEF-${d.projectDefectNumber}` : `DEF-${d.defectId}`,
    title: d.briefDescription || '',
    description: d.briefDescription || '',
    steps: d.steps || '',
    stepsToReproduce: d.steps ? [d.steps] : [],
    stepsToRecreation: d.steps || '',
    projectId: d.projectId,
    projectName: d.projectName || '',
    moduleId: d.moduleId,
    moduleName: d.moduleName || '',
    subModuleId: d.subModuleId,
    subModuleName: d.subModuleName || '',
    severityId: d.severityId,
    severityName: d.severityName || '',
    priorityId: d.priorityId,
    priorityName: d.priorityName || '',
    defectStatusId: d.statusTypeId,
    statusId: d.statusTypeId,
    status: d.statusName || 'New',
    statusName: d.statusName || 'New',
    defectTypeId: d.defectTypeId,
    defectTypeName: d.defectTypeName || '',
    releaseId: d.releaseId,
    releaseName: d.releaseName || '',
    assignedToId: d.assignToId,
    assignedTo: d.assignToName || 'Unassigned',
    assignedToName: d.assignToName || 'Unassigned',
    assignedByName: d.enterBy || 'Admin',
    reportedBy: d.enterBy || 'Admin',
    attachmentImage: d.attachmentImage,
    attachment: d.attachmentImage || null,
    image: d.attachmentImage || null,
    testCaseRequired: d.testCaseRequired,
    testCaseId: d.testCaseId,
  };
}

export async function getDefectsByProjectId(
  projectId: number,
  page: number = 0,
  size: number = 10,
  search?: string
): Promise<any> {
  if (!search) {
    try {
      const response = await apiClient.get(`/api/v1/defect/project/${projectId}?page=${page}&size=${size}`);
      const resData = response.data?.data || response.data;
      const items = Array.isArray(resData) ? resData : (resData?.content || []);

      return {
        status: 'success',
        statusCode: 200,
        data: {
          content: items.map(mapBackendDefect),
          totalElements: resData?.totalElements ?? items.length,
          totalPages: resData?.totalPages ?? 1,
          pageNumber: resData?.number ?? page,
          pageSize: resData?.size ?? size,
        },
      };
    } catch {
      // Fallback to filterDefects if project endpoint fails
    }
  }
  return filterDefects({ projectId, search }, page, size);
}

export async function filterDefects(
  filters: any = {},
  page: number = 0,
  size: number = 10
): Promise<any> {
  try {
    const body: any = {};
    if (filters.projectId) body.projectId = Number(filters.projectId);
    if (filters.moduleId) body.moduleId = Number(filters.moduleId);
    if (filters.subModuleId) body.subModuleId = Number(filters.subModuleId);
    if (filters.severityId) body.severityId = Number(filters.severityId);
    if (filters.priorityId) body.priorityId = Number(filters.priorityId);
    if (filters.statusId || filters.statusTypeId) body.statusTypeId = Number(filters.statusId || filters.statusTypeId);
    if (filters.defectTypeId) body.defectTypeId = Number(filters.defectTypeId);
    if (filters.search) body.search = filters.search;

    let response: any;
    try {
      response = await apiClient.post(`/api/v1/defect/filter?page=${page}&size=${size}`, body);
    } catch (postErr: any) {
      if (postErr.response?.status === 405) {
        response = await apiClient.get(`/api/v1/defect/filter`, {
          params: { ...body, page, size },
        });
      } else {
        throw postErr;
      }
    }

    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusCode: 200,
      data: {
        content: items.map(mapBackendDefect),
        totalElements: resData?.totalElements ?? items.length,
        totalPages: resData?.totalPages ?? 1,
        pageNumber: resData?.number ?? page,
        pageSize: resData?.size ?? size,
      },
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        statusCode: 200,
        data: {
          content: [],
          totalElements: 0,
          totalPages: 1,
          pageNumber: page,
          pageSize: size,
        },
      };
    }
    throw err;
  }
}

export async function filterDefectsForTest(filters: any = {}): Promise<any[]> {
  try {
    const result = await filterDefects(filters, 0, 1000);
    return result.data?.content || [];
  } catch (err) {
    console.error('Failed to filter defects for test:', err);
    return [];
  }
}

export default filterDefects;