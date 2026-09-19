import apiClient from '../../lib/api';

export async function getTestCasesByFilter({
  projectId,
  releaseId,
  moduleId,
  subModuleId,
}: {
  projectId: number;
  releaseId: number;
  moduleId?: number;
  subModuleId?: number;
}) {
  try {
    const params = new URLSearchParams();
    if (moduleId) params.append('moduleId', String(moduleId));
    if (subModuleId) params.append('subModuleId', String(subModuleId));

    const query = params.toString() ? `?${params.toString()}` : '';
    const response = await apiClient.get(`/api/v1/release-test-cases/release/${releaseId}/test-case${query}`);
    const resData = response.data?.data || response.data;
    const testCases = Array.isArray(resData) ? resData : [];

    return testCases.map((t: any) => {
      const rawStatus = (t.passOrFail || t.status || '').toUpperCase();
      let normalizedStatus: string = 'not-started';
      if (rawStatus === 'PASSED' || rawStatus === 'PASS') {
        normalizedStatus = 'passed';
      } else if (rawStatus === 'FAILED' || rawStatus === 'FAIL') {
        normalizedStatus = 'failed';
      } else if (rawStatus === 'BLOCKED') {
        normalizedStatus = 'blocked';
      } else if (rawStatus === 'IN_PROGRESS' || rawStatus === 'IN-PROGRESS') {
        normalizedStatus = 'in-progress';
      }

      const formattedDefectId = t.defectId
        ? (String(t.defectId).startsWith('DEF') ? String(t.defectId) : `DEF-${t.defectId}`)
        : null;

      return {
        id: t.releaseTestCaseId || t.id,
        backendId: t.releaseTestCaseId || t.id,
        releaseTestCaseId: t.releaseTestCaseId || t.id,
        testcaseNo: t.testCaseName || (t.testCaseId ? `TC-${t.testCaseId}` : `TC-${t.id}`),
        testCaseId: t.testCaseName || (t.testCaseId ? `TC-${t.testCaseId}` : `TC-${t.id}`),
        testCaseDbId: t.testCaseId || null,
        no: t.testCaseId ? String(t.testCaseId) : String(t.id),
        description: t.description || '',
        detailsSteps: t.testSteps || t.steps || '',
        steps: t.testSteps || t.steps || '',
        expectedResult: '',
        severityName: t.severityName || 'Medium',
        defectTypeName: t.defectTypeName || 'Functional Bug',
        subModuleId: t.subModuleId || null,
        submoduleId: t.subModuleId || null,
        moduleId: t.moduleId || null,
        subModuleName: t.subModuleName || '',
        moduleName: t.moduleName || '',
        projectId,
        releaseId,
        status: normalizedStatus,
        executionStatus: t.passOrFail || 'NOT_RUN',
        assignedTo: t.assignToName || t.employeeName || null,
        assignedToId: t.assignToId || t.employeeId || null,
        defectId: formattedDefectId,
        defectNo: formattedDefectId,
        rawDefectId: t.defectId || null,
        priorityName: t.priorityName || null,
      };
    });
  } catch (err: any) {
    console.error('Failed to get test cases by filter:', err);
    return [];
  }
}