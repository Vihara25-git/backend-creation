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
