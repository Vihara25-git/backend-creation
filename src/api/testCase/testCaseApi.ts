import apiClient from "../../lib/api";

export const getTestCasesByProjectAndSubmodule = async (
  projectId: string,
  subModuleId: string,
  description?: string,
  defectTypeId?: number,
  severityId?: number,
  _page?: number,
  _size: number = 1000000
): Promise<any[]> => {
  try {
    const q = new URLSearchParams();
    if (subModuleId) q.append('subModuleId', String(subModuleId));
    if (projectId) q.append('projectId', String(projectId));
    if (description) q.append('description', description);
    if (defectTypeId) q.append('defectTypeId', String(defectTypeId));
    if (severityId) q.append('severityId', String(severityId));

    const pId = projectId || '1';
    const response = await apiClient.get(`/api/v1/project/${pId}/filter?${q.toString()}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    const list = items.map((t: any) => ({
      id: t.testCaseId || t.id,
      no: `TC-${t.testCaseNumber || t.testCaseId || t.id}`,
      testcaseNo: `TC-${t.testCaseNumber || t.testCaseId || t.id}`,
      description: t.description,
      detailsSteps: t.testSteps || t.steps,
      expectedResult: '',
      moduleId: t.moduleId,
      moduleName: t.moduleName,
      subModuleId: t.subModuleId,
      subModuleName: t.subModuleName,
      severityId: t.severityId,
      severityName: t.severityName,
      defectTypeId: t.defectTypeId,
      defectTypeName: t.defectTypeName,
      type: t.defectTypeName || t.type || '',
      defectType: t.defectTypeName || t.type || '',
    }));

    (list as any).totalPages = 1;
    (list as any).totalElements = list.length;
    (list as any).isServerPaginated = false;
    return list;
  } catch {
    const list: any[] = [];
    (list as any).totalPages = 1;
    (list as any).totalElements = 0;
    (list as any).isServerPaginated = false;
    return list;
  }
};

export async function deleteTestCase(
  subModuleId: number,
  testCaseId: string | number,
  projectId: number = 1,
  moduleId: number = 1
) {
  const url = (projectId && testCaseId)
    ? `/api/v1/project/${projectId}/testcase/${testCaseId}`
    : `/api/v1/project/${projectId || 1}/module/${moduleId || 1}/submodule/${subModuleId}/testcase/${testCaseId}`;
  const response = await apiClient.delete(url);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Test case deleted successfully',
  };
}

export async function getTestCasesByProjectAndModule(
  projectId: string | number,
  moduleId: string | number,
  _page: number = 0,
  _size: number = 1000000
) {
  try {
    const q = new URLSearchParams();
    if (moduleId) q.append('moduleId', String(moduleId));
    if (projectId) q.append('projectId', String(projectId));

    const pId = projectId || '1';
    const response = await apiClient.get(`/api/v1/project/${pId}/filter?${q.toString()}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    const list = items.map((t: any) => ({
      id: t.testCaseId || t.id,
      no: `TC-${t.testCaseNumber || t.testCaseId || t.id}`,
      testcaseNo: `TC-${t.testCaseNumber || t.testCaseId || t.id}`,
      description: t.description,
      detailsSteps: t.testSteps || t.steps,
      expectedResult: '',
      moduleId: t.moduleId,
      moduleName: t.moduleName,
      subModuleId: t.subModuleId,
      subModuleName: t.subModuleName,
      severityId: t.severityId,
      severityName: t.severityName,
      defectTypeId: t.defectTypeId,
      defectTypeName: t.defectTypeName,
      type: t.defectTypeName || t.type || '',
      defectType: t.defectTypeName || t.type || '',
    }));

    (list as any).totalPages = 1;
    (list as any).totalElements = list.length;
    (list as any).isServerPaginated = false;
    return list;
  } catch {
    const list: any[] = [];
    (list as any).totalPages = 1;
    (list as any).totalElements = 0;
    (list as any).isServerPaginated = false;
    return list;
  }
}

export async function getTestCasesByBulkModules(
  projectId: string | number,
  moduleIds: number[]
) {
  const all: any[] = [];
  for (const modId of moduleIds) {
    const list = await getTestCasesByProjectAndModule(projectId, modId);
    all.push(...list);
  }
  return all;
}

export async function getTestCasesByBulkSubmodules(
  projectId: string | number,
  submoduleIds: number[]
) {
  const all: any[] = [];
  for (const subId of submoduleIds) {
    const list = await getTestCasesByProjectAndSubmodule(String(projectId), String(subId));
    all.push(...list);
  }
  return all;
}
