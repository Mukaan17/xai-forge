import { Toaster as SonnerToaster } from 'sonner@2.0.3';

export function Toaster() {
  return (
    <SonnerToaster
      position="top-right"
      theme="dark"
      toastOptions={{
        style: {
          background: '#252540',
          border: '1px solid #3a3a5c',
          color: '#ffffff',
        },
        className: 'sonner-toast',
      }}
      richColors
    />
  );
}
