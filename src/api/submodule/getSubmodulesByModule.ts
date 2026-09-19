import apiClient from "../../lib/api";

export const getSubmodulesByModule = async (moduleId: number) => {
  try {
    const response = await apiClient.get(`/api/v1/module/module/${moduleId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      data: items.map((s: any) => ({
        id: s.subModuleId || s.id,
        subModuleId: s.subModuleId || s.id,
        name: s.subModuleName || s.name || '',
        subModuleName: s.subModuleName || s.name || '',
        moduleId: s.moduleId || moduleId,
        developerIds: s.developerIds || [],
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return { status: 'success', statusCode: 200, data: [] };
    }
    throw err;
  }
};

export const getSubmodulesByModuleId = getSubmodulesByModule;
