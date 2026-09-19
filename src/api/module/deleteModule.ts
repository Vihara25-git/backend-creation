import apiClient from "../../lib/api";

export const deleteModule = async (projectId: number, id: number): Promise<{ status: string; statusCode?: string; success?: boolean; data?: any[]; message?: string }> => {
  const response = await apiClient.delete(`/api/v1/project/${projectId}/module/${id}`);
  return {
    status: 'success',
    statusCode: String(response.status || 200),
    success: true,
    message: response.data?.statusMessage || 'Module deleted successfully',
  };
};
