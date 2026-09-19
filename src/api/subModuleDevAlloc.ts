import apiClient from "../lib/api";

export interface SubModuleDevResponse {
  status: string;
  statusCode: number | string;
  statusMessage: string;
  data: SubModuleDevAllocation[];
}

export interface SubModuleDevAllocation {
  id: number | string;
  employeeId: number | string;
  submoduleId: number | string;
  employeeName?: string;
}

export const getAllSubmoduleAllocatedDevBySubmoduleId = async (
  subModuleId: number,
  moduleId: number = 0
): Promise<SubModuleDevResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/sub-module/${subModuleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      statusMessage: 'Success',
      data: items.map((a: any) => ({
        id: a.submoduleDevId || a.id,
        employeeId: a.employeeId || a.employee?.empId || a.empId,
        submoduleId: a.submoduleId || subModuleId,
        employeeName: a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`),
      })),
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusCode: err.response?.status || 500,
      statusMessage: err.message || 'Failed to fetch submodule developers',
      data: [],
    };
  }
};

export const allocateProjectEmployeeToSubModule = async (
  subModuleId: number,
  employeeId: number,
  moduleId: number = 0
): Promise<SubModuleDevResponse> => {
  const response = await apiClient.post(`/api/v1/module/${moduleId}/sub-module/${subModuleId}/employee`, {
    employeeId,
  });
  const resData = response.data?.data || response.data;

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: 'Developer allocated to submodule successfully',
    data: resData ? [{
      id: resData.submoduleDevId || resData.id,
      employeeId: resData.employeeId || employeeId,
      submoduleId: subModuleId,
      employeeName: resData.employeeName || `Employee ${employeeId}`,
    }] : [],
  };
};

export const deAllocateProjectEmployeeFromSubModule = async (
  subModuleId: number,
  employeeId: number,
  moduleId: number = 0
): Promise<SubModuleDevResponse> => {
  const response = await apiClient.delete(`/api/v1/module/${moduleId}/sub-module/${subModuleId}/employee/${employeeId}`);

  return {
    status: 'success',
    statusCode: response.status || 200,
    statusMessage: 'Developer deallocated from submodule successfully',
    data: [],
  };
};
