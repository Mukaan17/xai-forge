/**
 * Authentication API endpoints
 */
import apiClient from './client';

export const authAPI = {
  /**
   * Login with email and password
   */
  login: async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data;
  },

  /**
   * Register new user
   */
  register: async (userData) => {
    const response = await apiClient.post('/auth/register', userData);
    return response.data;
  },

  /**
   * Logout (clear token on client side)
   */
  logout: async () => {
    // Optionally call backend logout endpoint if it exists
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      // Ignore errors on logout
    }
  },

  /**
   * Get current authenticated user
   */
  getCurrentUser: async () => {
    const response = await apiClient.get('/users/me');
    return response.data;
  },

  /**
   * Request password reset
   */
  forgotPassword: async (email) => {
    const response = await apiClient.post('/auth/forgot-password', { email });
    return response.data;
  },

  /**
   * Reset password with token
   */
  resetPassword: async (token, newPassword) => {
    const response = await apiClient.post('/auth/reset-password', {
      token,
      newPassword,
    });
    return response.data;
  },

  /**
   * Verify 2FA code during login
   */
  verify2FA: async (code) => {
    const response = await apiClient.post('/auth/verify-2fa', { code });
    return response.data;
  },
};
