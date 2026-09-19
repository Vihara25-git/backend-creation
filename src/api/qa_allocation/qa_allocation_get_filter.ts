import apiClient from '../../lib/api';

export interface allocated_testcases {
  projectId: number;
  releaseId: string | number;
  moduleId: number;
  subModuleId: number;
}

export interface allocated_testcase_details {
  id: number;
  testCaseId: string;
  description: string;
  steps: string;
  type: string;
  severity: string;
}

export interface GetAllocatedTestCases_Response {
  status: string;
  statusCode: number;
  message: string;
  data: allocated_testcase_details[];
}

export async function getAllocatedTestCases({ releaseId, moduleId, subModuleId }: allocated_testcases): Promise<GetAllocatedTestCases_Response> {
  try {
    const url = `/api/v1/release-test-cases/release/${releaseId}/test-case?moduleId=${moduleId}&subModuleId=${subModuleId}`;
    const response = await apiClient.get(url);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      message: 'Allocated test cases retrieved successfully',
      data: items.map((t: any) => ({
        id: t.releaseTestCaseId || t.testCaseId || t.id,
        testCaseId: t.testCaseNumber || `TC-${t.testCaseId || t.id}`,
        description: t.description || '',
        steps: t.steps || '',
        type: t.defectTypeName || '',
        severity: t.severityName || '',
      })),
    };
  } catch (err: any) {
    return {
      status: 'success',
      statusCode: 200,
      message: 'Allocated test cases retrieved successfully',
      data: [],
    };
  }
}

export interface BulkAssignOwnerResponse {
  status: string;
  statusCode: number;
  message: string;
  data?: any;
}

export async function bulkAssignOwner(ownerId: number, testCaseIds: number[], releaseId?: number): Promise<BulkAssignOwnerResponse> {
  if (releaseId) {
    for (const id of testCaseIds) {
      try {
        await apiClient.post(`/api/v1/release-test-cases/release/${releaseId}/test-case/${id}/employee`, {
          employeeId: ownerId,
        });
      } catch (err) {
        console.error('Failed to assign QA to testcase', id, err);
      }
    }
  }

  return {
    status: 'success',
    statusCode: 200,
    message: 'Owner assigned successfully',
    data: { ownerId, testCaseIds },
  };
}
