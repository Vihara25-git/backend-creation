import apiClient from '../lib/api';

export interface Priority {
  id: number;
  name: string;
  color: string;
}

export interface GetPrioritiesResponse {
  status: string;
  message: string;
  data: {
    content: Priority[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}

export const getAllPriorities = async (
  _page: number = 0,
  _pageSize: number = 100
): Promise<GetPrioritiesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/priority/view?page=${_page}&size=${_pageSize}`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      message: 'Priorities fetched successfully',
      data: {
        content: items.map((p: any) => ({
          id: p.priorityId || p.id,
          name: p.priorityName || p.name || '',
          color: p.colorCode || p.color || '#000000',
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
        message: 'Priorities fetched successfully',
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

export const updatePriority = async (id: number, data: { name: string; color: string }) => {
  const response = await apiClient.put(`/api/v1/priority/update/${id}`, {
    priorityName: data.name,
    colorCode: data.color,
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Priority updated successfully',
    statusMessage: response.data?.statusMessage || 'Priority updated successfully',
    data: {
      id: resData?.priorityId || id,
      name: resData?.priorityName || data.name,
      color: resData?.colorCode || data.color,
    },
  };
};

export const deletePriority = async (id: number) => {
  const response = await apiClient.delete(`/api/v1/priority/delete/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Priority deleted successfully',
    statusMessage: response.data?.statusMessage || 'Priority deleted successfully',
  };
};

export const createPriority = async (data: { name: string; color: string }) => {
  const response = await apiClient.post('/api/v1/priority/save', {
    priorityName: data.name,
    colorCode: data.color,
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Priority created successfully',
    statusMessage: response.data?.statusMessage || 'Priority created successfully',
    data: {
      id: resData?.priorityId || resData?.id,
      name: resData?.priorityName || data.name,
      color: resData?.colorCode || data.color,
    },
  };
};