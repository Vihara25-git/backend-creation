import { Employee } from '../../types/index';
import apiClient from '../../lib/api';

export interface BenchSearchParams {
  startDate?: string;
  endDate?: string;
  designation?: string;
  firstName?: string;
  lastName?: string;
  availability?: number;
}

export const searchBenchEmployees = async (params: BenchSearchParams): Promise<Employee[]> => {
  try {
    const q = new URLSearchParams();
    q.append('page', '0');
    q.append('size', '1000');
    if (params.firstName) q.append('search', params.firstName);
    else if (params.lastName) q.append('search', params.lastName);
    if (params.designation) q.append('designationName', params.designation);
    if (params.availability !== undefined) q.append('availablePercentage', String(params.availability));
    if (params.startDate) q.append('startDate', params.startDate);
    if (params.endDate) q.append('endDate', params.endDate);

    const hasFilter = params.firstName || params.lastName || params.designation || params.availability !== undefined || params.startDate || params.endDate;
    const url = hasFilter ? `/api/v1/bench-availability-view/filter?${q.toString()}` : `/api/v1/bench-availability-view?${q.toString()}`;

    const response = await apiClient.get(url);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return items.map((u: any) => ({
      id: String(u.empId || u.id),
      firstName: u.firstName || u.employeeName?.split(' ')[0] || 'Employee',
      lastName: u.lastName || u.employeeName?.split(' ').slice(1).join(' ') || '',
      gender: (u.gender || u.userGender || 'Male') as any,
      email: u.email || `${(u.firstName || 'user').toLowerCase()}@sgic.com`,
      phone: u.contactNo || u.phone || '',
      designation: u.designationName || 'Software Engineer',
      experience: u.experience || 2,
      joinedDate: u.joinedDate || '2023-01-01',
      skills: Array.isArray(u.skills) ? u.skills : [],
      currentProjects: Array.isArray(u.currentProjects) ? u.currentProjects : [],
      availability: u.availablePercentage ?? u.availabilityPercent ?? 100,
      status: 'active',
      createdAt: u.createdAt || new Date().toISOString(),
      updatedAt: u.updatedAt || new Date().toISOString(),
    }));
  } catch (err) {
    console.error('Failed to search bench employees:', err);
    return [];
  }
};

export const searchByStartDate = async (startDate: string) => searchBenchEmployees({ startDate });
export const searchByDesignation = async (designation: string) => searchBenchEmployees({ designation });
export const searchByFirstName = async (firstName: string) => searchBenchEmployees({ firstName });
export const searchByAvailability = async (availability: number) => searchBenchEmployees({ availability });
