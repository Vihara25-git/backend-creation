import apiClient from "../../lib/api";

export async function getBenchList(): Promise<any[]> {
  try {
    const response = await apiClient.get('/api/v1/bench-availability-view?page=0&size=1000');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);
    return items;
  } catch {
    return [];
  }
}

export const getViewAllocation = async (userId: string) => {
  try {
    const response = await apiClient.get(`/api/v1/bench-availability-view/employee/${userId}`);
    const resData = response.data?.data || response.data;
    return { data: resData || {} };
  } catch {
    return { data: {} };
  }
};

export async function getEmployeeDetails(id: string): Promise<any> {
  try {
    const response = await apiClient.get(`/api/v1/Employee/${id}`);
    const resData = response.data?.data || response.data;
    return Array.isArray(resData) ? resData[0] : resData;
  } catch {
    return null;
  }
}

export const getBenchAvailability = async (page: number = 0, size: number = 5, filters: any = {}) => {
  try {
    const params = new URLSearchParams();
    params.append('page', String(page));
    params.append('size', String(size));
    if (filters.name?.trim()) params.append('search', filters.name.trim());
    if (filters.designation) params.append('designationName', filters.designation);
    if (filters.minAvailable) params.append('availablePercentage', String(filters.minAvailable));
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);

    const url = (filters.name || filters.designation || filters.minAvailable || filters.startDate || filters.endDate)
      ? `/api/v1/bench-availability-view/filter?${params.toString()}`
      : `/api/v1/bench-availability-view?${params.toString()}`;

    const response = await apiClient.get(url);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusCode: 200,
      data: {
        content: items,
        data: items,
        totalElements: resData?.totalElements ?? items.length,
        totalPages: resData?.totalPages ?? 1,
        size: resData?.size ?? size,
        number: resData?.number ?? page,
      },
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: {
        content: [],
        data: [],
        totalElements: 0,
        totalPages: 1,
        size,
        number: page,
      },
    };
  }
};

export const getEmployeeProjectHistory = async (userId: string) => {
  try {
    const response = await apiClient.get(`/api/v1/bench-availability-view/employee/${userId}`);
    return { data: response.data?.data || response.data };
  } catch {
    return { data: [] };
  }
};