import apiClient from "../../lib/api";

export interface DefectHistoryEntry {
  id: number;
  defectId: number;
  assignedByName: string;
  assignedToName: string;
  previousStatus: string;
  defectStatus: string;
  name: string;
  defectDate: string;
  defectTime: string;
  createdBy: string;
  updatedBy: string;
}

export async function getDefectHistoryByDefectId(
  defectId: string | number
): Promise<DefectHistoryEntry[]> {
  try {
    const response = await apiClient.get(`/api/v1/defect/${defectId}/history`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((h: any, idx: number) => ({
      id: h.historyId || h.id || idx + 1,
      defectId: Number(defectId),
      assignedByName: h.assignedByName || 'QA Tester',
      assignedToName: h.assignedToName || 'Unassigned',
      previousStatus: h.previousStatus || 'New',
      defectStatus: h.currentStatus || h.status || 'Updated',
      name: h.comment || `Status updated`,
      defectDate: h.createdAt ? String(h.createdAt).split('T')[0] : '',
      defectTime: h.createdAt ? String(h.createdAt).split('T')[1]?.substring(0, 5) : '',
      createdBy: h.createdBy || 'User',
      updatedBy: h.updatedBy || 'User',
    }));
  } catch {
    return [];
  }
}

export const getDefectHistory = getDefectHistoryByDefectId;
