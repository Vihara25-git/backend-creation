import apiClient from "../../lib/api";

export interface ActiveRelease {
  id: string;
  name: string;
  status: string;
}

export const getActiveReleasesByProject = async (
  projectId: string | number
): Promise<ActiveRelease[]> => {
  try {
    const response = await apiClient.get(`/api/v1/ReleaseView/project/${projectId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((r: any) => ({
      id: String(r.releaseId || r.id),
      name: r.releaseName || r.name || 'Release',
      status: r.status || 'In Progress',
    }));
  } catch {
    return [];
  }
};