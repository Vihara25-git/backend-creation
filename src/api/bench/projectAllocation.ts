import apiClient from "../../lib/api";

interface AvailablePeriod {
  period: string;
  percentage: number;
  project: string;
  userId: number;
}

export interface ViewAllocationsResponse {
  data: {
    availablePeriods: AvailablePeriod[];
  };
  message: string;
  status: string;
  statusCode: number;
}

export interface ProjectAllocationPayload {
  employeeId: number;
  projectId: number;
  roleId: number;
  allocationPercent: number;
  startDate: string;
  endDate: string;
}

export async function postProjectAllocations(payload: ProjectAllocationPayload) {
  const body = {
    empId: Number(payload.employeeId),
    roleId: Number(payload.roleId),
    projectId: Number(payload.projectId),
    startDate: payload.startDate ? payload.startDate.split('T')[0] : null,
    endDate: payload.endDate ? payload.endDate.split('T')[0] : null,
    availability: Number(payload.allocationPercent),
  };

  const response = await apiClient.post('/api/v1/bench-allocation', body);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project allocation created successfully',
    data: response.data?.data || response.data,
  };
}

export async function getProjectAllocationsById(projectId: string | number) {
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      data: items.map((a: any) => ({
        id: a.benchAllocationId || a.id,
        benchAllocationId: a.benchAllocationId || a.id,
        employeeId: a.empId,
        empId: a.empId,
        userId: a.empId,
        employeeName: a.employeeName || `${a.firstName || ''} ${a.lastName || ''}`.trim(),
        userFullName: a.employeeName || `${a.firstName || ''} ${a.lastName || ''}`.trim(),
        firstName: a.firstName || a.employeeName?.split(' ')[0] || '',
        lastName: a.lastName || a.employeeName?.split(' ').slice(1).join(' ') || '',
        employeeEmail: a.employeeEmail || '',
        projectId: a.projectId,
        projectName: a.projectName,
        roleId: a.roleId,
        roleName: a.roleName,
        allocationPercent: a.availability,
        allocationPercentage: a.availability,
        availability: a.availability,
        startDate: a.startDate,
        endDate: a.endDate,
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return { status: 'success', statusCode: 200, data: [] };
    }
    throw err;
  }
}

export async function updateProjectAllocation(id: string | number, payload: any) {
  const body = {
    empId: Number(payload.employeeId || payload.empId),
    roleId: Number(payload.roleId),
    projectId: Number(payload.projectId),
    startDate: payload.startDate ? String(payload.startDate).split('T')[0] : null,
    endDate: payload.endDate ? String(payload.endDate).split('T')[0] : null,
    availability: Number(payload.allocationPercent ?? payload.availability ?? 100),
  };

  const response = await apiClient.put(`/api/v1/bench-allocation/${id}`, body);
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project allocation updated successfully',
    data: response.data?.data || response.data,
  };
}

export async function deleteProjectAllocation(id: string | number, _forceDeallocate: boolean = false) {
  const response = await apiClient.patch(`/api/v1/bench-allocation/${id}/deallocate`, {});
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Project allocation removed successfully',
    data: { id },
  };
}

export async function filterProjectAllocations(projectId: string | number, filters: any) {
  try {
    const params = new URLSearchParams();
    if (filters?.name?.trim()) params.append('search', filters.name.trim());
    if (filters?.roleId) params.append('roleId', String(filters.roleId));
    if (filters?.startDate) params.append('startDateFrom', filters.startDate);
    if (filters?.endDate) params.append('startDateTo', filters.endDate);

    const response = await apiClient.get(`/api/v1/bench-allocation/filter/${projectId}?${params.toString()}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusCode: 200,
      data: items,
    };
  } catch {
    return { status: 'success', statusCode: 200, data: [] };
  }
}

export async function getMaxAvailablePercentage(userId: string | number, _startDate?: string, _endDate?: string) {
  try {
    const response = await apiClient.get(`/api/v1/bench-availability-view/employee/${userId}`);
    const data = response.data?.data || response.data;
    return { data: data?.availablePercentage ?? 100 };
  } catch {
    return { data: 100 };
  }
}

export async function getDevelopersWithRolesByProjectId(projectId: number | string | undefined) {
  if (!projectId) return { status: 'success', statusCode: 200, data: [] };
  try {
    const response = await apiClient.get(`/api/v1/bench-allocation/${projectId}/project`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return {
      status: 'success',
      statusCode: 200,
      data: items.map((a: any) => ({
        id: a.benchAllocationId || a.id,
        projectAllocationId: a.benchAllocationId || a.id,
        employeeId: a.empId,
        userId: a.empId,
        name: a.employeeName || `${a.firstName || ''} ${a.lastName || ''}`.trim(),
        email: a.employeeEmail || '',
        role: a.roleName || 'Developer',
        roleId: a.roleId || 4,
      })),
    };
  } catch {
    return { status: 'success', statusCode: 200, data: [] };
  }
}

export async function allocateDeveloperToModule(moduleId: number, projectAllocationId: number) {
  const response = await apiClient.post(`/api/v1/module/${moduleId}/employee/${projectAllocationId}`);
  return {
    status: 'success',
    statusCode: 200,
    message: response.data?.statusMessage || 'Developer allocated to module successfully',
  };
}

export async function allocateDeveloperToSubModule(moduleId: number, projectAllocationId: number, id: number) {
  const response = await apiClient.post(`/api/v1/module/${moduleId}/sub-module/${id}/employee`, {
    employeeId: projectAllocationId,
  });
  return {
    status: 'success',
    statusCode: 200,
    message: response.data?.statusMessage || 'Developer allocated to submodule successfully',
  };
}

export async function getViewAllocations(userId: string | number): Promise<ViewAllocationsResponse> {
  try {
    const response = await apiClient.get('/api/v1/bench-allocation?page=0&size=1000');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);
    const employeeAllocations = items.filter((a: any) => String(a.empId) === String(userId));

    if (employeeAllocations.length > 0) {
      const availablePeriods = employeeAllocations.map((a: any) => {
        const s = a.startDate ? String(a.startDate).split('T')[0] : '';
        const e = a.endDate ? String(a.endDate).split('T')[0] : '';
        const period = s && e ? `${s} - ${e}` : (s || e || 'Ongoing');
        return {
          project: a.projectName || `Project ${a.projectId}`,
          period,
          percentage: Number(a.availability ?? 0),
          userId: Number(userId),
        };
      });

      return {
        data: { availablePeriods },
        message: 'Success',
        status: 'success',
        statusCode: 200,
      };
    }

    // Fallback: check bench-availability-view for this employee
    const viewResponse = await apiClient.get(`/api/v1/bench-availability-view/employee/${userId}`);
    const viewData = viewResponse.data?.data || viewResponse.data;
    const periods: AvailablePeriod[] = [];
    if (viewData?.currentProjects && viewData.totalAllocatedPercentage > 0) {
      const pNames = String(viewData.currentProjects).split(',').map((p: string) => p.trim());
      const periodStr = viewData.availablePeriod ? String(viewData.availablePeriod).split('T')[0] : '';
      pNames.forEach((name: string) => {
        periods.push({
          project: name,
          period: periodStr ? `Until ${periodStr}` : 'Active',
          percentage: Number(viewData.totalAllocatedPercentage),
          userId: Number(userId),
        });
      });
    }

    return {
      data: { availablePeriods: periods },
      message: 'Success',
      status: 'success',
      statusCode: 200,
    };
  } catch {
    return {
      data: { availablePeriods: [] },
      message: 'Error fetching allocations',
      status: 'error',
      statusCode: 500,
    };
  }
}