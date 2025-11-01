import axios, { AxiosError, AxiosInstance } from 'axios';
import { storage } from '@utils/storage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = storage.get<string>('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Handle both 401 (Unauthorized) and 403 (Forbidden) as authentication errors
    // This ensures expired tokens trigger re-login regardless of HTTP status code
    if (error.response?.status === 401 || error.response?.status === 403) {
      storage.remove('auth_token');
      storage.remove('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export default api;
