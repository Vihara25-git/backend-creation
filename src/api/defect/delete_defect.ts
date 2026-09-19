import apiClient from "../../lib/api";

export const deleteDefectById = async (id: string | number) => {
  const response = await apiClient.delete(`/api/v1/defect/delete/${id}`);
  return {
    status: 'Success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Defect deleted successfully',
  };
};

export const deleteDefect = deleteDefectById;