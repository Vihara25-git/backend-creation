  import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { ENDPOINTS } from '../utils/apiendpoint';

  


  
  export const tokenManager = {
    getToken: (): string | null => {
      return localStorage.getItem('authToken');
    },
    
    setToken: (token: string): void => {
      localStorage.setItem('authToken', token);
    },
    
    removeToken: (): void => {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
    },
     getRefreshToken: (): string | null => localStorage.getItem('refreshToken'),
  setRefreshToken: (token: string) => localStorage.setItem('refreshToken', token),
  removeRefreshToken: () => localStorage.removeItem('refreshToken'),
    isTokenValid: (): boolean => {
      const token = tokenManager.getToken();
      if (!token) return false;

      
      if (token.includes('.')) {
        try {
          const base64Url = token.split('.')[1];
          if (!base64Url) return true; 

          
          const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
          const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
          const json = atob(padded);
          const payload = JSON.parse(json);

          const currentTimeSeconds = Date.now() / 1000;
          
          if (typeof payload.exp === 'number') {
            return payload.exp > currentTimeSeconds;
          }
          return true;
        } catch {
          
          return true;
        }
      }

      
      return true;
    },

    clearAuthData: (): void => {
      tokenManager.removeToken();
      
      localStorage.removeItem('user');
      localStorage.removeItem('authToken');
      tokenManager.removeRefreshToken();
    }
  };
  const apiClient: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL || '',
    timeout: 1000000, 
  });
  const token = tokenManager.getToken();
  if (token && !token.startsWith('mock_')) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  apiClient.interceptors.request.use(
    async (config: InternalAxiosRequestConfig) => {
      // Ensure URL has /api/v1 prefix
      if (config.url && !config.url.startsWith('http') && !config.url.startsWith('/api')) {
        config.url = `/api/v1${config.url.startsWith('/') ? '' : '/'}${config.url}`;
      }

      const isAuthLoginRequest = config.url?.includes('/auth/login') ||
                                config.url?.includes('auth/login') ||
                                (config.baseURL?.includes('auth') && config.url?.includes('login'));

      if (!isAuthLoginRequest) {
        let token = tokenManager.getToken();
        if (token && token.startsWith('mock_')) {
          tokenManager.removeToken();
          token = null;
        }

        if (token) {
          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${token}`;
        }
      }

      return config;
    },
    (error) => {
      console.error('API Client: Request interceptor error:', error);
      return Promise.reject(error);
    }
  );

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

const subscribeTokenRefresh = (cb: (token: string) => void) => {
  refreshSubscribers.push(cb);
};

const onTokenRefreshed = (token: string) => {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
};

apiClient.interceptors.response.use(
  (response) => {
    
    const authHeader = response.headers?.['authorization'];
    if (authHeader && authHeader.startsWith('Bearer ')) {
      const newToken = authHeader.substring(7);
      tokenManager.setToken(newToken);
      console.log('Token renewed via sliding expiration');
    }
    
    return response;
  },

  async (error) => {
    const originalRequest = error.config;
    
    
    if (originalRequest?.url?.includes('/refresh-token')) {
      tokenManager.clearAuthData();
      window.location.href = '/login';
      return Promise.reject(error);
    }
    
    if (error.response?.status === 401 && !originalRequest?._retry) {
      originalRequest._retry = true;
      const refreshToken = tokenManager.getRefreshToken();  
      
      if (!refreshToken) {
        tokenManager.clearAuthData();
        window.location.href = '/login';
        return Promise.reject(error);
      }
      
      if (isRefreshing) {
        
        return new Promise((resolve) => {
          subscribeTokenRefresh((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            resolve(apiClient(originalRequest));
          });
        });
      }
      
      isRefreshing = true;
      
      try {
        const response = await apiClient.post(ENDPOINTS.refreshToken, { refreshToken });  
        const newAccessToken = response.data.data.token;
        const newRefreshToken = response.data.data.refreshToken;
        
        tokenManager.setToken(newAccessToken);
        tokenManager.setRefreshToken(newRefreshToken);
        
        onTokenRefreshed(newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);
        
      } catch (refreshError) {
        tokenManager.clearAuthData();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    
    
    if (error.code === 'ERR_NETWORK' || error.response?.status === 404 || error.response?.status === 500) {
      
    }
    
    return Promise.reject(error);
  }
);

  export default apiClient;
