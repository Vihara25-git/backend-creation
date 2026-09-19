import apiClient from "../../lib/api";

export type ExecutionStatus =
  | "not-started"
  | "in-progress"
  | "passed"
  | "failed"
  | "blocked";

const EXECUTION_STATUS_KEY = "executionStatuses";

export function getExecutionStatuses(
  projectId: string | number,
  releaseId: string | number
): Record<string, ExecutionStatus> {
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    if (!raw) return {};
    const all: Record<string, any> = JSON.parse(raw);
    const proj = all[String(projectId)] || {};
    return (proj[String(releaseId)] || {}) as Record<string, ExecutionStatus>;
  } catch {
    return {};
  }
}

export function setExecutionStatus(
  projectId: string | number,
  releaseId: string | number,
  testCaseId: string | number,
  status: ExecutionStatus
): Record<string, ExecutionStatus> {
  let all: Record<string, any> = {};
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    all = raw ? JSON.parse(raw) : {};
  } catch {
    all = {};
  }

  const pid = String(projectId);
  const rid = String(releaseId);
  if (!all[pid]) all[pid] = {};
  if (!all[pid][rid]) all[pid][rid] = {};
  all[pid][rid][String(testCaseId)] = status;

  localStorage.setItem(EXECUTION_STATUS_KEY, JSON.stringify(all));
  return all[pid][rid] as Record<string, ExecutionStatus>;
}

export function setBulkExecutionStatuses(
  projectId: string | number,
  releaseId: string | number,
  statuses: Record<string, ExecutionStatus>
): void {
  let all: Record<string, any> = {};
  try {
    const raw = localStorage.getItem(EXECUTION_STATUS_KEY);
    all = raw ? JSON.parse(raw) : {};
  } catch {
    all = {};
  }

  const pid = String(projectId);
  const rid = String(releaseId);
  if (!all[pid]) all[pid] = {};
  all[pid][rid] = { ...(all[pid][rid] || {}), ...statuses };
  localStorage.setItem(EXECUTION_STATUS_KEY, JSON.stringify(all));
}

export const updateReleaseTestCaseStatus = async (
  releaseIdOrTestCaseId: number,
  releaseTestCaseIdOrPayload: any,
  payloadOrUndefined?: {
    status: "PASSED" | "FAILED";
    priorityId?: number;
    assignedTo?: number;
  }
): Promise<any> => {
  let releaseId: number;
  let releaseTestCaseId: number;
  let payload: {
    status: "PASSED" | "FAILED";
    priorityId?: number;
    assignedTo?: number;
  };

  if (payloadOrUndefined !== undefined) {
    releaseId = releaseIdOrTestCaseId;
    releaseTestCaseId = releaseTestCaseIdOrPayload;
    payload = payloadOrUndefined;
  } else {
    // Called as (releaseTestCaseId, payload)
    releaseTestCaseId = releaseIdOrTestCaseId;
    payload = releaseTestCaseIdOrPayload;
    releaseId = 0; // will be resolved if needed
  }

  const formData = new FormData();
  const rawStatus = (payload.status || '').toUpperCase();
  const normalizedStatus = (rawStatus === 'PASSED' || rawStatus === 'PASS') ? 'PASS' : 'FAIL';
  const requestData = {
    passOrFail: normalizedStatus,
    priorityId: payload.priorityId || null,
    assignedTo: payload.assignedTo || null,
  };

  formData.append(
    'data',
    new Blob([JSON.stringify(requestData)], { type: 'application/json' })
  );

  const response = await apiClient.patch(
    `/api/v1/release-test-cases/release/${releaseId}/test-case/${releaseTestCaseId}/status`,
    formData
  );

  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Test case status updated successfully',
    data: response.data?.data || response.data,
  };
};

export const updateReleaseTestCaseStatusWithImage = async (
  releaseId: number,
  releaseTestCaseId: number,
  formData: FormData
): Promise<any> => {
  const response = await apiClient.patch(
    `/api/v1/release-test-cases/release/${releaseId}/test-case/${releaseTestCaseId}/status`,
    formData
  );

  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Test case status updated successfully',
    data: response.data?.data || response.data,
  };
};

export const deleteReleaseTestCase = async (
  releaseId: number | string,
  releaseTestCaseId: number | string
): Promise<any> => {
  const response = await apiClient.delete(
    `/api/v1/release-test-cases/release/${releaseId}/test-case/${releaseTestCaseId}`
  );
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: 'Test case removed from release execution successfully',
  };
};