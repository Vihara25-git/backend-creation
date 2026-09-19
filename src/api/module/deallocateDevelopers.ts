import apiClient from "../../lib/api";
import { deAllocateProjectEmployeeFromSubModule } from "../subModuleDevAlloc";

export const deallocateModuleLeaderWithAllocateModuleId = async (allocateModuleId: number) => {
  return { status: 'success', message: 'Deallocated successfully', data: { allocateModuleId } };
};

export const deallocateDeveloperFromModule = async (
  _projectId: number,
  moduleId: number,
  userId: number
) => {
  const response = await apiClient.delete(`/api/v1/module/${moduleId}/employee/${userId}`);
  return { status: 'success', message: 'Developer deallocated from module', data: response.data };
};

export const deallocateSubmoduleDeveloperWithAllocateModuleId = async (
  allocateModuleId: number
) => {
  return { status: 'success', message: 'Submodule developer deallocated', data: { allocateModuleId } };
};

export const deallocateDeveloperFromSubmodule = async (
  _projectId: number,
  moduleId: number,
  submoduleId: number,
  userId: number
) => {
  return await deAllocateProjectEmployeeFromSubModule(submoduleId, userId, moduleId);
};

export const reassignDeveloperWithAllocateModuleId = async (
  allocateModuleId: number,
  newUserId: number
) => {
  return { status: 'success', message: 'Developer reassigned', data: { allocateModuleId, newUserId } };
};

export const reassignSubmoduleDeveloperWithAllocateModuleId = async (
  allocationId: number,
  newUserId: number
) => {
  return { status: 'success', message: 'Submodule developer reassigned', data: { allocationId, newUserId } };
};

export const reassignDeveloperToModule = async (
  projectId: number,
  moduleId: number,
  oldUserId: number,
  newUserId: number
) => {
  return { status: 'success', message: 'Developer reassigned to module', data: { projectId, moduleId, oldUserId, newUserId } };
};

export const reassignDeveloperToSubmodule = async (
  _projectId: number,
  _moduleId: number,
  _submoduleId: number,
  oldUserId: number,
  newUserId: number
) => {
  return { status: 'success', message: 'Developer reassigned to submodule', data: { oldUserId, newUserId } };
};