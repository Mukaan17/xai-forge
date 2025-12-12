/**
 * Dashboard API endpoints
 */
import apiClient from './client';

export const dashboardAPI = {
  /**
   * Get dashboard summary with KPIs
   */
  getSummary: async () => {
    const response = await apiClient.get('/dashboard/summary');
    return response.data;
  },

  /**
   * Get recent activity feed
   */
  getRecentActivity: async (limit = 10) => {
    const response = await apiClient.get('/dashboard/recent-activity', {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Get model distribution by type
   */
  getModelsByType: async () => {
    const response = await apiClient.get('/dashboard/models-by-type');
    return response.data;
  },

  /**
   * Get usage trend over time
   */
  getUsageTrend: async (days = 30) => {
    const response = await apiClient.get('/dashboard/usage-trend', {
      params: { days },
    });
    return response.data;
  },

  /**
   * Get recent models
   */
  getRecentModels: async (limit = 5) => {
    const response = await apiClient.get('/dashboard/recent-models', {
      params: { limit },
    });
    return response.data;
  },

  /**
   * Get quick stats for sidebar
   */
  getQuickStats: async () => {
    const response = await apiClient.get('/dashboard/quick-stats');
    return response.data;
  },
};
