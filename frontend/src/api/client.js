/**
 * API Client Base Configuration
 * Centralized axios instance with interceptors for authentication and error handling
 */
import axios from 'axios';
import { toast } from 'sonner';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

// Request interceptor: Add JWT token from localStorage
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add API key if provided (alternative auth method)
    const apiKey = localStorage.getItem('apiKey');
    if (apiKey && !token) {
      config.headers['X-API-Key'] = apiKey;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor: Handle 401, refresh token logic, error mapping
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.includes('/auth/');
    
    // Handle authentication errors
    if ((error.response?.status === 401 || error.response?.status === 403) && !isAuthEndpoint) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('apiKey');
      // Only redirect if not already on login page
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    // Map HTTP errors to user-friendly messages
    const errorMessage = mapErrorToMessage(error);
    if (errorMessage && error.config?.showErrorToast !== false) {
      toast.error(errorMessage);
    }
    
    return Promise.reject(error);
  }
);

/**
 * Map HTTP errors to user-friendly messages
 */
function mapErrorToMessage(error) {
  if (!error.response) {
    return 'Network error. Please check your connection.';
  }
  
  const status = error.response.status;
  const data = error.response.data;
  
  // Try to get message from response
  if (data?.message) {
    return data.message;
  }
  
  if (data?.error) {
    return data.error;
  }
  
  // Map status codes to messages
  const statusMessages = {
    400: 'Invalid request. Please check your input.',
    401: 'Authentication required. Please log in.',
    403: 'You do not have permission to perform this action.',
    404: 'Resource not found.',
    409: 'This resource already exists.',
    422: 'Validation error. Please check your input.',
    429: 'Too many requests. Please try again later.',
    500: 'Server error. Please try again later.',
    503: 'Service unavailable. Please try again later.',
  };
  
  return statusMessages[status] || 'An error occurred. Please try again.';
}

/**
 * Token storage helpers
 */
export const tokenStorage = {
  getToken: () => localStorage.getItem('token'),
  setToken: (token) => localStorage.setItem('token', token),
  removeToken: () => localStorage.removeItem('token'),
  getApiKey: () => localStorage.getItem('apiKey'),
  setApiKey: (key) => localStorage.setItem('apiKey', key),
  removeApiKey: () => localStorage.removeItem('apiKey'),
};

/**
 * File upload helper with progress callback
 */
export const uploadFile = async (url, file, onProgress) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return apiClient.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(percentCompleted);
      }
    },
  });
};

/**
 * File download helper
 */
export const downloadFile = async (url, filename, params = {}) => {
  const response = await apiClient.get(url, {
    params,
    responseType: 'blob',
  });
  
  const blob = new Blob([response.data]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = filename || 'download';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
};

export default apiClient;
