import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx', '.json'],
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    target: 'esnext',
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor chunks
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'query-vendor': ['@tanstack/react-query'],
          'ui-vendor': ['framer-motion', 'lucide-react'],
          'chart-vendor': ['recharts'],
          // Feature chunks
          'auth': [
            './src/features/auth/pages/LoginPage',
            './src/features/auth/pages/RegisterPage',
            './src/features/auth/pages/ForgotPasswordPage',
            './src/features/auth/pages/VerifyEmailPage',
          ],
          'dashboard': [
            './src/features/dashboard/pages/DashboardPage',
          ],
          'datasets': [
            './src/features/datasets/pages/DatasetsPage',
          ],
          'models': [
            './src/features/models/pages/ModelsPage',
            './src/features/models/pages/TrainModelPage',
            './src/features/models/pages/ModelDetailsPage',
          ],
          'predictions': [
            './src/features/predictions/pages/PredictionsPage',
            './src/features/predictions/pages/HistoryPage',
          ],
        },
      },
    },
    chunkSizeWarningLimit: 1000,
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});

