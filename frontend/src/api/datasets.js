/**
 * Datasets API endpoints
 */
import apiClient, { uploadFile } from './client';

export const datasetsAPI = {
  /**
   * Get all datasets with pagination
   */
  getAll: async (params = {}) => {
    const response = await apiClient.get('/datasets', { params });
    return response.data;
  },

  /**
   * Get dataset by ID
   */
  getById: async (id) => {
    const response = await apiClient.get(`/datasets/${id}`);
    return response.data;
  },

  /**
   * Upload new dataset
   */
  upload: async (file, onProgress) => {
    return uploadFile('/datasets/upload', file, onProgress);
  },

  /**
   * Update dataset metadata
   */
  update: async (id, data) => {
    const response = await apiClient.put(`/datasets/${id}`, data);
    return response.data;
  },

  /**
   * Delete dataset
   */
  delete: async (id) => {
    const response = await apiClient.delete(`/datasets/${id}`);
    return response.data;
  },

  /**
   * Get dataset preview (first N rows)
   */
  getPreview: async (id, limit = 10) => {
    const response = await apiClient.get(`/datasets/${id}/preview`, {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Get dataset columns metadata
   */
  getColumns: async (id) => {
    const response = await apiClient.get(`/datasets/${id}/columns`);
    return response.data;
  },

  /**
   * Analyze specific column
   */
  analyzeColumn: async (datasetId, columnName) => {
    const response = await apiClient.get(`/datasets/${datasetId}/columns/${columnName}/analyze`);
    return response.data;
  },
};
