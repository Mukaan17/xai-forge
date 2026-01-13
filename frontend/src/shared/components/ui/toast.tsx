import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { AlertToast } from "./alert-toast";
import { setToastInstance } from "@/shared/lib/toast";

export type ToastVariant = "success" | "warning" | "info" | "error";
export type ToastStyleVariant = "default" | "filled";

export interface Toast {
  id: string;
  title: string;
  description?: string;
  variant: ToastVariant;
  styleVariant?: ToastStyleVariant;
  duration?: number;
}

export interface ToastContextType {
  toasts: Toast[];
  toast: (options: {
    title: string;
    description?: string;
    variant?: ToastVariant;
    styleVariant?: ToastStyleVariant;
    duration?: number;
  }) => string;
  success: (title: string, description?: string) => string;
  warning: (title: string, description?: string) => string;
  info: (title: string, description?: string) => string;
  error: (title: string, description?: string) => string;
  dismiss: (id: string) => void;
}

const ToastContext = React.createContext<ToastContextType | undefined>(undefined);

export function useToast() {
  const context = React.useContext(ToastContext);
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return context;
}

interface ToastProviderProps {
  children: React.ReactNode;
  position?: "top-left" | "top-right" | "bottom-left" | "bottom-right";
}

export function ToastProvider({ children, position = "top-right" }: ToastProviderProps) {
  const [toasts, setToasts] = React.useState<Toast[]>([]);

  const dismiss = React.useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const addToast = React.useCallback(
    (options: {
      title: string;
      description?: string;
      variant?: ToastVariant;
      styleVariant?: ToastStyleVariant;
      duration?: number;
    }) => {
      const id = Math.random().toString(36).substring(2, 9);
      const toast: Toast = {
        id,
        title: options.title,
        description: options.description,
        variant: options.variant || "info",
        styleVariant: options.styleVariant || "filled",
        duration: options.duration || 5000,
      };

      setToasts((prev) => [...prev, toast]);

      // Auto-dismiss after duration
      if (toast.duration > 0) {
        setTimeout(() => {
          dismiss(id);
        }, toast.duration);
      }

      return id;
    },
    [dismiss]
  );

  const success = React.useCallback(
    (title: string, description?: string) => {
      return addToast({ title, description, variant: "success" });
    },
    [addToast]
  );

  const warning = React.useCallback(
    (title: string, description?: string) => {
      return addToast({ title, description, variant: "warning" });
    },
    [addToast]
  );

  const info = React.useCallback(
    (title: string, description?: string) => {
      return addToast({ title, description, variant: "info" });
    },
    [addToast]
  );

  const error = React.useCallback(
    (title: string, description?: string) => {
      return addToast({ title, description, variant: "error" });
    },
    [addToast]
  );

  const value = React.useMemo(
    () => ({
      toasts,
      toast: addToast,
      success,
      warning,
      info,
      error,
      dismiss,
    }),
    [toasts, addToast, success, warning, info, error, dismiss]
  );

  const positionClasses = {
    "top-left": "top-4 left-4",
    "top-right": "top-4 right-4",
    "bottom-left": "bottom-4 left-4",
    "bottom-right": "bottom-4 right-4",
  };

  // Set the toast instance for direct imports
  React.useEffect(() => {
    setToastInstance(value);
  }, [value]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className={`fixed z-[100] flex flex-col gap-2 ${positionClasses[position]}`}
      >
        <AnimatePresence mode="popLayout">
          {toasts.map((toast) => (
            <AlertToast
              key={toast.id}
              title={toast.title}
              description={toast.description || ""}
              variant={toast.variant}
              styleVariant={toast.styleVariant || "filled"}
              onClose={() => dismiss(toast.id)}
            />
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
}
