import { apiClient } from '@/shared/lib/api/client';

export interface SearchResult {
  id: number;
  name: string;
  type: 'dataset' | 'model' | 'prediction';
  url: string;
  description: string;
}

export interface SearchResponse {
  datasets: SearchResult[];
  models: SearchResult[];
  predictions: SearchResult[];
  totalCount: number;
  query: string;
}

export const searchApi = {
  search: async (query: string, limit: number = 10): Promise<SearchResponse> => {
    const response = await apiClient.get<SearchResponse>('/v1/search', {
      params: { q: query, limit },
    });
    return response;
  },
};
