/**
 * Data export API endpoints
 */
import apiClient, { downloadFile } from './client';

export const exportsAPI = {
  /**
   * Get all export jobs
   */
  getAll: async () => {
    const response = await apiClient.get('/export');
    return response.data;
  },

  /**
   * Request full data export
   */
  requestExport: async (data) => {
    const response = await apiClient.post('/export/full', data);
    return response.data;
  },

  /**
   * Get export job status
   */
  getStatus: async (jobId) => {
    const response = await apiClient.get(`/export/${jobId}/status`);
    return response.data;
  },

  /**
   * Download completed export
   */
  download: async (jobId) => {
    return downloadFile(`/export/${jobId}/download`, `xai-export-${jobId}.zip`);
  },
};
