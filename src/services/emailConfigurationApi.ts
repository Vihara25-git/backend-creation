import { SmtpConfig } from "../types/emailConfiguration";
import apiClient from "../lib/api";

export interface CreateSmtpConfigRequest {
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
  isEnabled?: boolean;
}

export const createSmtpConfig = async (data: CreateSmtpConfigRequest): Promise<SmtpConfig> => {
  const response = await apiClient.post('/api/v1/email-config/create', {
    name: data.name,
    host: data.smtpHost,
    port: Number(data.smtpPort),
    smtpUserName: data.username,
    smtpPassword: data.password,
    fromEmail: data.fromEmail,
    fromName: data.fromName,
    status: Boolean(data.isEnabled),
  });
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id,
    name: resData?.name || data.name,
    smtpHost: resData?.host || data.smtpHost,
    smtpPort: resData?.port || data.smtpPort,
    username: resData?.smtpUserName || resData?.username || data.username,
    password: resData?.smtpPassword || data.password,
    fromEmail: resData?.fromEmail || data.fromEmail,
    fromName: resData?.fromName || data.fromName,
    isEnabled: resData?.status === true || resData?.status === 'ACTIVE' || resData?.isEnabled || false,
    createdAt: resData?.createdAt || new Date().toISOString(),
    updatedAt: resData?.updatedAt || new Date().toISOString(),
  };
};

export const getSmtpConfigs = async (): Promise<SmtpConfig[]> => {
  try {
    const response = await apiClient.get('/api/v1/email-config');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];

    const mapped: SmtpConfig[] = items.map((c: any) => ({
      id: c.id,
      name: c.name || '',
      smtpHost: c.host || c.smtpHost || '',
      smtpPort: c.port || c.smtpPort || 587,
      username: c.smtpUserName || c.username || '',
      password: c.smtpPassword || '',
      fromEmail: c.fromEmail || '',
      fromName: c.fromName || '',
      isEnabled: c.status === true || c.status === 'ACTIVE' || c.isEnabled || false,
      createdAt: c.createdAt || new Date().toISOString(),
      updatedAt: c.updatedAt || new Date().toISOString(),
    }));

    return mapped.sort((a, b) => {
      const aActive = a.isEnabled ? 1 : 0;
      const bActive = b.isEnabled ? 1 : 0;
      if (bActive !== aActive) {
        return bActive - aActive;
      }
      return (a.id ?? 0) - (b.id ?? 0);
    });
  } catch {
    return [];
  }
};

export const updateSmtpConfig = async (id: number, data: CreateSmtpConfigRequest): Promise<SmtpConfig> => {
  const payload: any = {
    name: data.name,
    host: data.smtpHost,
    port: Number(data.smtpPort),
    smtpUserName: data.username,
    fromEmail: data.fromEmail,
    fromName: data.fromName,
  };
  if (data.password && data.password.trim()) {
    payload.smtpPassword = data.password.trim();
  }
  if (data.isEnabled !== undefined) {
    payload.status = Boolean(data.isEnabled);
  }

  const response = await apiClient.put(`/api/v1/email-config/${id}`, payload);
  const resData = response.data?.data || response.data;
  return {
    id: resData?.id || id,
    name: resData?.name || data.name,
    smtpHost: resData?.host || data.smtpHost,
    smtpPort: resData?.port || data.smtpPort,
    username: resData?.smtpUserName || resData?.username || data.username,
    password: resData?.smtpPassword || data.password,
    fromEmail: resData?.fromEmail || data.fromEmail,
    fromName: resData?.fromName || data.fromName,
    isEnabled: resData?.status === true || resData?.status === 'ACTIVE' || resData?.isEnabled || false,
    createdAt: resData?.createdAt || new Date().toISOString(),
    updatedAt: resData?.updatedAt || new Date().toISOString(),
  };
};

export const deleteSmtpConfig = async (id: number): Promise<{ status: string; message: string }> => {
  const response = await apiClient.delete(`/api/v1/email-config/${id}`);
  return {
    status: 'success',
    message: typeof response.data === 'string' ? response.data : 'Configuration deleted successfully',
  };
};

export const updateSmtpConfigStatus = async (id: number, _isEnabled: boolean) => {
  const response = await apiClient.put(`/api/v1/email-config/${id}/toggle`);
  return {
    status: 'success',
    statusCode: 200,
    data: response.data?.data || response.data,
  };
};

export const getAllEmailPointSetups = async () => {
  try {
    const response = await apiClient.get('/api/v1/email-templates');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];
    return items.map((t: any) => ({
      id: Number(t.templateId ?? t.id),
      eventType: t.emailNotificationType,
      description: t.subject,
      isEnabled: t.status !== false,
      createdAt: t.createdAt,
      updatedAt: t.updatedAt,
    }));
  } catch (error) {
    console.error('Error loading email point setups:', error);
    return [];
  }
};

export const getRoleNotificationChannels = async (roleId: number): Promise<Record<string, string>> => {
  try {
    const response = await apiClient.get(`/api/v1/role/${roleId}/assigned-points`);
    const data = response.data?.data || response.data || {};
    return data;
  } catch (error) {
    console.error('Error loading role notification channels:', error);
    return {};
  }
};

export const updateRoleNotificationRules = async (roleId: number, pointChannels: Map<number, string>): Promise<void> => {
  const pointChannelsObj: Record<string, string> = {};
  pointChannels.forEach((channel, pointId) => {
    pointChannelsObj[String(pointId)] = channel;
  });
  await apiClient.put('/api/v1/role-notifications/update', {
    roleId,
    pointChannels: pointChannelsObj,
  });
};

export const getUserNotificationChannels = async (userId: number): Promise<Record<string, string>> => {
  try {
    const response = await apiClient.get(`/api/v1/user/${userId}/notification-preferences`);
    const data = response.data?.data || response.data || {};
    return data;
  } catch (error) {
    console.error('Error loading user notification channels:', error);
    return {};
  }
};

export const updateUserExtraPoints = async (userId: number, pointChannels: Map<number, string>): Promise<void> => {
  const pointChannelsObj: Record<string, string> = {};
  pointChannels.forEach((channel, pointId) => {
    pointChannelsObj[String(pointId)] = channel;
  });
  await apiClient.put('/api/v1/user/notification-preferences/update', {
    userId,
    pointChannels: pointChannelsObj,
  });
};

export const deleteUserNotificationPreferences = async (userId: number): Promise<void> => {
  await apiClient.put('/api/v1/user/notification-preferences/update', {
    userId,
    pointChannels: {},
  });
};

export const updateEmailPointSetupStatus = async (id: number, isEnabled: boolean) => {
  return {
    status: 'success',
    statusCode: 200,
    data: { id, isEnabled },
  };
};

export const getAllEmailTemplates = async () => {
  try {
    const response = await apiClient.get('/api/v1/email-templates');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];
    return {
      status: 'success',
      statusCode: 200,
      data: items,
    };
  } catch {
    return {
      status: 'success',
      statusCode: 200,
      data: [],
    };
  }
};

export const updateEmailTemplate = async (
  id: number,
  data: { subject?: string; body?: string; status?: boolean; emailNotificationType?: string }
) => {
  const response = await apiClient.put(`/api/v1/email-templates/${id}`, data);
  return {
    status: 'success',
    statusCode: 200,
    data: response.data?.data || response.data,
  };
};

export const resetEmailTemplate = async (id: number) => {
  const response = await apiClient.put(`/api/v1/email-templates/${id}/reset`);
  return {
    status: 'success',
    statusCode: 200,
    data: response.data?.data || response.data,
  };
};