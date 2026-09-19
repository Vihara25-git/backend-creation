import apiClient from "../../lib/api";

export interface ActiveRelease {
  id: string;
  releaseId: string;
  name: string;
  description: string;
  status: string;
  releaseDate: string;
  releaseType_id: string;
  project_id: number;
}

export interface ActiveReleasesResponse {
  message: string;
  data: ActiveRelease[];
  status: string;
  statusCode: string;
}

export const getActiveReleases = async (projectId: string | number): Promise<ActiveReleasesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/ReleaseView/project/${projectId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      message: 'Success',
      status: 'success',
      statusCode: '200',
      data: items.map((r: any) => ({
        id: String(r.releaseId || r.id),
        releaseId: String(r.releaseId || r.id),
        name: r.releaseName || r.name || 'Release',
        description: r.description || '',
        status: r.status || 'In Progress',
        releaseDate: r.releaseDate || '',
        releaseType_id: String(r.releaseTypeId || 1),
        project_id: Number(projectId),
      })),
    };
  } catch {
    return {
      message: 'Success',
      status: 'success',
      statusCode: '200',
      data: [],
    };
  }
};