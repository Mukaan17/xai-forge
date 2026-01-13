import { QueryClient, QueryCache, MutationCache } from '@tanstack/react-query';
import { ApiError } from '@/shared/types/api.types';
import { toast } from '@/shared/lib/toast';

function handleGlobalError(error: unknown, fallbackMessage: string): void {
  if (error instanceof ApiError) {
    // Don't show toast for auth errors (handled by redirect)
    if (error.isAuthError) return;

    // Show user-friendly message
    toast.error(error.detail, {
      description: error.correlationId
        ? `Reference: ${error.correlationId}`
        : undefined,
      action: error.isServerError
        ? { label: 'Retry', onClick: () => window.location.reload() }
        : undefined,
    });
  } else {
    toast.error(fallbackMessage);
  }
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // Don't retry on 4xx errors
        if (error instanceof ApiError && error.status >= 400 && error.status < 500) {
          return false;
        }
        return failureCount < 3;
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes
    },
    mutations: {
      retry: false,
    },
  },
  queryCache: new QueryCache({
    onError: (error, query) => {
      // Only show toast for background refetches that fail
      if (query.state.data !== undefined) {
        handleGlobalError(error, 'Background sync failed');
      }
    },
  }),
  mutationCache: new MutationCache({
    onError: (error) => {
      handleGlobalError(error, 'Operation failed');
    },
  }),
});

