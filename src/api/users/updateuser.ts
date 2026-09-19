import apiClient from "../../lib/api";

export interface UpdateUserPayload {
  id?: number;
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
  contactNo?: string;
  whatsappNumber?: string;
  joinDate?: string;
  gender?: "Male" | "Female" | string;
  designationId?: number;
  password?: string;
}

export async function updateUser(id: number, userData: UpdateUserPayload) {
  const payload = {
    firstName: userData.firstName,
    lastName: userData.lastName,
    gender: userData.gender || 'Male',
    designationId: Number(userData.designationId),
    whatsappNumber: userData.whatsappNumber || userData.contactNo || '',
    email: userData.email,
    joinDate: userData.joinDate ? userData.joinDate.split('T')[0] : new Date().toISOString().split('T')[0],
    password: userData.password,
  };

  const response = await apiClient.put(`/api/v1/Employee/update/${id}`, payload);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Employee updated successfully',
    statusMessage: response.data?.statusMessage || 'Employee updated successfully',
    data: response.data?.data || response.data,
  };
}

export async function updateUserStatus(id: number, status: boolean) {
  const response = await apiClient.patch(`/api/v1/Employee/status/${id}`, {
    isActive: Boolean(status),
  });
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Employee status updated successfully',
    statusMessage: response.data?.statusMessage || 'Employee status updated successfully',
    data: response.data?.data || response.data,
  };
}
