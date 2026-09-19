import apiClient from "../lib/api";

export type RoleType =
  | "ADMIN"
  | "PROJECT_MANAGER"
  | "QA_LEAD"
  | "QA_ENGINEER"
  | "DEV_LEAD"
  | "SENIOR_DEVELOPER"
  | "DEVELOPER"
  | "JUNIOR_DEVELOPER"
  | "BUSINESS_ANALYST"
  | "UI_UX_DESIGNER"
  | "DEVOPS_ENGINEER"
  | "SUPPORT_ENGINEER"
  | "CLIENT";

async function fetchBackendRoles(): Promise<any[]> {
  try {
    const res = await apiClient.get('/api/v1/Role?page=0&size=100');
    const data = res.data?.data || res.data;
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.content)) return data.content;
    return [];
  } catch {
    return [];
  }
}

export const roleTypeBasedRoleFetch = async (
  roleType: RoleType,
): Promise<string[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => (role.type as string) === roleType || role.roleName?.toUpperCase().replace(/\s+/g, '_') === roleType || role.roleName?.toUpperCase().includes(roleType.replace('_', ' ')))
    .map((role) => role.roleName);
};

export const roleTypesBasedRoleFetch = async (
  roleTypes: RoleType[],
): Promise<string[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => {
      const typeStr = (role.type || '') as RoleType;
      return roleTypes.includes(typeStr) || roleTypes.some(t => role.roleName?.toUpperCase().replace(/\s+/g, '_') === t || role.roleName?.toUpperCase().includes(t.replace('_', ' ')));
    })
    .map((role) => role.roleName);
};

export const roleTypeBasedRoleIdFetch = async (
  roleType: RoleType,
): Promise<number[]> => {
  const roles = await fetchBackendRoles();
  return roles
    .filter((role) => (role.type as string) === roleType || role.roleName?.toUpperCase().replace(/\s+/g, '_') === roleType || role.roleName?.toUpperCase().includes(roleType.replace('_', ' ')))
    .map((role) => role.roleId || role.id);
};