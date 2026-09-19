import apiClient from "../../lib/api";
import { ENDPOINTS } from "../../utils/apiendpoint";

export interface Designations {
  id: number;
  name: string;
}

export interface CreateDesignations {
  name: string;
}

export async function getDesignations(page: number = 0, size: number = 10) {
  const response = await apiClient.get(ENDPOINTS.designationPagination(page, size));
  const pageData = response.data?.data || response.data || {};
  const content = (pageData.content || []).map((d: any) => ({
    id: d.designationId ?? d.id,
    name: d.designationName ?? d.name,
  }));

  return {
    status: 'success',
    statusCode: response.status || 200,
    data: {
      content,
      totalElements: pageData.totalElements ?? content.length,
      totalPages: pageData.totalPages ?? 1,
      size: pageData.size ?? size,
      number: pageData.number ?? page,
    },
  };
}

export const getAllDesignations = getDesignations;

export async function createDesignation(data: CreateDesignations) {
  const response = await apiClient.post(ENDPOINTS.designationCreate, {
    designationName: data.name,
  });
  const resData = response.data?.data || response.data || {};
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Designation created successfully',
    statusMessage: response.data?.statusMessage || 'Designation created successfully',
    data: {
      id: resData.designationId ?? resData.id,
      name: resData.designationName ?? resData.name ?? data.name,
    },
  };
}

export async function putDesignation(id: number, data: CreateDesignations) {
  const response = await apiClient.put(ENDPOINTS.designationUpdate(id), {
    designationName: data.name,
  });
  const resData = response.data?.data || response.data || {};
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Designation updated successfully',
    statusMessage: response.data?.statusMessage || 'Designation updated successfully',
    data: {
      id: resData.designationId ?? resData.id ?? id,
      name: resData.designationName ?? resData.name ?? data.name,
    },
  };
}

export const updateDesignation = putDesignation;

export async function deleteDesignation(id: number) {
  const response = await apiClient.delete(ENDPOINTS.designationDelete(id));
  return {
    status: 'success',
    statusCode: response.status || 200,
    message: response.data?.statusMessage || 'Designation deleted successfully',
    statusMessage: response.data?.statusMessage || 'Designation deleted successfully',
    data: response.data,
  };
}
