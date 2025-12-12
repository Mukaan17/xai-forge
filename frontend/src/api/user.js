/**
 * User profile and settings API endpoints
 */
import apiClient, { uploadFile } from './client';

export const userAPI = {
  /**
   * Get user profile
   */
  getProfile: async () => {
    const response = await apiClient.get('/users/me');
    return response.data;
  },

  /**
   * Update user profile
   */
  updateProfile: async (data) => {
    const response = await apiClient.put('/users/me', data);
    return response.data;
  },

  /**
   * Upload profile avatar
   */
  uploadAvatar: async (file, onProgress) => {
    return uploadFile('/users/me/avatar', file, onProgress);
  },

  /**
   * Delete profile avatar
   */
  deleteAvatar: async () => {
    const response = await apiClient.delete('/users/me/avatar');
    return response.data;
  },

  /**
   * Change password
   */
  changePassword: async (data) => {
    const response = await apiClient.put('/users/me/password', data);
    return response.data;
  },

  /**
   * Enable 2FA (returns QR code and backup codes)
   */
  enable2FA: async () => {
    const response = await apiClient.post('/users/me/2fa/enable');
    return response.data;
  },

  /**
   * Verify 2FA code and activate
   */
  verify2FA: async (code) => {
    const response = await apiClient.post('/users/me/2fa/verify', { code });
    return response.data;
  },

  /**
   * Disable 2FA
   */
  disable2FA: async (code) => {
    const response = await apiClient.delete('/users/me/2fa', {
      data: { code },
    });
    return response.data;
  },

  /**
   * Get user preferences
   */
  getPreferences: async () => {
    const response = await apiClient.get('/settings/preferences');
    return response.data;
  },

  /**
   * Update user preferences
   */
  updatePreferences: async (data) => {
    const response = await apiClient.put('/settings/preferences', data);
    return response.data;
  },

  /**
   * Get user statistics
   */
  getStatistics: async () => {
    const response = await apiClient.get('/users/me/statistics');
    return response.data;
  },

  /**
   * Delete user account
   */
  deleteAccount: async (password) => {
    const response = await apiClient.delete('/users/me', {
      data: { password },
    });
    return response.data;
  },
};
