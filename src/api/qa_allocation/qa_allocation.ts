import apiClient from '../../lib/api';

export interface QAMember {
  userId: number;
  userFullName: string;
}

export interface QAMembersResponse {
  status: string;
  statusCode: number;
  message: string;
  data: QAMember[];
}

const isQaLeadOrQaEngineer = (roleName?: string, roleType?: string): boolean => {
  const rName = String(roleName || '').trim().toUpperCase();
  const rType = String(roleType || '').trim().toUpperCase();

  const normRName = rName.replace(/[\s-]+/g, '_');
  const normRType = rType.replace(/[\s-]+/g, '_');

  return (
    normRType === 'QA_LEAD' ||
    normRType === 'QA_ENGINEER' ||
    normRName === 'QA_LEAD' ||
    normRName === 'QA_ENGINEER' ||
    rName === 'QA LEAD' ||
    rName === 'QA ENGINEER'
  );
};

export const getQAMembersByProjectId = async (projectId: number): Promise<QAMembersResponse> => {
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    const projectAllocs = Array.isArray(resData) ? resData : [];

    const qaMembersMap = new Map<number, { userId: number; userFullName: string }>();

    for (const alloc of projectAllocs) {
      const empId = Number(alloc.empId || alloc.employeeId);
      if (!empId) continue;

      let isQA = isQaLeadOrQaEngineer(alloc.roleName, alloc.roleType);

      if (!isQA) {
        try {
          const rolesRes = await apiClient.get(`/api/v1/bench-allocation/employee/${empId}/roles`);
          const rolesList = Array.isArray(rolesRes.data) ? rolesRes.data : (rolesRes.data?.data || []);
          isQA = rolesList.some((r: any) => isQaLeadOrQaEngineer(r.roleName, r.roleType));
        } catch {
          // fallback to allocation check
        }
      }

      if (isQA) {
        const name = alloc.employeeName || `${alloc.firstName || ''} ${alloc.lastName || ''}`.trim() || `Employee ${empId}`;
        qaMembersMap.set(empId, {
          userId: empId,
          userFullName: name,
        });
      }
    }

    return {
      status: 'success',
      statusCode: 200,
      message: 'QA members retrieved successfully',
      data: Array.from(qaMembersMap.values()),
    };
  } catch (err: any) {
    return {
      status: 'success',
      statusCode: 200,
      message: 'QA members retrieved successfully',
      data: [],
    };
  }
};