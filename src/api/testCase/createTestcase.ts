import apiClient from "../../lib/api";

export interface CreateTestCaseRequest {
  description: string;
  detailsSteps: string;
  expectedResult?: string;
  severityId: number;
  defectTypeId: number;
  projectId?: number;
  moduleId?: number;
  testCaseName?: string;
}

export interface CreateTestCaseResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: any;
}

export async function createTestCase(subModuleId: number, testCaseData: CreateTestCaseRequest) {
  const projectId = testCaseData.projectId || 1;
  const moduleId = testCaseData.moduleId || 1;
  const body = {
    testCaseName: testCaseData.testCaseName || testCaseData.description.slice(0, 50) || 'Test Case',
    description: testCaseData.description,
    testSteps: testCaseData.detailsSteps || 'Test steps',
    defectTypeId: Number(testCaseData.defectTypeId),
    projectId: Number(projectId),
    moduleId: Number(moduleId),
    subModuleId: Number(subModuleId),
    severityId: Number(testCaseData.severityId),
  };

  const response = await apiClient.post(`/api/v1/project/${projectId}/module/${moduleId}/submodule/${subModuleId}/testcase`, body);
  return {
    status: 'Created',
    statusCode: response.status || 201,
    message: response.data?.statusMessage || 'Test case created successfully',
    statusMessage: response.data?.statusMessage || 'Test case created successfully',
    data: response.data?.data || response.data,
  };
}

export const createTestCaseSub = async (subModuleId: number, payload: CreateTestCaseRequest): Promise<CreateTestCaseResponse> => {
  return createTestCase(subModuleId, payload);
};
