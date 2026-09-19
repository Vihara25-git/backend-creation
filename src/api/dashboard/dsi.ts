import apiClient from "../../lib/api";

export interface DsiResponse {
  status: string;
  statusCode: number;
  data: {
    projectId: number;
    defectSeverityIndex: number;
    dsiPercentage?: number;
    status: string;
    dsiStatus?: string;
    totalDefects: number;
    criticalDefects: number;
    highDefects: number;
    mediumDefects: number;
    lowDefects: number;
    riskLevel?: string;
  };
}

export async function getDefectSeverityIndex(projectId: string | number): Promise<DsiResponse> {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/dashboard/severity-index`);
    const resData = response.data?.data || response.data;

    const indexVal = Number(resData?.defectSeverityIndex ?? 0);
    const riskLevel = resData?.riskLevel || "Low Risk";

    let dsiStatus = "Healthy";
    if (indexVal >= 3.5 || riskLevel === "Critical") {
      dsiStatus = "Critical";
    } else if (indexVal >= 2.5 || riskLevel === "High Risk") {
      dsiStatus = "High Risk";
    } else if (indexVal >= 1.5 || riskLevel === "Medium Risk") {
      dsiStatus = "Needs Attention";
    }

    return {
      status: "success",
      statusCode: response.status || 200,
      data: {
        projectId: Number(projectId),
        defectSeverityIndex: indexVal,
        dsiPercentage: indexVal,
        status: riskLevel,
        dsiStatus,
        totalDefects: Number(resData?.totalDefects ?? 0),
        criticalDefects: Number(resData?.criticalDefects ?? 0),
        highDefects: Number(resData?.highDefects ?? 0),
        mediumDefects: Number(resData?.mediumDefects ?? 0),
        lowDefects: Number(resData?.lowDefects ?? 0),
        riskLevel,
      },
    };
  } catch (error) {
    console.error("Failed to fetch defect severity index:", error);
    return {
      status: "error",
      statusCode: 500,
      data: {
        projectId: Number(projectId),
        defectSeverityIndex: 0,
        dsiPercentage: 0,
        status: "Low Risk",
        dsiStatus: "Healthy",
        totalDefects: 0,
        criticalDefects: 0,
        highDefects: 0,
        mediumDefects: 0,
        lowDefects: 0,
        riskLevel: "Low Risk",
      },
    };
  }
}