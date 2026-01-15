import { apiClient } from '@/shared/lib/api/client';

export interface ExportJobDto {
  id: number;
  status: string;
  progress: number;
  currentStep: string | null;
  errorMessage: string | null;
  fileSize: number | null;
  createdAt: string;
  completedAt: string | null;
  expiresAt: string | null;
}

export interface ExportRequest {
  includeItems?: string[];
}

export const exportApi = {
  requestExport: async (request: ExportRequest): Promise<ExportJobDto> => {
    const response = await apiClient.post<ExportJobDto>('/v1/export/request', request);
    return response;
  },

  getExportJobs: async (): Promise<ExportJobDto[]> => {
    const response = await apiClient.get<ExportJobDto[]>('/v1/export/jobs');
    return response;
  },

  getExportStatus: async (jobId: number): Promise<ExportJobDto> => {
    const response = await apiClient.get<ExportJobDto>(`/v1/export/jobs/${jobId}/status`);
    return response;
  },

  downloadExport: async (jobId: number): Promise<Blob> => {
    const response = await apiClient.get(`/v1/export/jobs/${jobId}/download`, {
      responseType: 'blob',
    });
    return response as unknown as Blob;
  },
};
