import { Module } from "../../types/index";
import apiClient from "../../lib/api";

export const updateModule = async (
  projectId: number,
  id: number,
  data: Partial<Module>
): Promise<{ success: boolean; status?: string; module?: Module; message?: string }> => {
  const response = await apiClient.put(`/api/v1/project/${projectId}/module/${id}`, {
    moduleName: data.name,
  });

  return {
    success: true,
    status: 'success',
    module: {
      id,
      name: data.name || '',
      projectId,
    } as any,
    message: response.data?.statusMessage || 'Module updated successfully',
  };
};
