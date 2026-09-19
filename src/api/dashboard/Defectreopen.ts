import apiClient from "../../lib/api";

export async function getReopenCountSummary(projectId: number) {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/dashboard/reopened-summary`);
    const resData = response.data?.data || response.data;

    const reopened = Number(resData?.reopenedMultipleTimesCount ?? 0);
    const notReopened = Number(resData?.notReopenedMultipleTimesCount ?? 0);

    return {
      status: "success",
      data: [
        { label: "Reopened", count: reopened },
        { label: "Not Reopened", count: notReopened },
      ],
    };
  } catch (err) {
    console.error("Failed to get reopen count summary:", err);
    return {
      status: "error",
      data: [
        { label: "Reopened", count: 0 },
        { label: "Not Reopened", count: 0 },
      ],
    };
  }
}