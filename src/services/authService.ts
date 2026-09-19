import apiClient, { tokenManager } from '../lib/api';

interface LoginResponse {
  status: string;
  statusCode: number;
  statusMessage: string;
  data: {
    token: string;
    refreshToken: string;
    type: string;
    userId: number;
    employeeId: number | null;
    companyStaffId: number | null;
    email: string;
    firstName: string;
    lastName: string;
    userType: string;
    roles: string[];
    globalPermissions: string[];
    projectAccessList: string[];
  };
}

const clearAuthDataHelper = (): void => {
  tokenManager.removeToken();
  tokenManager.removeRefreshToken();
  delete apiClient.defaults.headers.common['Authorization'];
  localStorage.removeItem('user');
  localStorage.removeItem('authToken');
  localStorage.removeItem('auth_token');
  sessionStorage.removeItem('auth_token');
  sessionStorage.removeItem('redirectUrl');

  const APP_STORAGE_KEYS = [
    'statusWorkflowNodes',
    'statusWorkflowEdges',
    'emailConfigTab',
    'assignments',
    'selectedProjectId',
    'redirectUrl'
  ];

  APP_STORAGE_KEYS.forEach((key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
};

class AuthService {
  static async login(email: string, password?: string): Promise<LoginResponse> {
    this.clearAuthData();

    const response = await apiClient.post('/api/v1/auth/login', {
      email,
      password,
    });

    const data = response.data;
    const token = data.token || data.accessToken || data.jwt;

    const names = (data.employeeName || 'User').split(' ');
    const firstName = names[0] || 'User';
    const lastName = names.slice(1).join(' ') || '';

    const employeeFullName = (data.employeeName || `${firstName} ${lastName}`).trim() || 'Admin SGIC';

    const userData = {
      userId: Number(data.employeeId || 1),
      employeeId: Number(data.employeeId || 1),
      companyStaffId: Number(data.employeeId || 1),
      email: data.email || email,
      firstName,
      lastName,
      employeeName: employeeFullName,
      name: employeeFullName,
      fullName: employeeFullName,
      userType: 'ADMIN',
      roles: data.roles && data.roles.length > 0 ? data.roles : ['ROLE_ADMIN', 'ADMIN'],
      globalPermissions: ['ALL_PERMISSIONS'],
      projectAccessList: [],
    };

    tokenManager.setToken(token);
    if (data.refreshToken) {
      tokenManager.setRefreshToken(data.refreshToken);
    }
    localStorage.setItem('user', JSON.stringify(userData));

    return {
      status: 'success',
      statusCode: 200,
      statusMessage: data.message || 'Login successful',
      data: {
        token,
        refreshToken: data.refreshToken || token,
        type: 'Bearer',
        ...userData,
      },
    };
  }

  static async ensureAuthenticated(): Promise<string> {
    const existingToken = tokenManager.getToken();
    if (existingToken && !existingToken.startsWith('mock_')) {
      return existingToken;
    }
    throw new Error('User is not authenticated');
  }

  static async changePassword(
    currentPassword?: string,
    newPassword?: string,
    empId?: number,
    email?: string
  ): Promise<void> {
    const currentUser = this.getCurrentUser();
    const resolvedEmail = email || currentUser?.email;
    const resolvedEmpId = empId || currentUser?.employeeId || currentUser?.userId;

    await apiClient.post('/api/v1/auth/change-password', {
      currentPassword,
      oldPassword: currentPassword,
      newPassword,
      email: resolvedEmail,
      empId: resolvedEmpId ? Number(resolvedEmpId) : undefined,
    });
  }

  static async logout(): Promise<void> {
    this.clearAuthData();
    window.dispatchEvent(new CustomEvent('auth:logout'));
    try {
      await apiClient.post('/api/v1/auth/log-out');
    } catch {
      // ignore logout network errors
    }
  }

  static clearAuthData(): void {
    clearAuthDataHelper();
  }

  static logoutImmediate(): void {
    this.clearAuthData();
  }

  static isAuthenticated(): boolean {
    const token = tokenManager.getToken();
    return token !== null && token.length > 0 && !token.startsWith('mock_');
  }

  static getCurrentUser(): any | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  static getCurrentUserFullName(): string {
    const user = this.getCurrentUser();
    if (!user) return 'Admin SGIC';
    if (user.employeeName) return user.employeeName;
    if (user.fullName) return user.fullName;
    if (user.name) return user.name;
    const full = `${user.firstName || ''} ${user.lastName || ''}`.trim();
    if (full && full !== 'User') return full;
    if (user.username) return user.username;
    if (user.email) return user.email.split('@')[0];
    return 'Admin SGIC';
  }

  static getToken(): string | null {
    return tokenManager.getToken();
  }
}

export default AuthService;