# Subplan 1: Database Schema & Entity Foundation

## Objective
Establish the complete database schema foundation by creating all new entities and enhancing existing ones. This is the foundation layer that all subsequent subplans depend on.

## Prerequisites
- Existing entities: User, Dataset, MLModel (basic versions)
- PostgreSQL 14+ database
- Spring Boot JPA/Hibernate configured
- Flyway or Liquibase for migrations

## Tasks

### 1.1 Create New Entity: Prediction
**File**: `backend/src/main/java/com/example/xaiapp/entity/Prediction.java`
- Fields: id, model (ManyToOne), user (ManyToOne), inputData (JSONB), predictionResult, confidence, explanation (JSONB), explanationSummary, predictionTimeMs, explanationTimeMs, createdAt
- Indexes: user_id, model_id, created_at, composite (user_id, created_at DESC)
- Relationships: ManyToOne to MLModel and User

### 1.2 Create New Entity: ApiKey
**File**: `backend/src/main/java/com/example/xaiapp/entity/ApiKey.java`
- Fields: id, user (ManyToOne), name, keyHash (SHA-256), keyPrefix, keySuffix, environment (enum), permissions (JSONB Set), active, expiresAt, lastUsedAt, lastUsedIp, usageCount, createdAt, description
- Indexes: user_id, key_hash (unique), composite (user_id, active)
- Enum: ApiKeyEnvironment (PRODUCTION, DEVELOPMENT, STAGING)
- Helper methods: hasPermission(), isValid()

### 1.3 Create New Entity: UserSession
**File**: `backend/src/main/java/com/example/xaiapp/entity/UserSession.java`
- Fields: id, user (ManyToOne), sessionToken (unique), refreshTokenHash, deviceInfo, userAgent, ipAddress, location, countryCode, isActive, lastActiveAt, expiresAt, revokedAt, revocationReason, createdAt
- Indexes: user_id, session_token (unique), composite (user_id, is_active)
- Helper methods: isValid(), revoke()

### 1.4 Create New Entity: Notification
**File**: `backend/src/main/java/com/example/xaiapp/entity/Notification.java`
- Fields: id, user (ManyToOne), type (enum), title, message, metadata (JSONB), isRead, readAt, priority (enum), actionUrl, actionLabel, expiresAt, createdAt
- Indexes: user_id, composite (user_id, is_read), composite (user_id, created_at DESC), composite (user_id, type)
- Enums: NotificationType, NotificationPriority
- Helper methods: markAsRead()

### 1.5 Create New Entity: UserPreferences
**File**: `backend/src/main/java/com/example/xaiapp/entity/UserPreferences.java`
- Fields: id, user (OneToOne), theme (enum), accentColor, displayDensity (enum), reduceMotion, highContrast, fontSizeMultiplier, emailNotifications (JSONB Map), inAppNotifications (JSONB Map), pushNotifications (JSONB Map), quietHoursEnabled, quietHoursStart, quietHoursEnd, timezone, defaultClassificationAlgorithm, defaultRegressionAlgorithm, autoDetectColumnTypes, autoExcludeIdColumns, defaultPreviewRows, predictionRetentionDays, failedTrainingRetentionDays, deletedDatasetRetentionDays, sidebarCollapsed, datasetView (enum), itemsPerPage, showOnboarding, updatedAt
- Enums: Theme, DisplayDensity, ViewType
- Helper methods: isEmailEnabledFor(), isInAppEnabledFor(), isInQuietHours()

### 1.6 Create New Entity: ActivityLog
**File**: `backend/src/main/java/com/example/xaiapp/entity/ActivityLog.java`
- Fields: id, user (ManyToOne nullable), action (enum), resourceType, resourceId, resourceName, description, metadata (JSONB), success, errorMessage, ipAddress, userAgent, deviceInfo, location, sessionId, apiKeyId, durationMs, createdAt
- Indexes: user_id, composite (user_id, created_at DESC), composite (user_id, action), composite (resource_type, resource_id), created_at
- Enum: ActionType (30+ action types)

### 1.7 Create New Entity: Webhook
**File**: `backend/src/main/java/com/example/xaiapp/entity/Webhook.java`
- Fields: id, user (ManyToOne), name, url, secret, events (JSONB Set), active, description, lastTriggeredAt, lastResponseCode, lastResponseBody, failureCount, successCount, autoDisabled, autoDisabledAt, createdAt, updatedAt
- Indexes: user_id, composite (user_id, active)
- Static class: Events (event type constants)
- Helper methods: recordSuccess(), recordFailure()

