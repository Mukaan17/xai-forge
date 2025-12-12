/**
 * Models API endpoints
 */
import apiClient from './client';

export const modelsAPI = {
  /**
   * Get all models with pagination
   */
  getAll: async (params = {}) => {
    const response = await apiClient.get('/models', { params });
    return response.data;
  },

  /**
   * Get model by ID
   */
  getById: async (id) => {
    const response = await apiClient.get(`/models/${id}`);
    return response.data;
  },

  /**
   * Train new model
   */
  train: async (trainData) => {
    const response = await apiClient.post('/models/train', trainData);
    return response.data;
  },

  /**
   * Get training status
   */
  getTrainingStatus: async (id) => {
    const response = await apiClient.get(`/models/${id}/status`);
    return response.data;
  },

  /**
   * Delete model
   */
  delete: async (id) => {
    const response = await apiClient.delete(`/models/${id}`);
    return response.data;
  },

  /**
   * Archive model
   */
  archive: async (id) => {
    const response = await apiClient.put(`/models/${id}/archive`);
    return response.data;
  },

  /**
   * Get ready models (for prediction)
   */
  getReadyModels: async () => {
    const response = await apiClient.get('/models/ready');
    return response.data;
  },

  /**
   * Compare multiple models
   */
  compare: async (modelIds) => {
    const response = await apiClient.post('/models/compare', modelIds);
    return response.data;
  },

  /**
   * Get model versions
   */
  getVersions: async (id, baseName) => {
    const response = await apiClient.get(`/models/${id}/versions`, {
      params: { baseName },
    });
    return response.data;
  },

  /**
   * Get performance trend
   */
  getPerformanceTrend: async (id) => {
    const response = await apiClient.get(`/models/${id}/trend`);
    return response.data;
  },
};
