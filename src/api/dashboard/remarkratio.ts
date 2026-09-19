import apiClient from "../../lib/api";

export interface DefectRemarkRatioResponse {
  data: {
    ratio: string;
    category: string;
    color: string;
    totalRemark?: number;
    duplicateCount?: number;
    rejectCount?: number;
    totalDefects?: number;
    error?: string;
  };
}

export async function getDefectRemarkRatioByProjectId(
  projectId: string | number
): Promise<DefectRemarkRatioResponse> {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/dashboard/remark-ratio`);
    const resData = response.data?.data || response.data;

    const ratioVal = Number(resData?.ratio ?? 0);
    const totalRemark = Number(resData?.totalRemark ?? 0);

    let category = "Low";
    let color = "green";

    if (ratioVal >= 50) {
      category = "High";
      color = "red";
    } else if (ratioVal >= 25) {
      category = "Medium";
      color = "yellow";
    }

    return {
      data: {
        ratio: totalRemark > 0 ? `${ratioVal.toFixed(1)}%` : "0.0%",
        category,
        color,
        totalRemark,
        duplicateCount: Number(resData?.duplicateCount ?? 0),
        rejectCount: Number(resData?.rejectCount ?? 0),
        totalDefects: Number(resData?.totalDefects ?? 0),
      },
    };
  } catch (error) {
    console.error("Failed to fetch defect to remark ratio:", error);
    return {
      data: {
        ratio: "0.0%",
        category: "Low",
        color: "green",
      },
    };
  }
}