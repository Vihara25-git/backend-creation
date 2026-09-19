import { ProjectFormData } from '../../types';
import { createProject as createProj } from '../projectget';

export async function createProject(projectData: ProjectFormData) {
  return createProj(projectData);
}