### 1.8 Create New Entity: ExportJob
**File**: `backend/src/main/java/com/example/xaiapp/entity/ExportJob.java`
- Fields: id, user (ManyToOne), status (enum), exportType (enum), includeItems (JSONB Set), format (enum), progress, currentStep, filePath, fileSizeBytes, errorMessage, metadata (JSONB), createdAt, startedAt, completedAt, expiresAt, downloadCount
- Indexes: user_id, composite (user_id, status)
- Enums: ExportStatus, ExportType, ExportFormat
- Helper methods: startProcessing(), complete(), fail(), updateProgress()

### 1.9 Enhance Entity: User
**File**: `backend/src/main/java/com/example/xaiapp/entity/User.java`
- Add fields: firstName, lastName, organization, role, location, bio, profileImageUrl, emailVerified, emailVerificationToken, emailVerificationExpires, twoFactorEnabled, twoFactorSecret, twoFactorBackupCodes, active, accountLocked, lockExpiresAt, failedLoginAttempts, lastLoginAt, passwordChangedAt, updatedAt
- Add relationships: OneToMany to Prediction, ApiKey, UserSession, Notification, ActivityLog, Webhook, ExportJob; OneToOne to UserPreferences
- Helper methods: getFullName(), getDisplayName(), recordFailedLogin(), recordSuccessfulLogin(), isCurrentlyLocked()

### 1.10 Enhance Entity: Dataset
**File**: `backend/src/main/java/com/example/xaiapp/entity/Dataset.java`
- Add fields: name, description, originalFilename, fileSizeBytes, mimeType, rowCount, columnCount, status (enum), processingError, columnMetadata (JSONB List), columnNames (JSONB List), targetColumn, recommendedTarget, inferredTaskType (enum), qualityScore, qualityIssues (JSONB List), sampleRows (JSONB List), deleted, deletedAt, processedAt, updatedAt
- Change: owner -> user (rename field, keep relationship)
- Change: headers -> columnNames (migrate data)
- Add relationship: OneToMany to MLModel (was OneToOne)
- Enums: DatasetStatus, TaskType
- Helper methods: getFormattedFileSize(), markReady(), markFailed(), softDelete()

### 1.11 Enhance Entity: MLModel
**File**: `backend/src/main/java/com/example/xaiapp/entity/MLModel.java`
- Add fields: name (rename from modelName), description, version, baseName, modelPath (rename from serializedModelPath), modelSizeBytes, status (enum), accuracy, precisionScore, recallScore, f1Score, mse, rmse, mae, r2Score, confusionMatrix (JSONB), classLabels (JSONB), featureImportance (JSONB Map), trainingHistory (JSONB List), trainingDurationMs, trainingSamples, testSamples, trainTestSplit, hyperparameters (JSONB Map), trainingError, trainingProgress, trainingStep, trainedAt, lastUsedAt, archivedAt, updatedAt, predictionCount
- Change: targetVariable -> targetColumn
- Change: featureNames -> featureColumns
- Change: dataset relationship from OneToOne to ManyToOne
- Enums: ModelStatus (add to existing ModelType)
- Helper methods: completeTraining(), failTraining(), archive(), updateProgress(), recordPrediction(), getFormattedTrainingDuration()

### 1.12 Create Database Migrations
**Files**: 
- `backend/src/main/resources/db/migration/V2__add_user_profile_fields.sql`
- `backend/src/main/resources/db/migration/V3__create_new_tables.sql`
- `backend/src/main/resources/db/migration/V4__update_datasets_models.sql`
- `backend/src/main/resources/db/migration/V5__create_user_preferences_defaults.sql`
- `backend/src/main/resources/db/migration/V6__add_foreign_key_constraints.sql`
- `backend/src/main/resources/db/migration/V7__add_check_constraints.sql`
- `backend/src/main/resources/db/migration/V8__create_indexes_for_performance.sql`
- `backend/src/main/resources/db/migration/V9__create_views_and_functions.sql`
- `backend/src/main/resources/db/migration/V10__seed_sample_data.sql` (optional)

### 1.13 Update Application Properties
**File**: `backend/src/main/resources/application.properties`
- Add: JSONB support configuration
- Add: Migration strategy (Flyway/Liquibase)
- Add: Entity scan paths

## Validation Checklist
- [ ] All 9 new entities compile without errors
- [ ] All 3 enhanced entities compile without errors
- [ ] All relationships properly defined (bidirectional where needed)
- [ ] All indexes created
- [ ] All enums defined
- [ ] Database migrations run successfully
- [ ] No data loss in existing tables
- [ ] Foreign key constraints work
- [ ] Check constraints validate data
- [ ] Performance indexes created
- [ ] Views and functions work

## Dependencies
- None (this is the foundation)

## Next Subplan
Subplan 2: Repository Layer (depends on this subplan)
