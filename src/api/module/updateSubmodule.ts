import apiClient from "../../lib/api";

export const updateSubmodule = async (
  submoduleId: number,
  moduleId: number,
  data: { subModuleName: string }
) => {
  const response = await apiClient.put(`/api/v1/module/${moduleId}/sub-module/${submoduleId}`, {
    subModuleName: data.subModuleName,
    moduleId,
  });
  const resData = response.data?.data || response.data;

  return {
    status: "success",
    success: true,
    message: response.data?.statusMessage || "Submodule updated successfully",
    data: resData,
  };
};