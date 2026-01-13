import { apiClient } from '@/shared/lib/api/client';

export interface UserDto {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  twoFactorEnabled: boolean;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  organization?: string;
  role?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdatePreferencesRequest {
  theme?: string;
  accentColor?: string;
  notificationPreferences?: string;
}

export const settingsApi = {
  updateProfile: async (data: UpdateProfileRequest): Promise<UserDto> => {
    return apiClient.put<UserDto>('/v1/auth/profile', data);
  },

  changePassword: async (data: ChangePasswordRequest): Promise<{ message: string }> => {
    return apiClient.put<{ message: string }>('/v1/auth/password', data);
  },

  updatePreferences: async (data: UpdatePreferencesRequest): Promise<{ message: string }> => {
    return apiClient.put<{ message: string }>('/v1/auth/preferences', data);
  },

  deleteAccount: async (): Promise<{ message: string }> => {
    return apiClient.delete<{ message: string }>('/v1/auth/account');
  },
};
