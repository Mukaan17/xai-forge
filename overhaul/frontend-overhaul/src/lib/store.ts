// Global state management for XAI-Forge
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface Dataset {
  id: string;
  name: string;
  size: string;
  rows: number;
  columns: number;
  uploadDate: string;
  status: 'processing' | 'ready' | 'error';
  type: string;
}

export interface Model {
  id: string;
  name: string;
  algorithm: string;
  dataset: string;
  accuracy: number;
  precision: number;
  recall: number;
  f1Score: number;
  trainedDate: string;
  status: 'training' | 'ready' | 'failed';
  trainingTime: string;
  features: number;
}

export interface Prediction {
  id: number;
  model: string;
  input: string;
  prediction: string;
  confidence: string;
  date: string;
  inputs: Record<string, any>;
  topFactors: string[];
}

export interface Notification {
  id: number;
  icon: string;
  color: string;
  title: string;
  message: string;
  detail: string;
  time: string;
  unread: boolean;
  actions: Array<{ label: string; page: string }>;
  category: string;
  date: string;
}

export interface Activity {
  date: string;
  events: Array<{
    time: string;
    icon: string;
    color: string;
    type: string;
    details: string;
  }>;
}

interface AppState {
  // Data
  datasets: Dataset[];
  models: Model[];
  predictions: Prediction[];
  notifications: Notification[];
  activities: Activity[];
  
  // UI State
  currentPage: string;
  showNotifications: boolean;
  showOnboarding: boolean;
  isUploading: boolean;
  isTraining: boolean;
  uploadProgress: number;
  trainingProgress: number;
  
  // User preferences
  theme: 'dark' | 'light' | 'system';
  accentColor: string;
  
  // Actions
  setCurrentPage: (page: string) => void;
  setShowNotifications: (show: boolean) => void;
  setShowOnboarding: (show: boolean) => void;
  addDataset: (dataset: Dataset) => void;
  deleteDataset: (id: string) => void;
  addModel: (model: Model) => void;
  deleteModel: (id: string) => void;
  addPrediction: (prediction: Prediction) => void;
  markNotificationRead: (id: number) => void;
  markAllNotificationsRead: () => void;
  setUploadProgress: (progress: number) => void;
  setTrainingProgress: (progress: number) => void;
  setIsUploading: (uploading: boolean) => void;
  setIsTraining: (training: boolean) => void;
  setTheme: (theme: 'dark' | 'light' | 'system') => void;
  setAccentColor: (color: string) => void;
}

// Initial mock data
const initialDatasets: Dataset[] = [
  {
    id: '1',
    name: 'customer_churn_2024.csv',
    size: '2.4 MB',
    rows: 15420,
    columns: 12,
    uploadDate: '2024-12-08',
    status: 'ready',
    type: 'Classification',
  },
  {
    id: '2',
    name: 'sales_revenue_q4.csv',
    size: '1.8 MB',
    rows: 8903,
    columns: 8,
    uploadDate: '2024-12-07',
    status: 'ready',
    type: 'Regression',
  },
  {
    id: '3',
    name: 'customer_segmentation.csv',
    size: '3.1 MB',
    rows: 22145,
    columns: 15,
    uploadDate: '2024-12-05',
    status: 'ready',
    type: 'Clustering',
  },
];

const initialModels: Model[] = [
  {
    id: '1',
    name: 'Churn Predictor v3',
    algorithm: 'Logistic Regression',
    dataset: 'customer_churn_2024.csv',
    accuracy: 89.2,
    precision: 0.88,
    recall: 0.87,
    f1Score: 0.87,
    trainedDate: '2024-12-08',
    status: 'ready',
    trainingTime: '18s',
    features: 12,
  },
  {
    id: '2',
    name: 'Revenue Forecaster',
    algorithm: 'Linear Regression',
    dataset: 'sales_revenue_q4.csv',
    accuracy: 92.1,
    precision: 0.91,
    recall: 0.93,
    f1Score: 0.92,
    trainedDate: '2024-12-07',
    status: 'ready',
    trainingTime: '24s',
    features: 8,
  },
  {
    id: '3',
    name: 'Risk Classifier',
    algorithm: 'Random Forest',
    dataset: 'customer_churn_2024.csv',
    accuracy: 86.5,
    precision: 0.85,
    recall: 0.84,
    f1Score: 0.85,
    trainedDate: '2024-12-06',
    status: 'ready',
    trainingTime: '32s',
    features: 12,
  },
];

