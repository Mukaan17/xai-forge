# Subplan 2: Repository Layer

## Objective
Create comprehensive repository interfaces for all new entities with custom query methods for efficient data access. This layer provides the data access abstraction for services.

## Prerequisites
- Subplan 1 completed (all entities exist)
- Spring Data JPA configured
- PostgreSQL database with all tables created

## Tasks

### 2.1 Create PredictionRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/PredictionRepository.java`
- Extends: `JpaRepository<Prediction, Long>`
- Methods: findByUserIdOrderByCreatedAtDesc, findByModelIdOrderByCreatedAtDesc, findByUserIdAndDateRange, findByUserIdAndModelIdOrderByCreatedAtDesc, findByIdAndUserId, countByUserId, countByModelId, countByUserIdSince, deleteByModelId, deleteByIdInAndUserId, getDailyPredictionCounts, getAverageConfidenceByModel, findOldPredictions

### 2.2 Create ApiKeyRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/ApiKeyRepository.java`
- Extends: `JpaRepository<ApiKey, Long>`
- Methods: findByKeyHash, findByUserIdOrderByCreatedAtDesc, findByUserIdAndActiveTrue, findByIdAndUserId, countByUserIdAndActiveTrue, existsByKeyHash, deactivateAllByUserId, updateLastUsed, findExpiredKeys

### 2.3 Create UserSessionRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/UserSessionRepository.java`
- Extends: `JpaRepository<UserSession, Long>`
- Methods: findByUserId, findByUserIdAndIsActiveTrue, findBySessionToken, findByIdAndUserId, deactivateAllByUserIdExcept, deactivateAllByUserId, findExpiredSessions, updateLastActiveAt

### 2.4 Create NotificationRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/NotificationRepository.java`
- Extends: `JpaRepository<Notification, Long>`
- Methods: findByUserIdOrderByCreatedAtDesc, findByUserIdAndIsReadFalseOrderByCreatedAtDesc, countByUserIdAndIsReadFalse, findByIdAndUserId, markAllAsReadByUserId, deleteOldNotifications, findByUserIdAndTypeOrderByCreatedAtDesc

### 2.5 Create UserPreferencesRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/UserPreferencesRepository.java`
- Extends: `JpaRepository<UserPreferences, Long>`
- Methods: findByUserId, existsByUserId

### 2.6 Create ActivityLogRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/ActivityLogRepository.java`
- Extends: `JpaRepository<ActivityLog, Long>`
- Methods: findByUserIdOrderByCreatedAtDesc, findByUserIdAndCreatedAtBetween, findByUserIdAndActionOrderByCreatedAtDesc, findRecentByUserId, countByUserIdAndAction, countByUserIdAndActionSince, deleteOldLogs, findByUserIdAndActionIn

### 2.7 Create WebhookRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/WebhookRepository.java`
- Extends: `JpaRepository<Webhook, Long>`
- Methods: findByUserIdOrderByCreatedAtDesc, findByUserIdAndActiveTrue, findByIdAndUserId, findActiveWebhooksForEvent, findAutoDisabledWebhooks, countByUserId

### 2.8 Create ExportJobRepository
**File**: `backend/src/main/java/com/example/xaiapp/repository/ExportJobRepository.java`
- Extends: `JpaRepository<ExportJob, Long>`
- Methods: findByUserIdOrderByCreatedAtDesc, findByIdAndUserId, findPendingJobs, findExpiredJobs, findByUserIdAndStatus

### 2.9 Update Existing Repositories
**Files**: 
- `UserRepository.java` - Add findByEmail, existsByEmail
- `DatasetRepository.java` - Add countByUserIdAndDeletedFalse, findByUserIdAndDeletedFalse, countByUserIdAndCreatedAtAfter, getTotalFileSizeByUserId
- `MLModelRepository.java` - Add countByUserIdAndStatusNot, countByUserIdAndStatus, countByUserIdAndModelType, countByUserIdAndCreatedAtAfter, findByUserIdAndStatusIn, findTopByUserIdOrderByCreatedAtDesc, getAverageAccuracyByUserId, getTotalModelSizeByUserId, findByUserIdAndBaseNameOrderByVersionDesc, findByUserIdAndBaseNameOrderByVersionAsc

## Validation Checklist
- [ ] All repositories extend JpaRepository correctly
- [ ] All custom @Query methods use proper JPQL/SQL syntax
- [ ] All @Modifying queries have @Transactional in service layer
- [ ] Pagination works correctly
- [ ] JSONB queries work (for events, permissions, metadata)
- [ ] Date range queries work
- [ ] Count queries return correct values
- [ ] Delete queries cascade properly
- [ ] No N+1 query problems
- [ ] All methods tested with sample data

## Dependencies
- Subplan 1 (entities must exist)

## Next Subplan
Subplan 3: Service Layer & Business Logic (depends on this subplan)
