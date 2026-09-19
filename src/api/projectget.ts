import { Project } from "../types";
import apiClient from "../lib/api";

function mapBackendProject(p: any): Project {
  return {
    id: p.projectId ?? p.id,
    projectId: p.projectId ?? p.id,
    name: p.projectName ?? p.name ?? '',
    projectName: p.projectName ?? p.name ?? '',
    description: p.projectDescription ?? p.description ?? '',
    startDate: p.startDate ?? '',
    endDate: p.endDate ?? '',
    status: p.status ?? 'ACTIVE',
    projectStatus: p.status ?? 'ACTIVE',
    userId: p.projectManagerId ?? 1,
    manager: p.projectManagerName ?? 'Unassigned',
    projectManagerName: p.projectManagerName ?? 'Unassigned',
    clientName: p.clientDetails?.clientName ?? '',
    clientEmail: p.clientDetails?.email ?? '',
    clientPhone: p.clientDetails?.phoneNumber ?? '',
    clientCountry: p.clientDetails?.country ?? '',
    clientState: p.clientDetails?.state ?? '',
  } as Project;
}

export const getAllProjects = async (): Promise<Project[]> => {
  try {
    const response = await apiClient.get('/api/v1/project');
    const data = response.data?.data || response.data;
    if (Array.isArray(data)) {
      return data.map(mapBackendProject);
    }
    return [];
  } catch (err: any) {
    if (err.response?.status === 404) return [];
    throw err;
  }
};

export const getAllProjectsForDashbord = async (): Promise<any> => {
  const projects = await getAllProjects();
  return {
    status: 'success',
    statusCode: 200,
    data: projects,
  };
};

function formatProjectPayload(projectData: any) {
  return {
    projectName: projectData.name || projectData.projectName,
    projectDescription: projectData.description || projectData.projectDescription || '',
    startDate: projectData.startDate ? projectData.startDate.split('T')[0] : new Date().toISOString().split('T')[0],
    endDate: projectData.endDate ? projectData.endDate.split('T')[0] : new Date().toISOString().split('T')[0],
    status: projectData.status || projectData.projectStatus || 'ACTIVE',
    projectManagerId: Number(projectData.userId || projectData.projectManagerId || 1),
    designationId: Number(projectData.designationId || 1),
    managerAllocation: Number(projectData.managerAllocation || 100),
    clientDetails: {
      clientName: projectData.clientName || 'Client',
      country: projectData.clientCountry || projectData.country || 'USA',
      state: projectData.clientState || projectData.state || 'California',
      phoneNumber: (projectData.clientPhone || projectData.phoneNo || '1234567890').replace(/\D/g, '').slice(0, 10).padEnd(10, '0'),
      email: (projectData.clientEmail || projectData.email || 'client@gmail.com').includes('@gmail.com') ? (projectData.clientEmail || projectData.email) : 'client@gmail.com',
    },
  };
}

export async function updateProject(id: number | string, projectData: any) {
  const payload = formatProjectPayload(projectData);
  const response = await apiClient.put(`/api/v1/project/${id}`, payload);
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project updated successfully',
    data: resData ? mapBackendProject(resData) : null,
  };
}

export async function deleteProject(id: string | number) {
  const response = await apiClient.delete(`/api/v1/project/${id}`);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project deleted successfully',
  };
}

export async function createProject(project: any) {
  const payload = formatProjectPayload(project);
  const response = await apiClient.post('/api/v1/project', payload);
  const resData = response.data?.data || response.data;
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project created successfully',
    data: resData ? mapBackendProject(resData) : null,
  };
}

export interface AvailableManager {
  employeeId: number;
  firstName: string;
  lastName: string;
  email: string;
  designationId: number;
  designationName: string;
  availabilityPercent: number;
  isActive: boolean;
}

export const getAvailableManagers = async (designationId?: number): Promise<AvailableManager[]> => {
  try {
    const url = designationId ? `/api/v1/Employee/designation/${designationId}` : '/api/v1/Employee/view/paged?page=0&size=1000';
    const response = await apiClient.get(url);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return items.map((u: any) => ({
      employeeId: u.empId || u.id,
      firstName: u.firstName || '',
      lastName: u.lastName || '',
      email: u.email || '',
      designationId: u.designationId || 1,
      designationName: u.designationName || 'Project Manager',
      availabilityPercent: u.availablePercentage ?? 100,
      isActive: u.isActive !== false,
    }));
  } catch (err: any) {
    if (err.response?.status === 404) return [];
    throw err;
  }
};

export const getAvailableManagersForUpdate = async (
  designationId?: number,
  _projectId?: number
): Promise<AvailableManager[]> => {
  return getAvailableManagers(designationId);
};
