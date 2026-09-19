import apiClient from "../../lib/api";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const createRoles = async (data: { name: string; type?: string }) => {
  const response = await apiClient.post(ENDPOINTS.roleCreate, {
    roleName: data.name,
    roleType: data.type || data.name.toUpperCase().replace(/\s+/g, '_'),
  });
  const resData = response.data?.data || response.data || {};
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Role created successfully',
    statusMessage: response.data?.statusMessage || 'Role created successfully',
    data: {
      id: resData.roleId ?? resData.id,
      name: resData.roleName ?? resData.name ?? data.name,
      type: resData.roleType ?? resData.type ?? data.type,
    },
  };
};