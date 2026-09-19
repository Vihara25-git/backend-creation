import apiClient from "../../lib/api";

export const getReleaseTestCaseCount = async (releaseId: string | number) => {
  const relId = Number(releaseId);
  try {
    const response = await apiClient.get(`/api/v1/release-test-cases/count?releaseId=${relId}`);
    const count = Number(response.data?.data ?? response.data ?? 0);

    return {
      status: 'success',
      statusCode: 200,
      data: {
        releaseId: relId,
        totalTestCases: count,
        testCaseCount: count,
        passedTestCases: 0,
        failedTestCases: 0,
        blockedTestCases: 0,
        unexecutedTestCases: count,
      },
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: {
        releaseId: relId,
        totalTestCases: 0,
        testCaseCount: 0,
        passedTestCases: 0,
        failedTestCases: 0,
        blockedTestCases: 0,
        unexecutedTestCases: 0,
      },
    };
  }
};

export const getReleaseTestCaseCounts = async (projectId?: string | number) => {
  try {
    const url = projectId ? `/api/v1/ReleaseView/project/${projectId}` : `/api/v1/ReleaseView`;
    const response = await apiClient.get(url);
    const resData = response.data?.data || response.data;
    const releases = Array.isArray(resData) ? resData : [];

    const data = await Promise.all(
      releases.map(async (r: any) => {
        const id = r.releaseId || r.id;
        let count = 0;
        try {
          const cRes = await apiClient.get(`/api/v1/release-test-cases/count?releaseId=${id}`);
          count = Number(cRes.data?.data ?? cRes.data ?? 0);
        } catch {
          count = 0;
        }

        return {
          id,
          releaseId: id,
          releaseName: r.releaseName || r.name,
          testCaseCount: count,
          totalTestCases: count,
          passedTestCases: 0,
          failedTestCases: 0,
          blockedTestCases: 0,
          unexecutedTestCases: count,
        };
      })
    );

    return {
      status: 'success',
      statusCode: 200,
      data,
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: [],
    };
  }
};