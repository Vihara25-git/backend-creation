import apiClient from "../../lib/api";

export const getDevelopersByModuleId = async (_projectId: number, moduleId: number) => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((a: any) => {
      const name = a.employeeName || (a.firstName && a.lastName ? `${a.firstName} ${a.lastName}`.trim() : a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : a.email || `Employee ${a.employeeId}`);
      const role = a.isLeader ? 'QA Lead' : 'QA Engineer';
      return {
        id: a.id || a.modQaId,
        allocateModuleId: a.id || a.modQaId,
        projectAllocationId: a.id || a.modQaId,
        employeeId: a.employeeId || a.empId,
        userId: a.employeeId || a.empId,
        name,
        userName: name,
        email: a.email || a.employee?.email || '',
        role,
        userRole: role,
        userWithRole: `${name} - ${role}`,
        isLeader: a.isLeader ?? true,
        subModuleId: null,
      };
    });
  } catch {
    return [];
  }
};

export const getDevelopersBySubmoduleId = async (_projectId: number, moduleId: number, submoduleId: number) => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId || 0}/sub-module/${submoduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((a: any) => ({
      id: a.submoduleDevId || a.id,
      employeeId: a.employeeId || a.employee?.empId || a.empId,
      name: a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`),
      userName: a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`),
      email: a.employee?.email || '',
      subModuleId: submoduleId,
    }));
  } catch {
    return [];
  }
};