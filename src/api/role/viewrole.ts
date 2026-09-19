import apiClient from "../../lib/api";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Role {
  id: number;
  name: string;
  type?: string;
  roleType?: string;
}

export interface GetRolesResponse {
  status: string;
  message: string;
  data: {
    content: Role[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
  };
}

export const getAllRoles = async (page: number = 0, pageSize: number = 100): Promise<GetRolesResponse> => {
  const response = await apiClient.get(ENDPOINTS.role(page, pageSize));
  const pageData = response.data?.data || response.data || {};
  const content = (pageData.content || []).map((r: any) => ({
    id: r.roleId ?? r.id,
    name: r.roleName ?? r.name ?? '',
    type: r.roleType ?? r.type ?? '',
    roleType: r.roleType ?? r.type ?? '',
  }));

  return {
    status: 'success',
    message: response.data?.statusMessage || 'Roles fetched successfully',
    data: {
      content,
      totalElements: pageData.totalElements ?? content.length,
      totalPages: pageData.totalPages ?? 1,
      pageNumber: pageData.number ?? page,
      pageSize: pageData.size ?? pageSize,
    },
  };
};
