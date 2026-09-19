import apiClient from "../../lib/api";

export async function updateRelease(id: number, data: any) {
  const body = {
    releaseName: data.name || data.releaseName,
    releaseVersion: data.version || data.releaseVersion || 'v1.0.0',
    releaseDate: data.releaseDate ? String(data.releaseDate).split('T')[0] : new Date().toISOString().split('T')[0],
    projectId: Number(data.projectId || data.project_id || 1),
    releaseTypeId: Number(data.releaseTypeId || data.releaseType_id || 1),
  };

  const response = await apiClient.put(`/api/v1/ReleaseView/update/${id}`, body);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Release updated successfully',
    data: response.data?.data || response.data,
  };
}
