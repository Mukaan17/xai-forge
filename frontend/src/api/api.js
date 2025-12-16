/**
 * @Date:   2025-09-04 16:08:28
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:36:50
 */
import axios from 'axios';

// Use environment variable for API URL, fallback to localhost for development
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Don't redirect on 401/403 for auth endpoints (login/register) - let them handle errors
    const isAuthEndpoint = error.config?.url?.includes('/auth/login') || 
                          error.config?.url?.includes('/auth/register');
    
    // Handle both 401 (Unauthorized) and 403 (Forbidden) - both indicate auth issues
    if ((error.response?.status === 401 || error.response?.status === 403) && !isAuthEndpoint) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (userData) => api.post('/auth/register', userData),
  login: (credentials) => api.post('/auth/login', credentials),
};

// Dataset API
export const datasetAPI = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/datasets/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  getAll: () => api.get('/datasets'),
  getById: (id) => api.get(`/datasets/${id}`),
  delete: (id) => api.delete(`/datasets/${id}`),
};

// Model API
export const modelAPI = {
  train: (trainData) => api.post('/models/train', trainData),
  getAll: () => api.get('/models'),
  getById: (id) => api.get(`/models/${id}`),
  predict: (id, inputData) => api.post(`/models/${id}/predict`, inputData),
  explain: (id, inputData) => api.post(`/models/${id}/explain`, inputData),
  delete: (id) => api.delete(`/models/${id}`),
};

export default api;
