import { apiClient } from '@/shared/lib/api/client';

export interface ModelDto {
  id: number;
  modelName: string;
  modelType: string;
  trainingDate: string;
  accuracy: number | null;
  status: string;
  targetVariable?: string;
  featureNames?: string[];
}

export interface TrainRequest {
  datasetId: number;
  modelName: string;
  algorithm: string; // "CLASSIFICATION", "REGRESSION", "logistic", "random_forest", etc.
  targetColumn: string;
  featureNames: string[];
  trainTestSplit?: number;
  crossValidation?: boolean;
  hyperparameters?: Record<string, unknown>; // Algorithm-specific hyperparameters
}

export interface TrainResponse {
  id: number;
  jobId: number;
  message: string;
}

export interface TrainingProgressDto {
  jobId: number;
  modelId: number;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  progress: number;
  currentStep: string | null;
  errorMessage: string | null;
  startedAt: string | null;
  completedAt: string | null;
  estimatedCompletionSeconds: number | null;
}

export const modelsApi = {
  getAll: async (): Promise<ModelDto[]> => {
    return apiClient.get<ModelDto[]>('/v1/models');
  },

  getById: async (id: number): Promise<ModelDto> => {
    return apiClient.get<ModelDto>(`/v1/models/${id}`);
  },

  train: async (request: TrainRequest): Promise<TrainResponse> => {
    return apiClient.post<TrainResponse>('/v1/models/train', request);
  },

  getTrainingProgress: async (modelId: number): Promise<TrainingProgressDto> => {
    return apiClient.get<TrainingProgressDto>(`/v1/models/${modelId}/progress`);
  },

  getTrainingJobs: async (): Promise<TrainingProgressDto[]> => {
    return apiClient.get<TrainingProgressDto[]>('/v1/models/training/jobs');
  },

  cancelTraining: async (jobId: number): Promise<void> => {
    await apiClient.post(`/v1/models/training/${jobId}/cancel`);
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/v1/models/${id}`);
  },
};
