import apiClient from "../../lib/api";

export interface SimpleUser {
  id: number;
  userId: string;
  firstName: string;
  lastName: string;
  gender?: string;
  email?: string;
  contactNo?: string;
  designationId?: number;
  designationName?: string;
  name?: string;
  joinDate?: string;
  isActive?: boolean;
  skills?: string[];
  experience?: number;
  availability?: number;
  currentProjects?: string[];
}

export interface GetUsersByDesignationResponse {
  status: string;
  data: SimpleUser[];
}

export async function getAllUsers(page: number = 0, size: number = 10) {
  try {
    const response = await apiClient.get(`/api/v1/Employee/view/paged?page=${page}&size=${size}`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusCode: 200,
      data: {
        content: items.map((u: any) => ({
          id: u.empId || u.id,
          userId: `EMP${String(u.empId || u.id).padStart(4, '0')}`,
          firstName: u.firstName || '',
          lastName: u.lastName || '',
          gender: u.gender || '',
          email: u.email || '',
          contactNo: u.whatsappNumber || u.contactNo || '',
          designationId: u.designationId,
          designationName: u.designationName || '',
          name: u.designationName || '',
          joinDate: u.joinDate || '',
          isActive: u.isActive !== false,
          skills: u.skills || [],
          experience: u.experience ?? 0,
          availability: u.availablePercentage ?? 100,
          currentProjects: u.currentProjects || [],
        })),
        totalElements: resData?.totalElements ?? items.length,
        totalPages: resData?.totalPages ?? 1,
        size: resData?.size ?? size,
        number: resData?.number ?? page,
      },
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        statusCode: 200,
        data: {
          content: [],
          totalElements: 0,
          totalPages: 1,
          size,
          number: page,
        },
      };
    }
    throw err;
  }
}

export async function getAllUsersSimple() {
  try {
    const response = await apiClient.get(`/api/v1/Employee/view/paged?page=0&size=1000`);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      statusCode: 200,
      data: items.map((u: any) => ({
        id: u.empId || u.id,
        userId: `EMP${String(u.empId || u.id).padStart(4, '0')}`,
        firstName: u.firstName || '',
        lastName: u.lastName || '',
        gender: u.gender || 'Male',
        email: u.email || '',
        contactNo: u.whatsappNumber || u.contactNo || '',
        designationId: u.designationId,
        designationName: u.designationName || '',
        name: u.designationName || '',
        joinDate: u.joinDate || '',
        isActive: u.isActive !== false,
        skills: u.skills || [],
        experience: u.experience ?? 0,
        availability: u.availablePercentage ?? 100,
        currentProjects: u.currentProjects || [],
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        status: 'success',
        statusCode: 200,
        data: [],
      };
    }
    throw err;
  }
}

export async function getUsersByDesignationId(designationId: number): Promise<GetUsersByDesignationResponse> {
  try {
    const response = await apiClient.get(`/api/v1/Employee/designation/${designationId}`);
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    return {
      status: 'success',
      data: items.map((u: any) => ({
        id: u.empId || u.id,
        userId: `EMP${String(u.empId || u.id).padStart(4, '0')}`,
        firstName: u.firstName,
        lastName: u.lastName,
        designationId: u.designationId,
        designationName: u.designationName,
      })),
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return { status: 'success', data: [] };
    }
    throw err;
  }
}

export default getAllUsers;
