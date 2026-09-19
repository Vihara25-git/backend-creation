import apiClient from "../../lib/api";

export interface ProjectRelease {
  id: string;
  releaseId: string;
  releaseName: string;
  name: string;
  version?: string;
  releaseVersion?: string;
  description: string;
  status: string;
  releaseDate: string;
  releaseType_id?: string | number;
  releaseTypeId?: number;
  releaseType_name: string;
  releaseTypeName?: string;
  project_id: number;
}

export const projectReleaseCardView = async (projectId: string | number) => {
  try {
    const response = await apiClient.get(`/api/v1/ReleaseView/project/${projectId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'Success',
      statusCode: '200',
      message: 'Success',
      data: items.map((r: any) => ({
        ...r,
        id: String(r.releaseId || r.id),
        releaseId: String(r.releaseId || r.id),
        releaseName: r.releaseName || r.name || '',
        name: r.releaseName || r.name || '',
        version: r.releaseVersion || r.version || '',
        releaseVersion: r.releaseVersion || r.version || '',
        description: r.description || '',
        status: r.status || 'In Progress',
        releaseDate: r.releaseDate || '',
        releaseType_id: r.releaseTypeId || 1,
        releaseTypeId: r.releaseTypeId || 1,
        releaseType_name: r.releaseTypeName || 'Release',
        releaseTypeName: r.releaseTypeName || 'Release',
        project_id: Number(projectId),
        testCaseCount: r.testCaseCount || 0,
        totalTestCases: r.testCaseCount || 0,
        testCases: [],
      })),
    };
  } catch (err: any) {
    return {
      status: 'Success',
      statusCode: '200',
      message: 'Success',
      data: [],
    };
  }
};

export const getReleaseTestCaseCountsLoad = async (releaseIds: number[]) => {
  const list = await Promise.all(
    releaseIds.map(async (id) => {
      let count = 0;
      try {
        const res = await apiClient.get(`/api/v1/release-test-cases/count?releaseId=${id}`);
        count = Number(res.data?.data ?? res.data ?? 0);
      } catch {
        count = 0;
      }
      return {
        releaseId: Number(id),
        testCaseCount: count,
        total: count,
        passed: 0,
        failed: 0,
        blocked: 0,
        unexecuted: count,
      };
    })
  );

  const data: any = [...list];
  list.forEach(item => {
    data[item.releaseId] = item;
  });

  return {
    status: 'Success',
    statusCode: 200,
    data,
  };
};