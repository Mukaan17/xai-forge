/**
 * Sessions API endpoints
 */
import apiClient from './client';

export const sessionsAPI = {
  /**
   * Get all active sessions
   */
  getActiveSessions: async () => {
    const response = await apiClient.get('/sessions');
    return response.data;
  },

  /**
   * Revoke a specific session
   */
  revokeSession: async (sessionId) => {
    const response = await apiClient.delete(`/sessions/${sessionId}`);
    return response.data;
  },

  /**
   * Revoke all other sessions (keep current)
   */
  revokeAllOtherSessions: async () => {
    const response = await apiClient.delete('/sessions/others');
    return response.data;
  },

  /**
   * Get login history
   */
  getLoginHistory: async (limit = 20) => {
    const response = await apiClient.get('/sessions/history', {
      params: { limit },
    });
    return response.data;
  },
};
