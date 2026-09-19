import apiClient from "../../lib/api";

export interface Modules {
  id: number;
  name: string;
  projectId: number;
  assignedDev: {
    userId: number;
    userName: string;
  } | null;
  submodules?: any[];
}

export interface CreateReleaseResponse {
  status: string;
  message: string;
  data: Modules[];
  statusCode: number;
}

export const getModulesByProjectId = async (projectId: number): Promise<CreateReleaseResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/module`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      message: 'Modules fetched successfully',
      statusCode: 200,
      data: items.map((m: any) => ({
        id: m.moduleId || m.id,
        name: m.moduleName || m.name || 'Module',
        projectId: m.projectId || projectId,
        assignedDev: null,
        submodules: (m.subModules || []).map((s: any) => ({
          id: s.subModuleId || s.id,
          subModuleId: s.subModuleId || s.id,
          name: s.subModuleName || s.name || '',
          subModuleName: s.subModuleName || s.name || '',
          moduleId: s.moduleId || m.moduleId,
        })),
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        message: 'Modules fetched successfully',
        statusCode: 200,
        data: [],
      };
    }
    throw err;
  }
};

export async function getAllocatedUsersByModuleId(moduleId: string | number) {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/allocated-leader`);
    const resData = response.data?.data || response.data;
    return Array.isArray(resData) ? resData : (resData ? [resData] : []);
  } catch (err: any) {
    return [];
  }
}

export async function getUsersByAllocation(projectId: string | number, _moduleId: string | number, _subModuleId?: string | number) {
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    return Array.isArray(resData) ? resData : [];
  } catch (err: any) {
    return [];
  }
}
