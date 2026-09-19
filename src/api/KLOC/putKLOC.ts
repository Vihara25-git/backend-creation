import apiClient from "../../lib/api";

export interface CalculateKlocRequest {
  backendRepo: string;
  frontendRepo: string;
  githubUsername: string;
  githubToken: string;
}

export interface CalculateKlocResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    backendLOC: number;
    frontendLOC: number;
    backendKLOC: number;
    frontendKLOC: number;
    totalKLOC: number;
  };
}

export interface UpdateKlocResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    kiloOfCode: number;
  };
}

export const createProjectKloc = async (projectId: number, kloc: number): Promise<UpdateKlocResponse> => {
  const response = await apiClient.post(`/api/v1/project/${projectId}/kloc`, {
    kloc,
  });
  const resData = response.data?.data ?? response.data;

  return {
    status: 'success',
    statusCode: response.status || 201,
    statusMessage: 'Project KLOC created successfully',
    data: {
      kiloOfCode: typeof resData === 'number' ? resData : (resData?.kloc || kloc),
    },
  };
};

export const updateProjectKloc = async (projectId: number, kloc: number): Promise<UpdateKlocResponse> => {
  const response = await apiClient.patch(`/api/v1/project/${projectId}/kloc`, {
    kloc,
  });
  const resData = response.data?.data ?? response.data;

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: 'Project KLOC updated successfully',
    data: {
      kiloOfCode: typeof resData === 'number' ? resData : (resData?.kloc || kloc),
    },
  };
};

export const deleteProjectKloc = async (projectId: number): Promise<{ status: string; message: string }> => {
  const response = await apiClient.delete(`/api/v1/project/${projectId}/kloc`);
  return {
    status: 'success',
    message: response.data?.statusMessage || 'Project KLOC deleted successfully',
  };
};

export const calculateKlocFromGithub = async (
  payload: CalculateKlocRequest,
  projectId?: number
): Promise<CalculateKlocResponse> => {
  const pId = projectId || 1;
  const response = await apiClient.post(`/api/v1/project/${pId}/calculate-kloc`, payload);
  const resData = response.data?.data || response.data;

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: 'KLOC calculated from GitHub successfully',
    data: {
      backendLOC: resData?.backendLOC || 0,
      frontendLOC: resData?.frontendLOC || 0,
      backendKLOC: resData?.backendKLOC || 0,
      frontendKLOC: resData?.frontendKLOC || 0,
      totalKLOC: resData?.totalKLOC || 0,
    },
  };
};
