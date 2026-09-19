import apiClient from "../../lib/api";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const deleterole = async (id: number) => {
  const response = await apiClient.delete(ENDPOINTS.roleDelete(id));
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Role deleted successfully',
    statusMessage: response.data?.statusMessage || 'Role deleted successfully',
  };
};
