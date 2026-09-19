import apiClient from "../../lib/api";

interface DefectTypeItem {
  defectTypeName: string;
  defectCount: number;
  percentage: number;
}

export interface DefectTypeResponse {
  status: string;
  statusCode?: number;
  statusMessage?: string;
  data: {
    defectTypes: DefectTypeItem[];
    totalDefectCount: number;
    mostCommonDefectType: string;
    mostCommonDefectCount: number;
  };
}

export async function getDefectTypeByProjectId(
  projectId: string | number
): Promise<DefectTypeResponse> {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/dashboard/defect-type`);
    const resData = response.data?.data || response.data;

    const rawList = Array.isArray(resData?.defectTypes) ? resData.defectTypes : [];
    const defectTypes: DefectTypeItem[] = rawList.map((dt: any) => ({
      defectTypeName: dt.defectTypeName || dt.name || '',
      defectCount: Number(dt.defectCount ?? dt.count ?? 0),
      percentage: Number(dt.percentage ?? 0),
    }));

    return {
      status: 'success',
      statusCode: 200,
      data: {
        defectTypes,
        totalDefectCount: Number(resData?.totalDefectCount ?? 0),
        mostCommonDefectType: resData?.mostCommonDefectType || '',
        mostCommonDefectCount: Number(resData?.mostCommonDefectCount ?? 0),
      },
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch defect types distribution',
      data: {
        defectTypes: [],
        totalDefectCount: 0,
        mostCommonDefectType: '',
        mostCommonDefectCount: 0,
      },
    };
  }
}