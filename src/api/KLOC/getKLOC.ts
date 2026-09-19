import apiClient from "../../lib/api";

export interface DefectDensityData {
  kloc: number;
  totalDefects: number;
  defectDensity: number;
}

export interface DefectDensityResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: DefectDensityData;
}

export interface KlocResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    kloc: number;
  };
}

export const getKILOC = async (projectId: number): Promise<KlocResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/kloc`);
    const resData = response.data?.data ?? response.data;
    return {
      status: 'success',
      statusCode: response.status || 200,
      statusMessage: 'KLOC fetched successfully',
      data: {
        kloc: typeof resData === 'number' ? resData : (resData?.kloc || 0),
      },
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch KLOC',
      data: { kloc: 0 },
    };
  }
};

export const getDefectDensity = async (projectId: number): Promise<DefectDensityResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/defect-density`);
    const resData = response.data?.data ?? response.data;
    return {
      status: 'success',
      statusCode: response.status || 200,
      statusMessage: 'Defect density fetched successfully',
      data: {
        kloc: resData?.kloc || 0,
        totalDefects: resData?.totalDefects || 0,
        defectDensity: resData?.defectDensity || 0,
      },
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch defect density',
      data: {
        kloc: 0,
        totalDefects: 0,
        defectDensity: 0,
      },
    };
  }
};