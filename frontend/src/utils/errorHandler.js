/**
 * Centralized error handling
 */
import { toast } from 'sonner';

/**
 * Map HTTP errors to user-friendly messages
 */
export function mapErrorToMessage(error) {
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
 * Handle API errors with toast notifications
 */
export function handleApiError(error, customMessage = null) {
  const message = customMessage || mapErrorToMessage(error);
  toast.error(message);
  console.error('API Error:', error);
  return message;
}

/**
 * Handle validation errors
 */
export function handleValidationError(error) {
  if (error.response?.data?.errors) {
    const errors = error.response.data.errors;
    const firstError = Object.values(errors)[0];
    if (Array.isArray(firstError)) {
      toast.error(firstError[0]);
      return firstError[0];
    }
    toast.error(firstError);
    return firstError;
  }
  return handleApiError(error);
}

/**
 * Handle network errors
 */
export function handleNetworkError(error) {
  if (!error.response) {
    toast.error('Network error. Please check your connection.');
    return 'Network error. Please check your connection.';
  }
  return handleApiError(error);
}
