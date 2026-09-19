import apiClient from "../../lib/api";

export interface BulkSubmodule {
  subModuleId: number;
  subModuleName: string;
  moduleId: number;
  moduleName: string;
}

export const getBulkSubmodulesByModules = async (
  projectId: string | number,
  moduleIds: number[] | string
): Promise<BulkSubmodule[]> => {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/module`);
    const resData = response.data?.data || response.data;
    const modules = Array.isArray(resData) ? resData : [];

    const ids = Array.isArray(moduleIds)
      ? moduleIds.map(Number)
      : String(moduleIds).split(',').map(Number);

    const result: BulkSubmodule[] = [];
    modules.forEach((m: any) => {
      const mId = Number(m.moduleId || m.id);
      if (ids.length === 0 || ids.includes(mId)) {
        (m.subModules || m.submodules || []).forEach((s: any) => {
          result.push({
            subModuleId: Number(s.subModuleId || s.id),
            subModuleName: s.subModuleName || s.name || 'Submodule',
            moduleId: mId,
            moduleName: m.moduleName || m.name || 'Module',
          });
        });
      }
    });

    return result;
  } catch (err) {
    console.error('Failed to get bulk submodules:', err);
    return [];
  }
};
