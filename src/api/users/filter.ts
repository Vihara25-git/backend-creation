import apiClient from "../../lib/api";

export interface UserFilter {
  id: number;
  userId?: string;
  firstName: string;
  lastName: string;
  email: string;
  userGender?: string;
  status?: string;
  designationId?: number;
  designationName?: string;
}

export async function getUsersByFilter(
  gender?: string,
  status?: string,
  designation?: string,
  page: number = 1,
  size: number = 10
) {
  try {
    const params = new URLSearchParams();
    if (gender) params.append('gender', gender);
    if (status) params.append('status', status);
    if (designation) params.append('designation', designation);

    const response = await apiClient.get(`/api/v1/Employee/search?${params.toString()}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    const mapped = items.map((u: any) => ({
      id: u.empId || u.id,
      userId: `EMP${String(u.empId || u.id).padStart(4, '0')}`,
      firstName: u.firstName,
      lastName: u.lastName,
      email: u.email,
      userGender: u.gender,
      status: u.status,
      designationId: u.designationId,
      designationName: u.designationName,
    }));

    const start = (page - 1) * size;
    return mapped.slice(start, start + size);
  } catch (err: any) {
    if (err.response?.status === 404) return [];
    throw err;
  }
}

export const filterUsers = getUsersByFilter;
