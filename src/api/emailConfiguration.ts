import apiClient from "../lib/api";

export interface CreateSmtpConfigRequest {
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
}

export interface CreateSmtpConfigResponse {
  id: number;
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
}

export const createSmtpConfig = async (data: CreateSmtpConfigRequest): Promise<CreateSmtpConfigResponse> => {
  const response = await apiClient.post('/api/v1/email-config/create', {
    name: data.name,
    host: data.smtpHost,
    port: data.smtpPort,
    username: data.username,
    password: data.password,
    fromEmail: data.fromEmail,
    fromName: data.fromName,
  });
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id,
    name: resData?.name || data.name,
    smtpHost: resData?.host || data.smtpHost,
    smtpPort: resData?.port || data.smtpPort,
    username: resData?.username || data.username,
    password: resData?.password || data.password,
    fromEmail: resData?.fromEmail || data.fromEmail,
    fromName: resData?.fromName || data.fromName,
  };
};

export const getSmtpConfigs = async (): Promise<CreateSmtpConfigResponse[]> => {
  try {
    const response = await apiClient.get('/api/v1/email-config');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    return items.map((c: any) => ({
      id: c.id,
      name: c.name || '',
      smtpHost: c.host || c.smtpHost || '',
      smtpPort: c.port || c.smtpPort || 587,
      username: c.username || '',
      password: c.password || '',
      fromEmail: c.fromEmail || '',
      fromName: c.fromName || '',
    }));
  } catch {
    return [];
  }
};

export const updateSmtpConfig = async (id: number, data: CreateSmtpConfigRequest): Promise<CreateSmtpConfigResponse> => {
  const response = await apiClient.put(`/api/v1/email-config/${id}`, {
    name: data.name,
    host: data.smtpHost,
    port: data.smtpPort,
    username: data.username,
    password: data.password,
    fromEmail: data.fromEmail,
    fromName: data.fromName,
  });
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id || id,
    name: resData?.name || data.name,
    smtpHost: resData?.host || data.smtpHost,
    smtpPort: resData?.port || data.smtpPort,
    username: resData?.username || data.username,
    password: resData?.password || data.password,
    fromEmail: resData?.fromEmail || data.fromEmail,
    fromName: resData?.fromName || data.fromName,
  };
};

export const deleteSmtpConfig = async (id: number): Promise<void> => {
  await apiClient.delete(`/api/v1/email-config/${id}`);
};