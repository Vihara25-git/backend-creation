import apiClient from "../../lib/api";

export async function deleteUser(id: number) {
  const response = await apiClient.delete(`/api/v1/Employee/DeleteEmployee/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Employee deleted successfully',
    statusMessage: response.data?.statusMessage || 'Employee deleted successfully',
  };
}