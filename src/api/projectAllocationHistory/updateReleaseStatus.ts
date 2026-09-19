import apiClient from "../../lib/api";

export interface UpdateReleaseStatusResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    id: number;
    name: string;
    status: string;
  };
}

export const updateReleaseStatus = async (releaseId: number, status: 'ACTIVE' | 'HOLD' | 'ON_HOLD'): Promise<UpdateReleaseStatusResponse> => {
  const normalizedStatus = status === 'HOLD' ? 'ON_HOLD' : status;

  await apiClient.patch(
    `/api/v1/release-test-cases/release/${releaseId}/status`,
    `"${normalizedStatus}"`,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  return {
    status: 'success',
    statusCode: 200,
    statusMessage: 'Release status updated successfully',
    data: {
      id: releaseId,
      name: `Release ${releaseId}`,
      status: normalizedStatus,
    },
  };
};