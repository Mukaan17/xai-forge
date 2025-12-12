/**
 * Predictions API endpoints
 */
import apiClient, { downloadFile } from './client';

export const predictionsAPI = {
  /**
   * Get all predictions with filters and pagination
   */
  getAll: async (params = {}) => {
    const response = await apiClient.get('/predictions', { params });
    return response.data;
  },

  /**
   * Get prediction by ID
   */
  getById: async (id) => {
    const response = await apiClient.get(`/predictions/${id}`);
    return response.data;
  },

  /**
   * Make prediction with a model
   */
  predict: async (modelId, inputData) => {
    const response = await apiClient.post(`/models/${modelId}/predict`, inputData);
    return response.data;
  },

  /**
   * Delete prediction
   */
  delete: async (id) => {
    const response = await apiClient.delete(`/predictions/${id}`);
    return response.data;
  },

  /**
   * Bulk delete predictions
   */
  bulkDelete: async (ids) => {
    const response = await apiClient.post('/predictions/bulk-delete', { ids });
    return response.data;
  },

  /**
   * Regenerate explanation for prediction
   */
  reExplain: async (id) => {
    const response = await apiClient.post(`/predictions/${id}/re-explain`);
    return response.data;
  },

  /**
   * Export predictions to CSV
   */
  exportCsv: async (params = {}) => {
    return downloadFile('/predictions/export', 'predictions.csv', params);
  },

  /**
   * Export predictions to JSON
   */
  exportJson: async (params = {}) => {
    const response = await apiClient.get('/predictions/export', {
      params: { ...params, format: 'json' },
    });
    return response.data;
  },
};
