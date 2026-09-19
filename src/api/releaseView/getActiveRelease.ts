import apiClient from "../../lib/api";

export const getActiveRelease = async (projectId: string | number) => {
  try {
    const response = await apiClient.get(`/api/v1/ReleaseView/project/${projectId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      data: items.map((r: any) => ({
        ...r,
        releaseName: r.releaseName || r.name,
      })),
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: [],
    };
  }
};
