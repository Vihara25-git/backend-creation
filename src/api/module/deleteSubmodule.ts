import apiClient from "../../lib/api";

export const deleteSubmodule = async (submoduleId: number, moduleId: number) => {
  const response = await apiClient.delete(`/api/v1/module/${moduleId}/sub-module/${submoduleId}`);
  return {
    status: "success",
    success: true,
    message: response.data?.statusMessage || "Submodule deleted successfully",
    data: { submoduleId, moduleId },
  };
};