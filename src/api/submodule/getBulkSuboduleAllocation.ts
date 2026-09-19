import apiClient from "../../lib/api";

export const getBulkSuboduleAllocation = async (
  projectId: number,
  moduleId: number,
  submoduleId: number
) => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/sub-module/${submoduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((a: any) => ({
      id: a.submoduleDevId || a.id,
      employeeId: a.employeeId || a.employee?.empId || a.empId,
      employeeName: a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`),
      projectId,
      moduleId,
      submoduleId,
      role: 'Developer',
    }));
  } catch {
    return [];
  }
};