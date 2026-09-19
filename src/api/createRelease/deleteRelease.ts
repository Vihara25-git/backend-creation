import apiClient from "../../lib/api";

export async function deleteReleaseById(id: number) {
  const response = await apiClient.delete(`/api/v1/ReleaseView/delete/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Release deleted successfully',
  };
}

export const deleteRelease = deleteReleaseById;