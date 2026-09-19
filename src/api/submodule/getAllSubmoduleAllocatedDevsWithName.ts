import apiClient from "../../lib/api";

export interface SubDevWithName {
  id: number;
  employeeId: number;
  submoduleId: number;
  employeeName?: string;
}

export interface SubDevWithNameResponse {
  status: number;
  statusCode: string;
  statusMessage: string;
  data: SubDevWithName[];
}

export const getAllSubDevwithName = async (
  submoduleId: number,
  moduleId: number = 0
): Promise<SubDevWithNameResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/sub-module/${submoduleId}/employee`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 200,
      statusCode: '200',
      statusMessage: 'Success',
      data: items.map((a: any) => ({
        id: a.submoduleDevId || a.id,
        employeeId: a.employeeId || a.employee?.empId || a.empId,
        submoduleId: a.submoduleId || submoduleId,
        employeeName: a.employeeName || (a.employee ? `${a.employee.firstName || ''} ${a.employee.lastName || ''}`.trim() : `Employee ${a.employeeId}`),
      })),
    };
  } catch (err: any) {
    return {
      status: 200,
      statusCode: '200',
      statusMessage: 'Success',
      data: [],
    };
  }
};

export default getAllSubDevwithName;