// Toast helper that matches sonner API for easy migration
import type { ToastContextType } from '@/shared/components/ui/toast';

// Create a singleton toast instance for direct imports (like sonner)
let toastInstance: ToastContextType | null = null;

// This will be set by the ToastProvider
export function setToastInstance(instance: ToastContextType) {
  toastInstance = instance;
}

// Export a hook that returns toast functions matching sonner API
export function useToast() {
  if (!toastInstance) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  
  return {
    toast: (message: string, options?: { description?: string; variant?: 'success' | 'warning' | 'info' | 'error' }) => {
      if (options?.variant) {
        return toastInstance!.toast({ title: message, description: options.description, variant: options.variant });
      }
      return toastInstance!.info(message, options?.description);
    },
    success: (message: string, options?: { description?: string }) => {
      return toastInstance!.success(message, options?.description);
    },
    warning: (message: string, options?: { description?: string }) => {
      return toastInstance!.warning(message, options?.description);
    },
    info: (message: string, options?: { description?: string }) => {
      return toastInstance!.info(message, options?.description);
    },
    error: (message: string, options?: { description?: string }) => {
      return toastInstance!.error(message, options?.description);
    },
  };
}

// Direct toast functions for backward compatibility with sonner
export const toast = {
  success: (message: string, options?: { description?: string; action?: { label: string; onClick: () => void } }) => {
    if (toastInstance) {
      const description = options?.description || (options?.action ? `${options.action.label} available` : undefined);
      return toastInstance.success(message, description);
    }
    console.warn('Toast not initialized. Make sure ToastProvider is mounted.');
  },
  warning: (message: string, options?: { description?: string; action?: { label: string; onClick: () => void } }) => {
    if (toastInstance) {
      const description = options?.description || (options?.action ? `${options.action.label} available` : undefined);
      return toastInstance.warning(message, description);
    }
    console.warn('Toast not initialized. Make sure ToastProvider is mounted.');
  },
  info: (message: string, options?: { description?: string; action?: { label: string; onClick: () => void } }) => {
    if (toastInstance) {
      const description = options?.description || (options?.action ? `${options.action.label} available` : undefined);
      return toastInstance.info(message, description);
    }
    console.warn('Toast not initialized. Make sure ToastProvider is mounted.');
  },
  error: (message: string, options?: { description?: string; action?: { label: string; onClick: () => void } }) => {
    if (toastInstance) {
      let description = options?.description;
      if (options?.action) {
        // For actions, we'll include it in the description and execute on click
        description = description 
          ? `${description} (Click to ${options.action.label.toLowerCase()})`
          : `Click to ${options.action.label.toLowerCase()}`;
        // Note: We can't directly execute the action, but we can show it in the description
        // In a real implementation, you might want to add action button support to AlertToast
      }
      return toastInstance.error(message, description);
    }
    console.warn('Toast not initialized. Make sure ToastProvider is mounted.');
  },
};
