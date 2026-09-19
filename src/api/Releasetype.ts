import apiClient from '../lib/api';

export interface Releasetype {
  id: number;
  releaseTypeName: string;
  name?: string;
}

export interface ReleaseTypeList {
  totalElements?: number;
  content?: Releasetype[];
}

export interface ReleaseTypeResponse {
  status: string;
  statusMessage: string;
  data: ReleaseTypeList;
  statusCode: number;
}

export interface CreateReleaseTypeRequest {
  releaseTypeName: string;
}

export interface UpdateReleaseTypeRequest {
  releaseTypeName: string;
}

export const getAllReleaseTypes = async (_page: number = 0, _size: number = 100): Promise<ReleaseTypeResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/release_types?page=${_page}&size=${_size}`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusMessage: 'Success',
      statusCode: 200,
      data: {
        totalElements: resData?.totalElements ?? items.length,
        content: items.map((r: any) => ({
          id: r.id,
          releaseTypeName: r.type || r.releaseTypeName || r.name || '',
          name: r.type || r.name || r.releaseTypeName || '',
        })),
      },
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        statusMessage: 'Success',
        statusCode: 200,
        data: {
          totalElements: 0,
          content: [],
        },
      };
    }
    throw err;
  }
};

export const createReleaseType = async (data: CreateReleaseTypeRequest): Promise<Releasetype> => {
  const response = await apiClient.post('/api/v1/release_types', {
    type: data.releaseTypeName,
  });
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id,
    releaseTypeName: resData?.type || data.releaseTypeName,
    name: resData?.type || data.releaseTypeName,
  };
};

export const updateReleaseType = async (id: number, data: UpdateReleaseTypeRequest): Promise<Releasetype> => {
  const response = await apiClient.put(`/api/v1/release_types/${id}`, {
    type: data.releaseTypeName,
  });
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id || id,
    releaseTypeName: resData?.type || data.releaseTypeName,
    name: resData?.type || data.releaseTypeName,
  };
};

export const deleteReleaseType = async (id: number): Promise<any> => {
  const response = await apiClient.delete(`/api/v1/release_types/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Release type deleted successfully',
    statusMessage: response.data?.statusMessage || 'Release type deleted successfully',
  };
};