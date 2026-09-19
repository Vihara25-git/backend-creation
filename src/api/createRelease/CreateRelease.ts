import apiClient from "../../lib/api";

export interface CreateReleaseRequest {
  name: string;
  releaseDate: string;
  releaseType_name?: string;
  releaseTypeId?: number;
  releaseType_id?: string | number;
  project_id: number;
  status?: string;
  description?: string;
  version?: string;
}

export interface CreateReleaseResponse {
  status: string;
  message: string;
  data: any;
  statusCode: number;
}

export const createRelease = async (payload: CreateReleaseRequest): Promise<any> => {
  const releaseTypeId = Number(payload.releaseTypeId || (payload as any).releaseType_id) || 1;
  const body = {
    releaseName: payload.name,
    releaseVersion: payload.version || 'v1.0.0',
    releaseDate: payload.releaseDate ? payload.releaseDate.split('T')[0] : new Date().toISOString().split('T')[0],
    projectId: Number(payload.project_id),
    releaseTypeId,
  };

  const response = await apiClient.post('/api/v1/ReleaseView/save', body);
  return {
    status: 'success',
    message: response.data?.statusMessage || 'Release created successfully',
    statusCode: response.status || 200,
    data: response.data?.data || response.data,
  };
};
