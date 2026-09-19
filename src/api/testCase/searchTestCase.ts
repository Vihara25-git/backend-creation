import apiClient from "../../lib/api";

export const searchTestCaseByCriteria = async (
  moduleId: number,
  description?: string,
  defectTypeId?: number,
  severityId?: number
) => {
  try {
    const response = await apiClient.get(`/api/v1/module/${moduleId}/test-case`);
    const resData = response.data?.data || response.data;
    let testCases = Array.isArray(resData) ? resData : [];

    if (description) {
      const term = description.toLowerCase();
      testCases = testCases.filter((t: any) => (t.description || '').toLowerCase().includes(term));
    }
    if (defectTypeId) {
      testCases = testCases.filter((t: any) => Number(t.defectTypeId) === Number(defectTypeId));
    }
    if (severityId) {
      testCases = testCases.filter((t: any) => Number(t.severityId) === Number(severityId));
    }
    return testCases;
  } catch (err) {
    console.error('Failed to search test cases:', err);
    return [];
  }
};