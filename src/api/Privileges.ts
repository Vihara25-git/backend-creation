import apiClient from "../lib/api";

export interface Permission {
  permissionId: number;
  action: string;
  description: string | null;
}

export type PermissionId = number | string;

export interface ModulePermission {
  module: string;
  permissions: Permission[];
}

export interface GetPrivilegesResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: ModulePermission[];
}

export interface PermissionAssignmentChange {
  permissionId: string | number;
  isAssigned: boolean;
}

export interface EmployeePermissionUpdatePayload {
  permissionIds: number[];
}

export interface RolePermissionResponse {
  status: string;
  statusCode: number | string;
  statusMessage: string;
  data: {
    permissionIds: number[];
    messages: string[];
  };
}

export interface UserPrivilegeResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    module: string;
    permissions: {
      permissionId: number;
      action: string;
      description: string | null;
      checked: boolean;
      inheritedFromRole: boolean;
    }[];
  }[];
}

export interface RolePermissionByRoleResponse {
  permissionIds: PermissionId[];
}

export const getAllPrivileges = async (): Promise<GetPrivilegesResponse> => {
  try {
    const response = await apiClient.get('/api/v1/permission');
    const resData = response.data?.data || response.data;
    const modules = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: response.status || 200,
      statusMessage: response.data?.statusMessage || 'Success',
      data: modules.map((m: any) => ({
        module: m.module || m.moduleName || 'Module',
        permissions: (m.permissions || []).map((p: any) => ({
          permissionId: p.permissionId || p.id,
          action: p.action || p.name || '',
          description: p.description || null,
        })),
      })),
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch permissions',
      data: [],
    };
  }
};

export const getRolePermission = async (roleId: number): Promise<RolePermissionResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/assign-permission/matrix/${roleId}`);
    const resData = response.data?.data || response.data;

    return {
      status: 'success',
      statusCode: response.status || 200,
      statusMessage: 'Success',
      data: {
        permissionIds: Array.isArray(resData?.permissionIds) ? resData.permissionIds : [],
        messages: Array.isArray(resData?.messages) ? resData.messages : ['Retrieved successfully'],
      },
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch role permissions',
      data: { permissionIds: [], messages: [] },
    };
  }
};

export const getRolePermissionByRoleId = async (
  roleId: number | string
): Promise<RolePermissionByRoleResponse> => {
  const res = await getRolePermission(Number(roleId));
  return {
    permissionIds: res.data.permissionIds,
  };
};

export const addRolePermission = async (
  roleId: number,
  rolePermissions: PermissionAssignmentChange[]
): Promise<RolePermissionResponse> => {
  const payload = rolePermissions.map(p => ({
    permissionId: String(p.permissionId),
    isAssigned: Boolean(p.isAssigned),
  }));

  const response = await apiClient.post(`/api/v1/assign-permission/matrix/${roleId}`, payload);
  const resData = response.data?.data || response.data;

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: response.data?.statusMessage || 'Role permissions updated successfully',
    data: {
      permissionIds: Array.isArray(resData?.permissionIds) ? resData.permissionIds : [],
      messages: Array.isArray(resData?.messages) ? resData.messages : ['Updated successfully'],
    },
  };
};

export const getAllEmployeePermission = async (employeeId: number): Promise<UserPrivilegeResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/employee/${employeeId}/permission`);
    const resData = response.data?.data || response.data;
    const modules = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: response.status || 200,
      statusMessage: 'Success',
      data: modules.map((m: any) => ({
        module: m.module || m.moduleName || 'Module',
        permissions: (m.permissions || []).map((p: any) => ({
          permissionId: p.permissionId || p.id,
          action: p.action || '',
          description: p.description || null,
          checked: Boolean(p.checked || p.isAssigned),
          inheritedFromRole: Boolean(p.inheritedFromRole),
        })),
      })),
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch employee permissions',
      data: [],
    };
  }
};

export const addEmployeePermission = async (
  employeeId: number,
  payload: EmployeePermissionUpdatePayload
): Promise<UserPrivilegeResponse> => {
  const response = await apiClient.post(`/api/v1/employee/${employeeId}/permission`, {
    permissionIds: payload.permissionIds,
  });
  const resData = response.data?.data || response.data;
  const modules = Array.isArray(resData) ? resData : [];

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: 'Employee permissions updated successfully',
    data: modules.map((m: any) => ({
      module: m.module || m.moduleName || 'Module',
      permissions: (m.permissions || []).map((p: any) => ({
        permissionId: p.permissionId || p.id,
        action: p.action || '',
        description: p.description || null,
        checked: Boolean(p.checked || p.isAssigned),
        inheritedFromRole: Boolean(p.inheritedFromRole),
      })),
    })),
  };
};

export interface PrivilegeTemplateItem {
  id: number;
  type: string;
  subType: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreatePrivilegeTemplatePayload {
  type: string;
  subType: string;
  description?: string;
}

export const getAllPrivilegeTemplates = async (): Promise<PrivilegeTemplateItem[]> => {
  try {
    const response = await apiClient.get('/api/v1/privilege-templates');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];
    return items.map((item: any) => ({
      id: item.id,
      type: item.type || '',
      subType: item.subType || '',
      description: item.description || '',
      createdAt: item.createdAt,
      updatedAt: item.updatedAt,
    }));
  } catch (err) {
    console.error('Error fetching privilege templates:', err);
    return [];
  }
};

export const createPrivilegeTemplate = async (
  payload: CreatePrivilegeTemplatePayload
): Promise<PrivilegeTemplateItem> => {
  const response = await apiClient.post('/api/v1/privilege-templates', payload);
  const item = response.data?.data || response.data;
  return {
    id: item.id,
    type: item.type || '',
    subType: item.subType || '',
    description: item.description || '',
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
};

export const updatePrivilegeTemplate = async (
  id: number,
  payload: CreatePrivilegeTemplatePayload
): Promise<PrivilegeTemplateItem> => {
  const response = await apiClient.put(`/api/v1/privilege-templates/${id}`, payload);
  const item = response.data?.data || response.data;
  return {
    id: item.id,
    type: item.type || '',
    subType: item.subType || '',
    description: item.description || '',
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
};

export const deletePrivilegeTemplate = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/v1/privilege-templates/${id}`);
};
