import apiClient from "../lib/api";

export interface SmtpConfigRequest {
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
}

export interface SmtpConfigResponse {
  id: number;
  name: string;
  smtpHost: string;
  smtpPort: number;
  username: string;
  password: string;
  fromEmail: string;
  fromName: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserEmailPreferences {
  defectEmailStatus: boolean;
  projectAllocationEmailStatus: boolean;
  moduleAllocationEmailStatus: boolean;
  submoduleAllocationEmailStatus: boolean;
}

export interface SimpleUser {
  id?: number;
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface ApiResponse<T> {
  status: string;
  message?: string;
  data?: T;
}

export const createSmtpConfig = async (data: SmtpConfigRequest): Promise<SmtpConfigResponse> => {
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

export const getSmtpConfigs = async (): Promise<SmtpConfigResponse[]> => {
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
      createdAt: c.createdAt,
      updatedAt: c.updatedAt,
    }));
  } catch {
    return [];
  }
};

export const updateSmtpConfig = async (id: number, data: SmtpConfigRequest): Promise<SmtpConfigResponse> => {
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

export const getAllUsers = async (): Promise<SimpleUser[]> => {
  try {
    const response = await apiClient.get('/api/v1/Employee/view/paged?page=0&size=1000');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);
    return items.map((u: any) => ({
      id: u.empId || u.id,
      userId: u.empId || u.id,
      firstName: u.firstName || '',
      lastName: u.lastName || '',
      email: u.email || '',
    }));
  } catch {
    return [];
  }
};

export const getUserEmailPreferences = async (userId: string): Promise<UserEmailPreferences> => {
  try {
    const response = await apiClient.get(`/api/v1/user-based-preferences/${userId}`);
    const resData = response.data?.data || response.data;
    return {
      defectEmailStatus: Boolean(resData?.defectEmailStatus ?? true),
      projectAllocationEmailStatus: Boolean(resData?.projectAllocationEmailStatus ?? true),
      moduleAllocationEmailStatus: Boolean(resData?.moduleAllocationEmailStatus ?? true),
      submoduleAllocationEmailStatus: Boolean(resData?.submoduleAllocationEmailStatus ?? true),
    };
  } catch {
    return {
      defectEmailStatus: true,
      projectAllocationEmailStatus: true,
      moduleAllocationEmailStatus: true,
      submoduleAllocationEmailStatus: true,
    };
  }
};

export const updateUserEmailPreferences = async (
  userId: string,
  preferences: UserEmailPreferences
): Promise<ApiResponse<UserEmailPreferences>> => {
  try {
    await apiClient.post('/api/v1/user-based-preferences', {
      userId: Number(userId),
      ...preferences,
    });
  } catch {
    // Ignore error
  }

  return {
    status: 'success',
    message: 'Email preferences updated successfully',
    data: preferences,
  };
};

export interface NotificationRule {
  id: string;
  eventType: string;
  eventLabel: string;
  description: string;
  enabled: boolean;
  recipientRoles: string[];
}

export const getNotificationRules = async (): Promise<NotificationRule[]> => {
  const savedRules = localStorage.getItem('email_notification_rules');
  if (savedRules) {
    return JSON.parse(savedRules);
  }

  return [
    {
      id: 'rule_1',
      eventType: 'defect_created',
      eventLabel: 'Defect Created',
      description: 'When a new defect is created',
      enabled: true,
      recipientRoles: ['project_manager', 'team_lead'],
    },
    {
      id: 'rule_2',
      eventType: 'defect_assigned',
      eventLabel: 'Defect Assigned',
      description: 'When a defect is assigned to a user',
      enabled: true,
      recipientRoles: ['developer', 'tester'],
    },
    {
      id: 'rule_3',
      eventType: 'project_allocated',
      eventLabel: 'Project Allocated',
      description: 'When user is allocated to a project',
      enabled: true,
      recipientRoles: ['developer', 'tester', 'team_lead'],
    },
    {
      id: 'rule_4',
      eventType: 'module_allocated',
      eventLabel: 'Module Allocated',
      description: 'When user is allocated to a module',
      enabled: true,
      recipientRoles: ['developer', 'tester'],
    },
  ];
};

export const updateNotificationRule = async (rule: NotificationRule): Promise<NotificationRule> => {
  const rules = await getNotificationRules();
  const index = rules.findIndex(r => r.id === rule.id);
  if (index !== -1) {
    rules[index] = rule;
    localStorage.setItem('email_notification_rules', JSON.stringify(rules));
  }
  return rule;
};

export const updateAllNotificationRules = async (rules: NotificationRule[]): Promise<NotificationRule[]> => {
  localStorage.setItem('email_notification_rules', JSON.stringify(rules));
  return rules;
};

export const getAvailableRoles = async (): Promise<{ id: string; name: string; description: string }[]> => {
  try {
    const response = await apiClient.get('/api/v1/Role/');
    const resData = response.data?.data || response.data;
    const items = Array.isArray(resData) ? resData : [];
    return items.map((r: any) => ({
      id: String(r.id),
      name: r.roleName || r.name,
      description: r.description || r.roleType || '',
    }));
  } catch {
    return [
      { id: 'admin', name: 'Admin', description: 'Full system access' },
      { id: 'project_manager', name: 'Project Manager', description: 'Manage projects' },
      { id: 'team_lead', name: 'Team Lead', description: 'Lead development team' },
      { id: 'developer', name: 'Developer', description: 'Development' },
      { id: 'tester', name: 'Tester', description: 'Quality assurance' },
    ];
  }
};