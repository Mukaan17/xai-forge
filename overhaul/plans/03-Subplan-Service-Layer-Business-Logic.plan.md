# Subplan 3: Service Layer & Business Logic

## Objective
Implement comprehensive service layer with business logic for all features. Services coordinate between repositories, handle transactions, implement business rules, and provide async operations where needed.

## Prerequisites
- Subplan 2 completed (all repositories exist)
- Spring Transaction Management configured
- Async support configured (@EnableAsync)

## Tasks

### 3.1 Create UserProfileService
**File**: `backend/src/main/java/com/example/xaiapp/service/UserProfileService.java`
- Methods: getUserProfile, updateProfile, uploadAvatar, deleteAvatar, getUserStatistics, changePassword, enable2FA, verify2FA, disable2FA, deleteAccount
- Features: File upload handling, password validation, 2FA setup, activity logging

### 3.2 Create NotificationService
**File**: `backend/src/main/java/com/example/xaiapp/service/NotificationService.java`
- Methods: createNotification, getNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification, notifyModelTrained, notifyModelFailed, notifyDatasetUploaded, notifySecurityAlert, notifyExportReady
- Features: Preference-based notification creation, email integration, quiet hours support, async email sending

### 3.3 Create ApiKeyService
**File**: `backend/src/main/java/com/example/xaiapp/service/ApiKeyService.java`
- Methods: generateApiKey, getApiKeys, revokeApiKey, validateApiKey, updateLastUsed, hasPermission
- Features: Secure key generation (SecureRandom), SHA-256 hashing, permission checking, usage tracking

### 3.4 Create PredictionHistoryService
**File**: `backend/src/main/java/com/example/xaiapp/service/PredictionHistoryService.java`
- Methods: savePrediction, getPredictions, getPrediction, getPredictionsByModel, deletePrediction, bulkDeletePredictions, exportPredictionsToCsv, exportPredictionsToJson, regenerateExplanation, getDailyPredictionStats
- Features: Filtering, pagination, CSV/JSON export, explanation regeneration, statistics

### 3.5 Create SessionService
**File**: `backend/src/main/java/com/example/xaiapp/service/SessionService.java`
- Methods: createSession, getActiveSessions, updateLastActive, revokeSession, revokeAllOtherSessions, revokeAllSessions, getLoginHistory, recordLoginAttempt, isSessionValid, cleanupExpiredSessions
- Features: Device parsing, IP geolocation, session tracking, security monitoring

### 3.6 Create ActivityLogService
**File**: `backend/src/main/java/com/example/xaiapp/service/ActivityLogService.java`
- Methods: logActivity, logActivityAsync, getActivityLogs, getActivityLogsByAction, getRecentActivity, exportActivityLogsToCsv, getActivityStatistics, deleteOldLogs
- Features: Automatic request context extraction, IP masking, CSV export, statistics

### 3.7 Create ModelComparisonService
**File**: `backend/src/main/java/com/example/xaiapp/service/ModelComparisonService.java`
- Methods: compareModels, getModelVersions, getPerformanceTrend
- Features: Metrics comparison, feature importance comparison, best model recommendation, trend analysis

### 3.8 Create DashboardService
**File**: `backend/src/main/java/com/example/xaiapp/service/DashboardService.java`
- Methods: getDashboardSummary, getRecentActivity, getModelsByType, getUsageTrend, getRecentModels, getQuickStats
- Features: KPI aggregation, activity feed formatting, trend calculation

### 3.9 Create WebhookService
**File**: `backend/src/main/java/com/example/xaiapp/service/WebhookService.java`
- Methods: createWebhook, getWebhooks, updateWebhook, deleteWebhook, testWebhook, triggerWebhooks, triggerUserWebhooks
- Features: HMAC-SHA256 signature generation, retry logic with exponential backoff, auto-disable on failures, async delivery

### 3.10 Create DataExportService
**File**: `backend/src/main/java/com/example/xaiapp/service/DataExportService.java`
- Methods: requestFullExport, getExportStatus, downloadExport, processExportAsync, cleanupExpiredExports
- Features: Async ZIP creation, progress tracking, multiple format support, expiration handling

### 3.11 Create Utility Services
**Files**:
- `GeoIpService.java` - IP geolocation
- `DeviceParser.java` - Parse User-Agent strings
- `TwoFactorAuthService.java` - TOTP generation/verification

### 3.12 Update Existing Services
**Files**:
- `ModelService.java` - Update to save predictions, create notifications, log activities
- `DatasetService.java` - Update to create notifications, log activities, use new Dataset fields

## Validation Checklist
- [ ] All services have @Service annotation
- [ ] All transactional methods have @Transactional
- [ ] All async methods have @Async
- [ ] Error handling with proper exceptions
- [ ] Activity logging integrated
- [ ] Notification creation integrated
- [ ] File upload/download works
- [ ] Async operations complete successfully
- [ ] Webhook delivery works
- [ ] Export generation works
- [ ] All business rules enforced
- [ ] No circular dependencies

## Dependencies
- Subplan 2 (repositories must exist)
- Spring Async configured
- RestTemplate bean configured
- Email service (optional, can be mocked)

## Next Subplan
Subplan 4: REST API Controllers & DTOs (depends on this subplan)
