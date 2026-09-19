import apiClient from '../lib/api';

export interface ApiDefectType {
  id: number;
  defectTypeName: string;
  name?: string;
  description: string;
  category: 'functional' | 'performance' | 'security' | 'usability' | 'compatibility' | 'other';
  severity: 'low' | 'medium' | 'high' | 'critical';
  priority: 'low' | 'medium' | 'high' | 'critical';
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface GetDefectTypesResponse {
  status: string;
  message: string;
  data: {
    content: ApiDefectType[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  };
}

export const createDefectType = async (data: { name: string }) => {
  const response = await apiClient.post('/api/v1/defecttype/create', {
    defectTypeName: data.name
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Defect type created successfully',
    statusMessage: response.data?.statusMessage || 'Defect type created successfully',
    data: {
      id: resData?.defectTypeId || resData?.id,
      name: resData?.defectTypeName || data.name,
      defectTypeName: resData?.defectTypeName || data.name
    },
  };
};

export const updateDefectType = async (id: number, data: { name: string }) => {
  const response = await apiClient.patch(`/api/v1/defecttype/update/${id}`, {
    defectTypeName: data.name
  });
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Defect type updated successfully',
    statusMessage: response.data?.statusMessage || 'Defect type updated successfully',
    data: {
      id: resData?.defectTypeId || id,
      name: resData?.defectTypeName || data.name,
      defectTypeName: resData?.defectTypeName || data.name
    },
  };
};

export const getDefectTypes = async (_page = 0, _size = 100): Promise<GetDefectTypesResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/defecttype/getAll?page=${_page}&size=${_size}`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);
    const now = new Date().toISOString();

    return {
      status: 'success',
      message: 'Success',
      data: {
        content: items.map((d: any) => ({
          id: d.defectTypeId || d.id,
          defectTypeName: d.defectTypeName || d.name || '',
          name: d.defectTypeName || d.name || '',
          description: d.description || d.defectTypeName || '',
          category: 'functional',
          severity: 'medium',
          priority: 'medium',
          isActive: true,
          createdAt: now,
          updatedAt: now,
        })),
        totalElements: resData?.totalElements ?? items.length,
        totalPages: resData?.totalPages ?? 1,
        pageNumber: resData?.number ?? _page,
        pageSize: resData?.size ?? _size,
      },
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      // Backend returns 404 when defect type table is empty
      return {
        status: 'success',
        message: 'Success',
        data: {
          content: [],
          totalElements: 0,
          totalPages: 1,
          pageNumber: _page,
          pageSize: _size,
        },
      };
    }
    throw err;
  }
};

export const deleteDefectType = async (id: number) => {
  const response = await apiClient.delete(`/api/v1/defecttype/delete/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Defect type deleted successfully',
    statusMessage: response.data?.statusMessage || 'Defect type deleted successfully',
  };
};