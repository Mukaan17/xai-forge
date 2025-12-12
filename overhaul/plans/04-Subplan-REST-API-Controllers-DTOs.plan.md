# Subplan 4: REST API Controllers & DTOs

## Objective
Create comprehensive REST API layer with DTOs for request/response objects and controllers that expose all service functionality via HTTP endpoints with proper validation and documentation.

## Prerequisites
- Subplan 3 completed (all services exist)
- Spring Validation configured
- OpenAPI/Swagger configured (optional but recommended)
- Security annotations working

## Tasks

### 4.1 Create Request DTOs
**Directory**: `backend/src/main/java/com/example/xaiapp/dto/request/`
- UpdateProfileRequest, ChangePasswordRequest, CreateApiKeyRequest, UpdateWebhookRequest, CreateWebhookRequest, PredictionFilterRequest, FullExportRequest, TwoFactorVerifyRequest, DeleteAccountRequest, BulkDeleteRequest
- All with Jakarta Validation annotations

### 4.2 Create Response DTOs
**Directory**: `backend/src/main/java/com/example/xaiapp/dto/response/`
- UserProfileDTO, UserStatisticsDTO, DashboardSummaryDTO, PredictionDTO, PredictionDetailDTO, NotificationDTO, ApiKeyDTO, ApiKeyResponseDTO, SessionDTO, LoginHistoryDTO, ActivityLogDTO, WebhookDTO, WebhookTestResultDTO, ExportJobDTO, ModelComparisonDTO, ModelSummaryDTO, MetricComparisonDTO, ActivityFeedItemDTO, UsageTrendDTO, RecentModelDTO, QuickStatsDTO, TwoFactorSetupDTO, TwoFactorVerifyResponse, MessageResponse, UnreadCountResponse, BulkDeleteResponse, AvatarUploadResponse
- All with Lombok @Builder

### 4.3 Create UserProfileController
**File**: `backend/src/main/java/com/example/xaiapp/controller/UserProfileController.java`
- Base path: `/api/users`
- Endpoints: GET /me, PUT /me, POST /me/avatar, DELETE /me/avatar, GET /me/statistics, PUT /me/password, POST /me/2fa/enable, POST /me/2fa/verify, DELETE /me/2fa, DELETE /me

### 4.4 Create SessionController
**File**: `backend/src/main/java/com/example/xaiapp/controller/SessionController.java`
- Base path: `/api/sessions`
- Endpoints: GET /, DELETE /{sessionId}, DELETE /others, GET /history

### 4.5 Create NotificationController
**File**: `backend/src/main/java/com/example/xaiapp/controller/NotificationController.java`
- Base path: `/api/notifications`
- Endpoints: GET /, GET /unread-count, PUT /{notificationId}/read, PUT /read-all, DELETE /{notificationId}`

### 4.6 Create ApiKeyController
**File**: `backend/src/main/java/com/example/xaiapp/controller/ApiKeyController.java`
- Base path: `/api/keys`
- Endpoints: GET /, POST /, DELETE /{keyId}

### 4.7 Create PredictionController
**File**: `backend/src/main/java/com/example/xaiapp/controller/PredictionController.java`
- Base path: `/api/predictions`
- Endpoints: GET /, GET /{predictionId}, DELETE /{predictionId}, POST /bulk-delete, GET /export, POST /{predictionId}/re-explain

### 4.8 Create DashboardController
**File**: `backend/src/main/java/com/example/xaiapp/controller/DashboardController.java`
- Base path: `/api/dashboard`
- Endpoints: GET /summary, GET /recent-activity, GET /models-by-type, GET /usage-trend, GET /recent-models, GET /quick-stats

### 4.9 Create ModelComparisonController
**File**: `backend/src/main/java/com/example/xaiapp/controller/ModelComparisonController.java`
- Base path: `/api/models`
- Endpoints: POST /compare, GET /{id}/versions, GET /{id}/trend

### 4.10 Create SettingsController
**File**: `backend/src/main/java/com/example/xaiapp/controller/SettingsController.java`
- Base path: `/api/settings`
- Endpoints: GET /preferences, PUT /preferences, PUT /notifications, PUT /appearance, POST /reset, GET /storage, GET /retention, PUT /retention

### 4.11 Create ExportController
**File**: `backend/src/main/java/com/example/xaiapp/controller/ExportController.java`
- Base path: `/api/export`
- Endpoints: POST /full, GET /{jobId}/status, GET /{jobId}/download

### 4.12 Create ActivityLogController
**File**: `backend/src/main/java/com/example/xaiapp/controller/ActivityLogController.java`
- Base path: `/api/activity`
- Endpoints: GET /, GET /export

### 4.13 Create WebhookController
**File**: `backend/src/main/java/com/example/xaiapp/controller/WebhookController.java`
- Base path: `/api/webhooks`
- Endpoints: GET /, POST /, PUT /{id}, DELETE /{id}, POST /{id}/test

### 4.14 Create SearchController (Optional)
**File**: `backend/src/main/java/com/example/xaiapp/controller/SearchController.java`
- Base path: `/api/search`
- Endpoints: GET /?q={query}

### 4.15 Update Existing Controllers
- Update ModelController to save predictions and create notifications
- Update DatasetController to use new fields and create notifications

### 4.16 Create @CurrentUser Annotation
**File**: `backend/src/main/java/com/example/xaiapp/security/CurrentUser.java`
- Custom annotation for injecting current user from security context

### 4.17 Update GlobalExceptionHandler
- Add handlers for new exception types
- Add validation error handling
- Add file upload error handling

## Validation Checklist
- [ ] All DTOs have proper validation annotations
- [ ] All controllers have proper security annotations
- [ ] All endpoints documented with OpenAPI
- [ ] Error responses consistent
- [ ] Pagination works correctly
- [ ] File upload/download works
- [ ] Multipart requests handled
- [ ] Date/time parsing works
- [ ] JSON serialization correct
- [ ] No circular references in DTOs
- [ ] All endpoints tested manually or with integration tests

## Dependencies
- Subplan 3 (services must exist)
- Spring Validation
- OpenAPI/Swagger (optional)

## Next Subplan
Subplan 5: Security Enhancements (depends on this subplan)
