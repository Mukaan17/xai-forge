/**
 * Activity logs API endpoints
 */
import apiClient, { downloadFile } from './client';

export const activityAPI = {
  /**
   * Get all activity logs with pagination
   */
  getAll: async (params = {}) => {
    const response = await apiClient.get('/activity', { params });
    return response.data;
  },

  /**
   * Get activity log by ID
   */
  getById: async (id) => {
    const response = await apiClient.get(`/activity/${id}`);
    return response.data;
  },

  /**
   * Export activity logs to CSV
   */
  exportCsv: async () => {
    return downloadFile('/activity/export', 'activity_logs.csv');
  },
};
