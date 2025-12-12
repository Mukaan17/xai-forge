# Subplan 7: Frontend-Backend Connection

## Objective
Wire all existing Figma UI components to backend API endpoints, replace mock data with real API calls, and ensure complete end-to-end functionality. This is the final integration step.

## Prerequisites
- Subplan 6 completed (API integration infrastructure exists)
- Figma components exist in `overhaul/frontend-overhaul/` or similar
- All backend endpoints functional (Subplan 4)

## Tasks

### 7.1 Update Dashboard Component
**File**: `frontend/src/components/Dashboard.tsx`
- Replace mock data with React Query hooks
- Add loading states with Skeleton components
- Add error handling
- Use real data for KPIs, charts, activity feed

### 7.2 Update Datasets Component
**File**: `frontend/src/components/Datasets.tsx`
- Replace mock data with useQuery for datasets list
- Add mutations for delete and upload
- Add file upload with drag-and-drop (react-dropzone)
- Add pagination controls
- Add filtering and search
- Show real dataset status, metadata, quality scores

### 7.3 Update Model Training Component
**File**: `frontend/src/components/ModelTraining.tsx`
- Replace mock data with real dataset/column data
- Add mutation for training
- Add form validation
- Show real algorithm options
- Handle training start and redirect to model details
- Show training progress (polling)

### 7.4 Update Model Details Component
**File**: `frontend/src/components/ModelDetails.tsx`
- Replace mock data with real model data
- Add polling for training progress
- Add mutations for delete and archive
- Show real metrics, feature importance, confusion matrix
- Add prediction count, usage stats
- Show version history if applicable

### 7.5 Update Predictions Component
**File**: `frontend/src/components/Predictions.tsx`
- Replace mock data with real model data
- Build dynamic input form based on model features
- Add mutation for making predictions
- Show real prediction results with confidence
- Show LIME explanations
- Handle explanation visualization

### 7.6 Update Prediction History Component
**File**: `frontend/src/components/PredictionHistory.tsx`
- Replace mock data with real prediction history
- Add mutations for delete, bulk delete, regenerate explanation
- Add filters: model, date range, result
- Add export buttons (CSV/JSON)
- Show pagination
- Show detailed prediction view

### 7.7 Update User Profile Component
**File**: `frontend/src/components/UserProfile.tsx`
- Replace mock data with real profile data
- Add mutations for profile update, avatar upload, password change
- Add 2FA section with setup, verification, disable
- Show QR code for 2FA setup
- Handle avatar upload with preview

### 7.8 Update Settings Component
**File**: `frontend/src/components/Settings.tsx`
- Replace mock data with real settings data
- Add mutations for each section (preferences, sessions, API keys, webhooks)
- Add API key creation modal (show full key once)
- Add webhook creation/editing forms
- Show session management with device info
- Show login history

### 7.9 Update Notification Center Component
**File**: `frontend/src/components/NotificationCenter.tsx`
- Replace mock data with real notifications
- Add mutations for mark as read, mark all as read, delete
- Show unread count badge
- Handle notification clicks (navigate to actionUrl)
- Auto-refresh unread count
- Show notification types with icons

### 7.10 Update Navigation Component
**File**: `frontend/src/components/Navigation.tsx`
- Integrate with auth store, notifications store, UI store
- Replace mock navigation with real routes
- Show user avatar and name from auth store
- Show notification badge with real count
- Handle logout with API call

### 7.11 Update Model Comparison Component
**File**: `frontend/src/components/ModelComparison.tsx`
- Replace mock data with real comparison API
- Add model selection (multi-select, max 5)
- Show comparison table with metrics
- Show feature importance comparison
- Show recommendations
- Show performance trends

### 7.12 Update Activity Log Component
**File**: `frontend/src/components/ActivityLog.tsx`
- Replace mock data with real activity logs
- Add filters: action type, date range
- Add export button
- Show pagination
- Format activity entries with icons
- Show IP addresses (masked)
- Show device info

### 7.13 Update Export Component
**File**: `frontend/src/components/Export.tsx` (if separate component)
- Replace mock data with real export jobs
- Add mutation for creating export
- Show export job list
- Show progress for in-progress exports
- Add download button for completed exports
- Handle file download

### 7.14 Add Real-time Updates
- Add polling for training progress, export progress, unread notifications
- Use React Query's `refetchInterval` option
- Consider WebSocket for real-time (optional, future enhancement)

### 7.15 Add Error Boundaries
**File**: `frontend/src/components/ErrorBoundary.tsx`
- Create error boundary component
- Wrap main app routes
- Show user-friendly error messages
- Log errors to console/service

### 7.16 Add Loading States
- Replace mock loading with real loading states
- Use Skeleton components from UI library
- Show spinners for mutations
- Disable forms during submission

### 7.17 Add Toast Notifications
- Use Sonner or similar toast library
- Show success messages on mutations
- Show error messages on failures
- Show info messages for long operations

### 7.18 Test End-to-End
- Test all user flows
- Test error scenarios
- Test loading states
- Test pagination
- Test filtering
- Test file uploads/downloads

## Validation Checklist
- [ ] All components use real API data
- [ ] No mock data remaining
- [ ] All mutations work correctly
- [ ] Loading states show properly
- [ ] Error handling works
- [ ] Toast notifications show
- [ ] File uploads work
- [ ] File downloads work
- [ ] Pagination works
- [ ] Filtering works
- [ ] Real-time updates work (polling)
- [ ] Navigation works
- [ ] Authentication flow works
- [ ] All user flows tested
- [ ] No console errors
- [ ] No TypeScript errors
- [ ] Performance acceptable

## Dependencies
- Subplan 6 (API integration must exist)
- All Figma components exist
- Backend fully functional (Subplan 4)

## Completion
This is the final subplan. Once complete, the entire feature expansion is done.
