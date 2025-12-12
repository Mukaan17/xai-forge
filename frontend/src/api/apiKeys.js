/**
 * API Keys API endpoints
 */
import apiClient from './client';

export const apiKeysAPI = {
  /**
   * Get all API keys (masked)
   */
  getAll: async () => {
    const response = await apiClient.get('/keys');
    return response.data;
  },

  /**
   * Create new API key (returns full key once)
   */
  create: async (data) => {
    const response = await apiClient.post('/keys', data);
    return response.data;
  },

  /**
   * Revoke API key
   */
  revoke: async (keyId) => {
    const response = await apiClient.delete(`/keys/${keyId}`);
    return response.data;
  },
};
