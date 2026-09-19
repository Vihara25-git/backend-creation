import apiClient from "../../lib/api";

export interface AllocateModuleLeaderRequest {
  projectId?: number;
  moduleId: number;
  userId: number;
}

export interface AllocatedLeaderResponse {
  allocateModuleId: number;
  moduleId: number;
  userId: number;
  userName?: string;
}

export const allocateModuleLeader = async (data: AllocateModuleLeaderRequest) => {
  const response = await apiClient.post(`/api/v1/module/${data.moduleId}/employee/${data.userId}`);
  return {
    status: 'success',
    statusCode: 200,
    message: response.data?.message || 'Leader allocated successfully',
    data: response.data,
  };
};

export const getAllocatedLeader = async (moduleId: number): Promise<AllocatedLeaderResponse | null> => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];
    if (items.length > 0) {
      const item = items[0];
      const fullName = `${item.firstName || ''} ${item.lastName || ''}`.trim() || item.email || `Employee ${item.employeeId}`;
      return {
        allocateModuleId: item.id || item.modQaId,
        moduleId: item.moduleId || moduleId,
        userId: item.employeeId,
        userName: fullName,
      };
    }
    return null;
  } catch {
    return null;
  }
};

export const deallocateModuleLeader = async (moduleId: number, userId: number) => {
  const response = await apiClient.delete(`/api/v1/module/${moduleId}/employee/${userId}`);
  return {
    status: 'success',
    statusCode: 200,
    message: 'Leader deallocated successfully',
    data: response.data,
  };
};