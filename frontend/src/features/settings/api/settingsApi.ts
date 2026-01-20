import { apiClient } from '@/shared/lib/api/client';

export interface UserDto {
  id: number;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  organization?: string;
  role?: string;
  twoFactorEnabled: boolean;
}

export interface ProfileDto {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  organization?: string;
  role?: string;
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
  getProfile: async (): Promise<ProfileDto> => {
    return apiClient.get<ProfileDto>('/v1/auth/profile');
  },

  updateProfile: async (data: UpdateProfileRequest): Promise<ProfileDto> => {
    return apiClient.put<ProfileDto>('/v1/auth/profile', data);
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
