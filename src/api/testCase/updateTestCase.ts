import apiClient from "../../lib/api";

export async function updateTestCase(
  subModuleId: number,
  testCaseId: string | number,
  data: any
) {
  const projectId = data.projectId || 1;
  const moduleId = data.moduleId || 1;
  const body = {
    testCaseName: data.testCaseName || data.description?.slice(0, 50) || 'Test Case',
    description: data.description,
    testSteps: data.detailsSteps || data.steps || 'Test steps',
    defectTypeId: Number(data.defectTypeId),
    projectId: Number(projectId),
    moduleId: Number(moduleId),
    subModuleId: Number(subModuleId),
    severityId: Number(data.severityId),
  };

  const url = (projectId && moduleId && subModuleId)
    ? `/api/v1/project/${projectId}/module/${moduleId}/submodule/${subModuleId}/testcase/${testCaseId}`
    : `/api/v1/project/${projectId}/testcase/${testCaseId}`;

  const response = await apiClient.put(url, body);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Test case updated successfully',
    statusMessage: response.data?.statusMessage || 'Test case updated successfully',
    data: response.data?.data || response.data,
  };
}
