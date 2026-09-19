import apiClient from "../../lib/api";

export async function createUser(userData: any) {
  const payload = {
    firstName: userData.firstName,
    lastName: userData.lastName,
    gender: userData.gender || 'Male',
    designationId: Number(userData.designationId),
    whatsappNumber: userData.whatsappNumber || userData.contactNo || '',
    email: userData.email,
    joinDate: userData.joinDate ? userData.joinDate.split('T')[0] : new Date().toISOString().split('T')[0],
    password: userData.password || 'Employee@123',
  };

  const response = await apiClient.post('/api/v1/Employee/createEmployee', payload);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Employee created successfully',
    statusMessage: response.data?.statusMessage || 'Employee created successfully',
    data: response.data?.data || response.data,
  };
}