# Subplan 6: Frontend API Integration

## Objective
Establish frontend infrastructure for API communication including TypeScript types, API client, state management, and React Query setup. This prepares the frontend to connect to backend endpoints.

## Prerequisites
- Subplan 4 completed (REST API endpoints exist)
- React 18.2+ project setup
- TypeScript configured
- Vite or Create React App

## Tasks

### 6.1 Install Dependencies
**File**: `frontend/package.json`
- Add: axios, @tanstack/react-query, zustand, zod, @hookform/resolvers, date-fns, react-dropzone, sonner, clsx, tailwind-merge
- Add dev: @types/react, @types/react-dom

### 6.2 Create TypeScript Types
**File**: `frontend/src/types/index.ts`
- Define all TypeScript interfaces matching backend DTOs
- User, AuthResponse, LoginRequest, RegisterRequest
- Dataset, DatasetStatus, ColumnMetadata
- Model, ModelStatus, ModelType, Algorithm, FeatureImportance
- Prediction, MakePredictionRequest
- DashboardSummary, ActivityFeedItem, UsageTrend
- Notification, NotificationType, NotificationPriority
- ApiKey, ApiKeyEnvironment, CreateApiKeyRequest
- Session, LoginHistory, ActivityLog, Webhook, ExportJob
- UserPreferences, TwoFactorSetup
- PaginatedResponse, PaginationParams
- Common response types

### 6.3 Create API Client Base
**File**: `frontend/src/api/client.ts`
- Create axios instance with base URL
- Request interceptor: Add JWT token from localStorage
- Response interceptor: Handle 401, refresh token logic
- Token storage helpers
- File upload helper with progress callback
- File download helper

### 6.4 Create API Endpoint Modules
**Files**:
- `frontend/src/api/auth.ts` - login, register, logout, getCurrentUser, forgotPassword, resetPassword
- `frontend/src/api/datasets.ts` - getAll, getById, upload, update, delete, getPreview, getColumns, analyzeColumn
- `frontend/src/api/models.ts` - getAll, getById, train, getTrainingStatus, delete, archive, getReadyModels, compare, getVersions
- `frontend/src/api/predictions.ts` - getAll, getById, predict, delete, bulkDelete, reExplain, exportCsv, exportJson
- `frontend/src/api/dashboard.ts` - getSummary, getRecentActivity, getModelsByType, getUsageTrend, getRecentModels, getQuickStats
- `frontend/src/api/user.ts` - getProfile, updateProfile, uploadAvatar, deleteAvatar, changePassword, enable2FA, verify2FA, disable2FA, getPreferences, updatePreferences, deleteAccount
- `frontend/src/api/notifications.ts` - getAll, getUnreadCount, markAsRead, markAllAsRead, delete
- `frontend/src/api/sessions.ts` - getActiveSessions, revokeSession, revokeAllOtherSessions, getLoginHistory
- `frontend/src/api/apiKeys.ts` - getAll, create, revoke
- `frontend/src/api/webhooks.ts` - getAll, getById, create, update, delete, test
- `frontend/src/api/activity.ts` - getAll, getById, exportCsv
- `frontend/src/api/exports.ts` - getAll, requestExport, getStatus, download
- `frontend/src/api/index.ts` - Export all API modules

### 6.5 Create Zustand Stores
**Files**:
- `frontend/src/stores/authStore.ts` - user, isAuthenticated, isLoading, error, login, register, logout, fetchUser, setUser, clearError
- `frontend/src/stores/uiStore.ts` - sidebarCollapsed, notificationsPanelOpen, theme, toggleSidebar, setSidebarCollapsed, toggleNotificationsPanel, setNotificationsPanelOpen, setTheme
- `frontend/src/stores/notificationsStore.ts` - notifications, unreadCount, isLoading, fetchNotifications, fetchUnreadCount, markAsRead, markAllAsRead, deleteNotification
- `frontend/src/stores/index.ts` - Export all stores

### 6.6 Setup React Query
**File**: `frontend/src/main.tsx` or `frontend/src/App.tsx`
- Create QueryClient with default options
- Wrap app with QueryClientProvider

### 6.7 Create Utility Functions
**File**: `frontend/src/utils/index.ts`
- cn() - Tailwind class merge utility
- formatDate() - Date formatting (short, long, relative)
- formatNumber() - Number formatting
- formatPercentage() - Percentage formatting
- formatFileSize() - File size formatting
- formatStatus() - Status string formatting
- getStatusColor() - Get status color class
- getStatusBadgeClass() - Get status badge class
- formatAlgorithm() - Algorithm name formatting
- isValidEmail() - Email validation
- isValidPassword() - Password validation with error messages

### 6.8 Create Environment Configuration
**Files**:
- `frontend/.env.example` - All environment variables
- `frontend/.env.development` - Development config
- `frontend/.env.production` - Production config

### 6.9 Update Vite Config
**File**: `frontend/vite.config.ts`
- Configure proxy for `/api` to `http://localhost:8080`
- Add path alias: `@` -> `./src`
- Configure build options

### 6.10 Create React Router Setup
**File**: `frontend/src/App.tsx`
- Setup React Router with routes
- Create ProtectedRoute component
- Create MainLayout component with Navigation
- Define all routes

### 6.11 Create Auth Pages
**Files**:
- `frontend/src/pages/LoginPage.tsx` - Email/password form, 2FA code input (conditional), error handling
- `frontend/src/pages/RegisterPage.tsx` - Email/password/confirm password form, first name/last name (optional), password strength indicator

### 6.12 Create API Error Handling
**File**: `frontend/src/utils/errorHandler.ts`
- Centralized error handling
- Map HTTP errors to user-friendly messages
- Handle network errors
- Handle validation errors
- Show toast notifications

## Validation Checklist
- [ ] All TypeScript types match backend DTOs
- [ ] API client handles authentication correctly
- [ ] Token refresh works
- [ ] All API endpoints have corresponding functions
- [ ] Zustand stores work correctly
- [ ] React Query configured correctly
- [ ] Environment variables work
- [ ] Vite proxy works
- [ ] React Router setup correctly
- [ ] Auth pages functional
- [ ] Error handling works
- [ ] No TypeScript errors
- [ ] No console errors

## Dependencies
- Subplan 4 (REST API endpoints must exist)
- React 18.2+
- TypeScript
- Vite or CRA

## Next Subplan
Subplan 7: Frontend-Backend Connection (depends on this subplan)
