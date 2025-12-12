/**
 * Notifications store using Zustand
 */
import { create } from 'zustand';
import { notificationsAPI } from '../api/notifications';

const useNotificationsStore = create((set, get) => ({
  // State
  notifications: [],
  unreadCount: 0,
  isLoading: false,
  error: null,

  // Actions
  fetchNotifications: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const response = await notificationsAPI.getAll(params);
      const notifications = response.content || response.data || response;
      set({
        notifications: Array.isArray(notifications) ? notifications : [],
        isLoading: false,
        error: null,
      });
      return notifications;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Failed to fetch notifications',
        isLoading: false,
      });
      throw error;
    }
  },

  fetchUnreadCount: async () => {
    try {
      const response = await notificationsAPI.getUnreadCount();
      const count = response.count || response;
      set({ unreadCount: typeof count === 'number' ? count : 0 });
      return count;
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
      return 0;
    }
  },

  markAsRead: async (id) => {
    try {
      await notificationsAPI.markAsRead(id);
      set((state) => ({
        notifications: state.notifications.map((n) =>
          n.id === id ? { ...n, isRead: true } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      throw error;
    }
  },

  markAllAsRead: async () => {
    try {
      await notificationsAPI.markAllAsRead();
      set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, isRead: true })),
        unreadCount: 0,
      }));
    } catch (error) {
      console.error('Failed to mark all as read:', error);
      throw error;
    }
  },

  deleteNotification: async (id) => {
    try {
      await notificationsAPI.delete(id);
      set((state) => {
        const notification = state.notifications.find((n) => n.id === id);
        return {
          notifications: state.notifications.filter((n) => n.id !== id),
          unreadCount: notification && !notification.isRead
            ? Math.max(0, state.unreadCount - 1)
            : state.unreadCount,
        };
      });
    } catch (error) {
      console.error('Failed to delete notification:', error);
      throw error;
    }
  },

  setUnreadCount: (count) => {
    set({ unreadCount: count });
  },
}));

export default useNotificationsStore;
