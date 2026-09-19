import apiClient from "../../lib/api";
import { ENDPOINTS } from "../../utils/apiendpoint";

export const updateRole = async (id: number, data: { name: string; type?: string }) => {
  const response = await apiClient.put(ENDPOINTS.roleUpdate(id), {
    roleName: data.name,
    roleType: data.type || data.name.toUpperCase().replace(/\s+/g, '_'),
  });
  const resData = response.data?.data || response.data || {};
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Role updated successfully',
    statusMessage: response.data?.statusMessage || 'Role updated successfully',
    data: {
      id: resData.roleId ?? resData.id ?? id,
      name: resData.roleName ?? resData.name ?? data.name,
      type: resData.roleType ?? resData.type ?? data.type,
    },
  };
};