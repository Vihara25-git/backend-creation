import { deleteProject as deleteProj } from '../projectget';

export const deleteProject = async (projectId: string | number): Promise<any> => {
  return deleteProj(projectId);
};