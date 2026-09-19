import apiClient from "../../lib/api";

interface DefectByModule {
  name: string;
  value: number;
  percentage?: number;
}

export interface DefectsByModuleResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: DefectByModule[];
}

export async function getDefectsByModule(
  projectId: number
): Promise<DefectsByModuleResponse> {
  try {
    const response = await apiClient.get(`/api/v1/defect/module-summary/${projectId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    const mapped: DefectByModule[] = items.map((m: any) => ({
      name: m.moduleName || m.name || 'Module',
      value: Number(m.defectCount ?? m.count ?? m.value ?? 0),
      percentage: Number(m.percentage ?? 0),
    }));

    return {
      status: 'success',
      statusCode: 200,
      statusMessage: 'Success',
      data: mapped,
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch defects by module',
      data: [],
    };
  }
}