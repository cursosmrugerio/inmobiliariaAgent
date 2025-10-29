import { LoginRequest, LoginResponse, User } from '@/types/auth.types';
import { storage } from '@utils/storage';
import api from './api';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', credentials);

    storage.set('auth_token', response.data.token);
    storage.set('user', response.data.user);

    return response.data;
  },

  logout: (): void => {
    storage.remove('auth_token');
    storage.remove('user');
  },

  getCurrentUser: (): User | null => {
    return storage.get<User>('user');
  },

  getToken: (): string | null => {
    return storage.get<string>('auth_token');
  },

  isAuthenticated: (): boolean => {
    return !!authService.getToken();
  },
};

export default authService;
