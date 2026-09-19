import { CreateModuleRequest, CreateModuleResponse } from "../../types/index";
import apiClient from "../../lib/api";

export const createModule = async (data: CreateModuleRequest): Promise<CreateModuleResponse & { success: boolean }> => {
  const response = await apiClient.post(`/api/v1/project/${data.projectId}/module`, {
    moduleName: data.name,
  });
  const resData = response.data?.data || response.data;
  const mapped = {
    id: resData?.moduleId || resData?.id,
    name: resData?.moduleName || data.name,
    projectId: data.projectId,
  };

  return {
    status: "Created",
    statusCode: "201",
    success: true,
    message: response.data?.statusMessage || "Module created successfully",
    data: [mapped as any],
  };
};

export const createSubmodule = async (data: { subModuleName: string; moduleId: number }) => {
  const response = await apiClient.post(`/api/v1/module/${data.moduleId}/sub-module`, {
    subModuleName: data.subModuleName,
    moduleId: data.moduleId,
  });
  const resData = response.data?.data || response.data;

  return {
    status: "success",
    success: true,
    message: response.data?.statusMessage || "Submodule created successfully",
    data: {
      id: resData?.subModuleId || resData?.id,
      subModuleId: resData?.subModuleId || resData?.id,
      name: resData?.subModuleName || data.subModuleName,
      subModuleName: resData?.subModuleName || data.subModuleName,
      moduleId: data.moduleId,
    },
  };
};
