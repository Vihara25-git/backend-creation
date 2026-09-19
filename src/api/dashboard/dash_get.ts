import apiClient from "../../lib/api";

export interface TimeToFindDefectsResponse {
  status: string;
  message?: string;
  data: {
    dayNumber: number;
    totalDefects: number;
    periodStart?: string;
    periodEnd?: string;
  }[];
  statusCode?: number;
}

export interface TimeToFixDefectsResponse {
  projectId: number;
  releaseName: string;
  dailyData: {
    label: string;
    dayNumber: number;
    defectFixedCount: number;
    timeRange: string;
  }[];
  data?: {
    label: string;
    dayNumber: number;
    defectFixedCount: number;
    timeRange: string;
  }[];
}

export async function getDefectSeveritySummary(projectId: string) {
  try {
    const response = await apiClient.get(`/api/v1/project/${projectId}/defect/severity-breakdown`);
    const resData = response.data?.data || response.data;
    return {
      status: 'success',
      statusCode: response.status || 200,
      data: resData,
    };
  } catch (error: any) {
    console.error("Failed to fetch defect severity summary:", error);
    throw error;
  }
}

export async function getReleaseDefectsDaily(projectId: string, releaseId?: string): Promise<TimeToFindDefectsResponse> {
  try {
    const url = releaseId
      ? `/api/v1/project/${projectId}/release/${releaseId}/dashboard/time-to-find`
      : `/api/v1/project/${projectId}/dashboard/time-to-find`;

    const response = await apiClient.get(url);
    const rawData = Array.isArray(response.data) ? response.data : (response.data?.data || []);

    const mapped = rawData.map((item: any) => {
      const dayNum = typeof item.dayNumber === 'number'
        ? item.dayNumber
        : parseInt(String(item.day || '').replace(/\D/g, '') || '1', 10);

      return {
        dayNumber: dayNum,
        totalDefects: Number(item.count ?? item.totalDefects ?? 0),
        periodStart: item.periodStart || '',
        periodEnd: item.periodEnd || '',
      };
    });

    return {
      status: "success",
      statusCode: 200,
      message: "Retrieved successfully",
      data: mapped,
    };
  } catch (error) {
    console.error("Failed to fetch time to find defects:", error);
    return {
      status: "error",
      statusCode: 500,
      message: "Failed to retrieve time to find defects",
      data: [],
    };
  }
}

export async function getTimeToFixDefectsDaily(projectId: number, releaseId?: number): Promise<TimeToFixDefectsResponse> {
  try {
    const url = releaseId
      ? `/api/v1/project/${projectId}/release/${releaseId}/dashboard/time-to-fixed`
      : `/api/v1/project/${projectId}/dashboard/time-to-fixed`;

    const response = await apiClient.get(url);
    const rawData = Array.isArray(response.data) ? response.data : (response.data?.data || []);

    const mapped = rawData.map((item: any) => {
      const dayNum = typeof item.dayNumber === 'number'
        ? item.dayNumber
        : parseInt(String(item.day || '').replace(/\D/g, '') || '1', 10);

      return {
        label: item.day || item.label || `Day ${dayNum}`,
        dayNumber: dayNum,
        defectFixedCount: Number(item.count ?? item.defectFixedCount ?? 0),
        timeRange: item.timeRange || '24h',
      };
    });

    return {
      projectId,
      releaseName: '',
      dailyData: mapped,
      data: mapped,
    };
  } catch (error) {
    console.error("Failed to fetch time to fix defects:", error);
    return {
      projectId,
      releaseName: '',
      dailyData: [],
      data: [],
    };
  }
}
