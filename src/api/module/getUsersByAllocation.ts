import apiClient from "../../lib/api";

export interface UserByAllocation {
  userId: number;
  userName: string;
  userRole?: string;
  userWithRole: string;
  allocateModuleId?: number;
  allocationId?: number;
  moduleName?: string;
  projectName?: string;
  moduleId?: number;
  projectId?: number;
  subModuleId?: number;
}

export const getUsersByAllocation = async (projectId: number, moduleId: number): Promise<UserByAllocation[]> => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((item: any) => {
      const fullName = item.employeeName || (item.firstName && item.lastName ? `${item.firstName} ${item.lastName}`.trim() : item.email || `Employee ${item.employeeId}`);
      const role = item.isLeader ? 'QA Lead' : 'QA Engineer';
      return {
        userId: item.employeeId,
        userName: fullName,
        userRole: role,
        userWithRole: `${fullName} - ${role}`,
        allocateModuleId: item.id || item.modQaId,
        allocationId: item.id || item.modQaId,
        moduleId: item.moduleId || moduleId,
        projectId,
      };
    });
  } catch {
    return [];
  }
};

export const getUsersBySubmoduleAllocation = async (projectId: number, moduleId: number, subModuleId: number): Promise<UserByAllocation[]> => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId || 0}/sub-module/${subModuleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((a: any) => {
      const name = a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`);
      return {
        allocationId: a.submoduleDevId || a.id,
        userId: a.employeeId || a.employee?.empId || a.empId,
        userName: name,
        userWithRole: `${name} - Developer`,
        userRole: 'Developer',
        moduleId,
        projectId,
        subModuleId,
      };
    });
  } catch {
    return [];
  }
};

export async function getUsersByModuleSubmoduleAllocation(projectId: number) {
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      message: 'Developers retrieved successfully',
      data: items.map((a: any) => ({
        employeeId: a.empId || a.employeeId,
        employeeName: a.employeeName || `${a.firstName || ''} ${a.lastName || ''}`.trim() || 'Developer',
        roleName: a.roleName || 'Developer',
        projectId,
      })),
      statusCode: 200,
    };
  } catch {
    return {
      status: 'success',
      message: 'Developers retrieved successfully',
      data: [],
      statusCode: 200,
    };
  }
}