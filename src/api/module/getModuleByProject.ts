import { getModulesByProjectId, CreateReleaseResponse } from './getModule';

export const getModulesByProject = async (projectId: number): Promise<CreateReleaseResponse> => {
  return getModulesByProjectId(projectId);
};