const initialPredictions: Prediction[] = [
  {
    id: 1547,
    model: 'Churn Predictor',
    input: 'Age: 35, Tenure: 6',
    prediction: 'Will Churn',
    confidence: '87%',
    date: '2h ago',
    inputs: {
      age: 35,
      accountTenure: '6 months',
      monthlyCharges: '$95',
      contractType: 'Month-to-month',
      techSupport: 'No',
    },
    topFactors: [
      'Short tenure (+32%)',
      'High charges (+28%)',
      'Monthly contract (+21%)',
    ],
  },
];

const initialNotifications: Notification[] = [
  {
    id: 1,
    icon: 'BrainCircuit',
    color: 'text-secondary',
    title: 'Model Training Complete',
    message: '"Churn Predictor v3" finished training',
    detail: 'Accuracy: 89.2%',
    time: '2 hours ago',
    unread: true,
    actions: [
      { label: 'View Model', page: 'models-all' },
      { label: 'Make Prediction', page: 'predictions-new' },
    ],
    category: 'training',
    date: 'today',
  },
];

export const useStore = create<AppState>()(
  persist(
    (set) => ({
      // Initial state
      datasets: initialDatasets,
      models: initialModels,
      predictions: initialPredictions,
      notifications: initialNotifications,
      activities: [],
      currentPage: 'dashboard',
      showNotifications: false,
      showOnboarding: !localStorage.getItem('xai-forge-onboarding-completed'),
      isUploading: false,
      isTraining: false,
      uploadProgress: 0,
      trainingProgress: 0,
      theme: 'dark',
      accentColor: '#00d9ff',
      
      // Actions
      setCurrentPage: (page) => set({ currentPage: page }),
      setShowNotifications: (show) => set({ showNotifications: show }),
      setShowOnboarding: (show) => set({ showOnboarding: show }),
      
      addDataset: (dataset) => set((state) => ({
        datasets: [...state.datasets, dataset],
      })),
      
      deleteDataset: (id) => set((state) => ({
        datasets: state.datasets.filter((d) => d.id !== id),
      })),
      
      addModel: (model) => set((state) => ({
        models: [...state.models, model],
      })),
      
      deleteModel: (id) => set((state) => ({
        models: state.models.filter((m) => m.id !== id),
      })),
      
      addPrediction: (prediction) => set((state) => ({
        predictions: [prediction, ...state.predictions],
      })),
      
      markNotificationRead: (id) => set((state) => ({
        notifications: state.notifications.map((n) =>
          n.id === id ? { ...n, unread: false } : n
        ),
      })),
      
      markAllNotificationsRead: () => set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, unread: false })),
      })),
      
      setUploadProgress: (progress) => set({ uploadProgress: progress }),
      setTrainingProgress: (progress) => set({ trainingProgress: progress }),
      setIsUploading: (uploading) => set({ isUploading: uploading }),
      setIsTraining: (training) => set({ isTraining: training }),
      setTheme: (theme) => set({ theme }),
      setAccentColor: (color) => set({ accentColor: color }),
    }),
    {
      name: 'xai-forge-storage',
      partialize: (state) => ({
        datasets: state.datasets,
        models: state.models,
        predictions: state.predictions,
        theme: state.theme,
        accentColor: state.accentColor,
      }),
    }
  )
);
