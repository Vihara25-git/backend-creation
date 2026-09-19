import apiClient from '../lib/api';

export interface Severity {
  id: number;
  name: string;
  color: string;
  weight: number;
}

export interface CreateSeverityRequest {
  name: string;
  color: string;
  weight: number;
}

export interface CreateSeverityResponse {
  status: string;
  message: string;
  statusCode: number;
  data?: Severity;
}

export interface GetSeveritiesResponse {
  status: string;
  message: string;
  data: {
    content: Severity[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}

export const createSeverity = async (data: CreateSeverityRequest): Promise<CreateSeverityResponse> => {
  const response = await apiClient.post('/api/v1/severity', {
    severityName: data.name,
    colorCode: data.color,
    weight: data.weight,
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    message: response.data?.statusMessage || 'Severity created successfully',
    statusCode: response.status || 201,
    data: {
      id: resData?.severityId || resData?.id,
      name: resData?.severityName || data.name,
      color: resData?.colorCode || data.color,
      weight: resData?.weight || data.weight,
    },
  };
};

export const updateSeverity = async (id: number, data: Partial<CreateSeverityRequest>): Promise<CreateSeverityResponse> => {
  const response = await apiClient.put(`/api/v1/severity/${id}`, {
    severityName: data.name,
    colorCode: data.color,
    weight: data.weight,
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    message: response.data?.statusMessage || 'Severity updated successfully',
    statusCode: response.status || 200,
    data: {
      id: resData?.severityId || id,
      name: resData?.severityName || data.name || '',
      color: resData?.colorCode || data.color || '',
      weight: resData?.weight || data.weight || 1,
    },
  };
};

export const getSeverities = async (
  _page: number = 0,
  _pageSize: number = 100
): Promise<GetSeveritiesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/severity?page=${_page}&size=${_pageSize}`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      message: 'Severities fetched successfully',
      data: {
        content: items.map((s: any) => ({
          id: s.severityId || s.id,
          name: s.severityName || s.name || '',
          color: s.colorCode || s.color || '#000000',
          weight: s.weight || 1,
        })),
        totalElements: resData?.totalElements ?? items.length,
        totalPages: resData?.totalPages ?? 1,
        size: resData?.size ?? _pageSize,
        number: resData?.number ?? _page,
      },
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        message: 'Severities fetched successfully',
        data: {
          content: [],
          totalElements: 0,
          totalPages: 1,
          size: _pageSize,
          number: _page,
        },
      };
    }
    throw err;
  }
};

export const deleteSeverity = async (id: number) => {
  const response = await apiClient.delete(`/api/v1/severity/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Severity deleted successfully',
    statusMessage: response.data?.statusMessage || 'Severity deleted successfully',
  };
};
