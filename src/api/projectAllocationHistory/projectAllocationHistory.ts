import apiClient from "../../lib/api";
import { postProjectAllocations, updateProjectAllocation, deleteProjectAllocation } from "../bench/projectAllocation";

export interface getProjectAllocationHistoryResponse {
  status: string;
  message: string;
  data: any[];
  statusCode: number;
}

export const getProjectAllocationHistory = async (projectId: number): Promise<any[]> => {
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((a: any) => {
      const empId = a.empId || a.employeeId;
      const firstName = a.firstName || a.employeeName?.split(' ')[0] || `Employee`;
      const lastName = a.lastName || a.employeeName?.split(' ').slice(1).join(' ') || `${empId}`;
      const fullName = a.employeeName || `${firstName} ${lastName}`.trim();
      const email = a.email || `${firstName.toLowerCase()}@sgic.com`;
      const isActive = a.status !== false && a.status !== 'DEALLOCATED' && a.status !== 'INACTIVE';
      const percentage = a.availability || a.allocationPercent || 50;

      const record = {
        id: a.benchAllocationId || a.id,
        action: 'allocated',
        percentage,
        roleName: a.roleName || 'Developer',
        roleId: a.roleId || 1,
        startDate: a.startDate || '2026-01-01',
        endDate: a.endDate || '2026-12-31',
        status: isActive,
        timestamp: a.startDate || new Date().toISOString(),
        firstName,
        lastName,
        email,
        userFullName: fullName,
      };

      return {
        id: a.benchAllocationId || a.id,
        userId: empId,
        employeeId: empId,
        firstName,
        lastName,
        email,
        userFullName: fullName,
        roleName: a.roleName || 'Developer',
        roleId: a.roleId || 1,
        roleType: a.roleType || '',
        designationName: a.designationName || 'Software Engineer',
        allocationPercent: percentage,
        percentage,
        startDate: a.startDate || '2026-01-01',
        endDate: a.endDate || '2026-12-31',
        status: isActive,
        projectId: Number(projectId),
        allocations: [record],
        deallocations: !isActive ? [record] : [],
        history: [record],
      };
    });
  } catch (err) {
    console.error('Failed to get project allocation history:', err);
    return [];
  }
};

export const getProjectAllocationHistoryByRole = async (projectId: number, roleId?: string): Promise<any[]> => {
  let list = await getProjectAllocationHistory(projectId);
  if (roleId && roleId !== 'all') {
    list = list.filter((item: any) => String(item.roleId) === String(roleId));
  }
  return list;
};

export const createProjectAllocationRecord = async (data: any): Promise<any> => {
  const result = await postProjectAllocations({
    employeeId: data.employeeId || data.userId,
    projectId: data.projectId,
    roleId: data.roleId,
    allocationPercent: data.percentage || data.allocationPercent || 50,
    startDate: data.startDate,
    endDate: data.endDate,
  });
  return result;
};

export const updateProjectAllocationRecord = async (id: number | string, data: any): Promise<any> => {
  const result = await updateProjectAllocation(id, {
    employeeId: data.employeeId || data.userId,
    projectId: data.projectId,
    roleId: data.roleId,
    allocationPercent: data.percentage || data.allocationPercent || 50,
    startDate: data.startDate,
    endDate: data.endDate,
  });
  return result;
};

export const deallocateProjectAllocationRecord = async (id: number | string): Promise<any> => {
  const result = await deleteProjectAllocation(id, true);
  return result;
};

export const deleteProjectAllocationRecord = async (id: number | string): Promise<any> => {
  return await deleteProjectAllocation(id, true);
};
