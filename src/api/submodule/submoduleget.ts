import apiClient from "../../lib/api";

export interface Submodule {
  id: number;
  name: string;
  submoduleName?: string;
  subModuleName?: string;
  getSubModuleName?: string;
}

export interface GetSubmodulesResponse {
  status: string;
  message: string;
  data: Submodule[];
  statusCode: number;
}

export const getSubmodulesByModule = async (moduleId: number): Promise<GetSubmodulesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/module/module/${moduleId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      message: 'Submodules fetched successfully',
      statusCode: 200,
      data: items.map((s: any) => ({
        id: s.subModuleId || s.id,
        name: s.subModuleName || s.name || 'Submodule',
        submoduleName: s.subModuleName || s.name || 'Submodule',
        subModuleName: s.subModuleName || s.name || 'Submodule',
        getSubModuleName: s.subModuleName || s.name || 'Submodule',
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        message: 'Submodules fetched successfully',
        statusCode: 200,
        data: [],
      };
    }
    throw err;
  }
};

export const getSubmodulesByModuleId = async (moduleId: number): Promise<GetSubmodulesResponse> => {
  return getSubmodulesByModule(moduleId);
};
