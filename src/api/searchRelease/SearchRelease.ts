import apiClient from "../../lib/api";

export const getReleasesByProjectId = async (projectId: string | number) => {
  try {
    const response = await apiClient.get(`/api/v1/ReleaseView/project/${projectId}`);
    const resData = response.data?.data || response.data;
    return Array.isArray(resData) ? resData : [];
  } catch {
    return [];
  }
};

export async function searchReleases(params: any) {
  try {
    if (typeof params === 'number' || typeof params === 'string') {
      const response = await apiClient.get(`/api/v1/ReleaseView/${params}`);
      return response.data?.data || response.data;
    }
    const response = await apiClient.get('/api/v1/ReleaseView');
    const resData = response.data?.data || response.data;
    return Array.isArray(resData) ? resData : [];
  } catch {
    return typeof params === 'number' || typeof params === 'string' ? null : [];
  }
}
