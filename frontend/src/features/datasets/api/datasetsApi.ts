import { apiClient } from '@/shared/lib/api/client';
import { DatasetDto } from '@/shared/types/dataset.types';
import { PaginatedResponse, PaginationParams } from '@/shared/types/api.types';

export interface DatasetPreviewDto {
  rows: Record<string, string>[];
  totalRows: number;
  offset: number;
  limit: number;
  hasMore: boolean;
}

export const datasetsApi = {
  getAll: async (params?: PaginationParams): Promise<PaginatedResponse<DatasetDto>> => {
    return apiClient.get<PaginatedResponse<DatasetDto>>('/v1/datasets', { params });
  },

  getById: async (id: number): Promise<DatasetDto> => {
    return apiClient.get<DatasetDto>(`/v1/datasets/${id}`);
  },

  upload: async (file: File): Promise<DatasetDto> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/v1/datasets/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
      },
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.detail || 'Upload failed');
    }

    return response.json() as Promise<DatasetDto>;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/datasets/${id}`);
  },

  getPreview: async (id: number, rows: number = 10, offset: number = 0): Promise<DatasetPreviewDto> => {
    const url = `/v1/datasets/${id}/preview?rows=${rows}&offset=${offset}`;
    return apiClient.get<DatasetPreviewDto>(url);
  },

  export: async (id: number, filename: string): Promise<void> => {
    await apiClient.downloadFile(`/v1/datasets/${id}/export`, filename);
  },
};
