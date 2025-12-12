/**
 * Notifications API endpoints
 */
import apiClient from './client';

export const notificationsAPI = {
  /**
   * Get all notifications with pagination
   */
  getAll: async (params = {}) => {
    const response = await apiClient.get('/notifications', { params });
    return response.data;
  },

  /**
   * Get unread notification count
   */
  getUnreadCount: async () => {
    const response = await apiClient.get('/notifications/unread-count');
    return response.data;
  },

  /**
   * Mark notification as read
   */
  markAsRead: async (id) => {
    const response = await apiClient.put(`/notifications/${id}/read`);
    return response.data;
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async () => {
    const response = await apiClient.put('/notifications/read-all');
    return response.data;
  },

  /**
   * Delete notification
   */
  delete: async (id) => {
    const response = await apiClient.delete(`/notifications/${id}`);
    return response.data;
  },
};
