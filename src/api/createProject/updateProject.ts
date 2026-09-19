import { ProjectFormData } from '../../types';
import { updateProject as updateProj } from '../projectget';

export async function updateProject(id: string | number, projectData: ProjectFormData) {
  return updateProj(id, projectData);
}