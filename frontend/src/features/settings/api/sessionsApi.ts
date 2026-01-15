import { apiClient } from '@/shared/lib/api/client';

export interface SessionDto {
  id: number;
  deviceInfo: string;
  ipAddress: string;
  location: string | null;
  lastActiveAt: string;
  createdAt: string;
  isCurrentSession: boolean;
}

export const sessionsApi = {
  getAll: async (): Promise<SessionDto[]> => {
    return apiClient.get<SessionDto[]>('/v1/sessions');
  },

  revoke: async (sessionId: number): Promise<void> => {
    await apiClient.delete(`/v1/sessions/${sessionId}`);
  },

  revokeAllOthers: async (): Promise<void> => {
    await apiClient.delete('/v1/sessions/others');
  },
};
