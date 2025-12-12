/**
 * Webhooks API endpoints
 */
import apiClient from './client';

export const webhooksAPI = {
  /**
   * Get all webhooks
   */
  getAll: async () => {
    const response = await apiClient.get('/webhooks');
    return response.data;
  },

  /**
   * Get webhook by ID
   */
  getById: async (id) => {
    const response = await apiClient.get(`/webhooks/${id}`);
    return response.data;
  },

  /**
   * Create webhook
   */
  create: async (data) => {
    const response = await apiClient.post('/webhooks', data);
    return response.data;
  },

  /**
   * Update webhook
   */
  update: async (id, data) => {
    const response = await apiClient.put(`/webhooks/${id}`, data);
    return response.data;
  },

  /**
   * Delete webhook
   */
  delete: async (id) => {
    const response = await apiClient.delete(`/webhooks/${id}`);
    return response.data;
  },

  /**
   * Test webhook
   */
  test: async (id) => {
    const response = await apiClient.post(`/webhooks/${id}/test`);
    return response.data;
  },
};
