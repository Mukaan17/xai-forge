import { apiClient } from '@/shared/lib/api/client';
import { User } from '../store/authStore';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organization?: string;
  role?: string;
}

export interface RegisterResponse {
  token: string;
  user: User;
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<{ token: string; user: { id: number; username: string; email: string; firstName?: string; lastName?: string; twoFactorEnabled: boolean } }>('/v1/auth/login', credentials);
    return {
      token: response.token,
      user: {
        id: response.user.id,
        username: response.user.username,
        email: response.user.email,
        firstName: response.user.firstName,
        lastName: response.user.lastName,
        twoFactorEnabled: response.user.twoFactorEnabled,
      },
    };
  },
  
  register: async (data: RegisterRequest): Promise<RegisterResponse> => {
    const response = await apiClient.post<{ token: string; user: { id: number; username: string; email: string; firstName?: string; lastName?: string; twoFactorEnabled: boolean } }>('/v1/auth/register', data);
    return {
      token: response.token,
      user: {
        id: response.user.id,
        username: response.user.username,
        email: response.user.email,
        firstName: response.user.firstName,
        lastName: response.user.lastName,
        twoFactorEnabled: response.user.twoFactorEnabled,
      },
    };
  },
  
  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<{ id: number; username: string; email: string; firstName?: string; lastName?: string; twoFactorEnabled: boolean }>('/v1/auth/me');
    return {
      id: response.id,
      username: response.username,
      email: response.email,
      firstName: response.firstName,
      lastName: response.lastName,
      twoFactorEnabled: response.twoFactorEnabled,
    };
  },
  
  logout: async (): Promise<void> => {
    // Clear token on client side
    localStorage.removeItem('token');
    localStorage.removeItem('auth-storage');
  },

  checkEmailExists: async (email: string): Promise<boolean> => {
    const response = await apiClient.post<{ exists: boolean }>('/v1/auth/forgot-password/check-email', { email });
    return response.exists;
  },

  sendPasswordResetOtp: async (email: string): Promise<void> => {
    await apiClient.post('/v1/auth/forgot-password/send-otp', { email });
  },

  verifyPasswordResetOtp: async (email: string, code: string): Promise<boolean> => {
    const response = await apiClient.post<{ valid: boolean }>('/v1/auth/forgot-password/verify-otp', { email, code });
    return response.valid;
  },

  resetPassword: async (email: string, newPassword: string): Promise<void> => {
    await apiClient.post('/v1/auth/forgot-password/reset', { email, newPassword });
  },

  verifyEmail: async (token: string): Promise<{ message: string }> => {
    const response = await apiClient.get<{ message: string }>('/v1/auth/verify-email', {
      params: { token },
    });
    return response;
  },

  resendVerificationEmail: async (email: string): Promise<void> => {
    await apiClient.post('/v1/auth/resend-verification', { email });
  },
};

