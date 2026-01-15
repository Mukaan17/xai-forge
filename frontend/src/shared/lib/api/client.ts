import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from 'axios';
import { ApiError, ProblemDetail } from '@/shared/types/api.types';

function generateCorrelationId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

class ApiClient {
  private client: AxiosInstance;
  private retryConfig = { maxRetries: 3, retryDelay: 1000 };

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
      timeout: 30000,
      headers: { 'Content-Type': 'application/json' },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor
    this.client.interceptors.request.use((config) => {
      const correlationId = generateCorrelationId();
      config.headers['X-Correlation-ID'] = correlationId;

      // Try to get token from localStorage (direct or from Zustand persisted state)
      let token = localStorage.getItem('token');
      if (!token) {
        const authStorage = localStorage.getItem('auth-storage');
        if (authStorage) {
          try {
            const parsed = JSON.parse(authStorage);
            token = parsed.state?.token || null;
          } catch {
            // Not valid JSON, ignore
          }
        }
      }
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      return config;
    });

    // Response interceptor with error transformation
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError<ProblemDetail>) => {
        // Handle network errors
        if (!error.response) {
          throw new ApiError(
            0,
            'NETWORK_ERROR',
            'Unable to connect to server. Please check your internet connection.',
            undefined
          );
        }

        const { status, data } = error.response;

        // Handle rate limiting with retry-after
        if (status === 429) {
          const retryAfter = error.response.headers['retry-after'];
          throw new ApiError(
            429,
            'RATE_LIMIT_EXCEEDED',
            `Too many requests. Please wait ${retryAfter || 60} seconds.`,
            data?.correlationId
          );
        }

        // Handle auth errors
        if (status === 401) {
          // Don't redirect on login/register endpoints - let the component handle it
          const isAuthEndpoint = error.config?.url?.includes('/auth/login') || error.config?.url?.includes('/auth/register');
          if (!isAuthEndpoint) {
            localStorage.removeItem('token');
            window.location.href = '/login';
            throw new ApiError(401, 'UNAUTHORIZED', 'Session expired. Please log in again.');
          }
          // For login/register, extract the detail message from the problem detail
          if (data && typeof data === 'object' && 'detail' in data) {
            throw ApiError.fromProblemDetail(data);
          }
          // Fallback if no problem detail
          throw new ApiError(401, 'UNAUTHORIZED', 'Invalid username or password.');
        }

        // Transform to ApiError from ProblemDetail
        if (data && typeof data === 'object' && 'detail' in data) {
          throw ApiError.fromProblemDetail(data);
        }

        // Fallback for non-standard errors
        throw new ApiError(
          status,
          'UNKNOWN_ERROR',
          'An unexpected error occurred.',
          undefined
        );
      }
    );
  }

  async request<T>(config: AxiosRequestConfig): Promise<T> {
    let lastError: Error | null = null;

    for (let attempt = 0; attempt <= this.retryConfig.maxRetries; attempt++) {
      try {
        const response = await this.client.request<T>(config);
        return response.data;
      } catch (error) {
        lastError = error as Error;

        // Don't retry on client errors (4xx) except 429
        // Also don't retry on authentication errors (401)
        if (error instanceof ApiError) {
          if (error.status >= 400 && error.status < 500 && error.status !== 429) {
            throw error; // Immediately throw, no retry
          }
          // For 401, don't retry
          if (error.status === 401) {
            throw error;
          }
        }

        // Wait before retry with exponential backoff
        if (attempt < this.retryConfig.maxRetries) {
          const delay = this.retryConfig.retryDelay * Math.pow(2, attempt);
          await new Promise((resolve) => setTimeout(resolve, delay));
        }
      }
    }

    throw lastError;
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.request<T>({ ...config, method: 'GET', url });
  }

  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return this.request<T>({ ...config, method: 'POST', url, data });
  }

  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return this.request<T>({ ...config, method: 'PUT', url, data });
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.request<T>({ ...config, method: 'DELETE', url });
  }

  async downloadFile(url: string, filename: string, config?: AxiosRequestConfig): Promise<void> {
    const response = await this.client.get(url, {
      ...config,
      responseType: 'blob',
    });
    
    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }
}

export const apiClient = new ApiClient();

