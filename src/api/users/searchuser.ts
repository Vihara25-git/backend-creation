import apiClient from "../../lib/api";

export interface SearchUserData {
  id: number;
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  userStatus: string;
  userGender: string;
  designationName: string;
}

export async function searchUsers(searchTerm: string) {
  try {
    const response = await apiClient.get(`/api/v1/Employee/search?keyword=${encodeURIComponent(searchTerm)}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((u: any) => ({
      id: u.empId || u.id,
      userId: `EMP${String(u.empId || u.id).padStart(4, '0')}`,
      firstName: u.firstName,
      lastName: u.lastName,
      email: u.email,
      userStatus: u.status || (u.isActive ? 'ACTIVE' : 'INACTIVE'),
      userGender: u.gender,
      designationName: u.designationName || '',
    }));
  } catch (err: any) {
    if (err.response?.status === 404) return [];
    throw err;
  }
}
