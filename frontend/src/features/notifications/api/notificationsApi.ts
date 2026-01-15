import { apiClient } from '@/shared/lib/api/client';
import { PaginatedResponse } from '@/shared/types/api.types';

export interface NotificationDto {
  id: number;
  type: string;
  title: string;
  message: string;
  detail: string | null;
  read: boolean;
  createdAt: string;
}

export const notificationsApi = {
  getAll: async (page: number = 0, size: number = 20): Promise<PaginatedResponse<NotificationDto>> => {
    const response = await apiClient.get<PaginatedResponse<NotificationDto>>('/v1/notifications', {
      params: { page, size },
    });
    return response;
  },

  getUnreadCount: async (): Promise<{ count: number }> => {
    const response = await apiClient.get<{ count: number }>('/v1/notifications/unread-count');
    return response;
  },

  markAsRead: async (id: number): Promise<void> => {
    await apiClient.put(`/v1/notifications/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.put('/v1/notifications/read-all');
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/notifications/${id}`);
  },
};
