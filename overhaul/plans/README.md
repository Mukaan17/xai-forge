# XAI-Forge Feature Expansion Plans

This directory contains the master plan and all subplans for the complete feature expansion of XAI-Forge.

## Plan Structure

- **[00-MASTER-PLAN.md](./00-MASTER-PLAN.md)** - Overview and coordination of all subplans
- **[01-Subplan-Database-Schema-Entity-Foundation.md](./01-Subplan-Database-Schema-Entity-Foundation.md)** - Database schema and entity creation
- **[02-Subplan-Repository-Layer.md](./02-Subplan-Repository-Layer.md)** - Repository layer implementation
- **[03-Subplan-Service-Layer-Business-Logic.md](./03-Subplan-Service-Layer-Business-Logic.md)** - Service layer and business logic
- **[04-Subplan-REST-API-Controllers-DTOs.md](./04-Subplan-REST-API-Controllers-DTOs.md)** - REST API controllers and DTOs
- **[05-Subplan-Security-Enhancements.md](./05-Subplan-Security-Enhancements.md)** - Security enhancements (API keys, 2FA, sessions)
- **[06-Subplan-Frontend-API-Integration.md](./06-Subplan-Frontend-API-Integration.md)** - Frontend API integration infrastructure
- **[07-Subplan-Frontend-Backend-Connection.md](./07-Subplan-Frontend-Backend-Connection.md)** - Final frontend-backend connection

## Execution Order

The subplans must be executed in sequential order due to dependencies:

```
Subplan 1 (Foundation)
    ↓
Subplan 2 (Repositories)
    ↓
Subplan 3 (Services)
    ↓
Subplan 4 (Controllers & DTOs)
    ↓
    ├──→ Subplan 5 (Security)
    └──→ Subplan 6 (Frontend API)
            ↓
        Subplan 7 (Frontend Connection)
```

## Quick Start

1. Read the [Master Plan](./00-MASTER-PLAN.md) for overview
2. Start with [Subplan 1](./01-Subplan-Database-Schema-Entity-Foundation.md)
3. Complete each subplan in order
4. Test thoroughly before moving to the next subplan

## Estimated Timeline

- **Total**: 15-22 days
- **Subplan 1**: 2-3 days
- **Subplan 2**: 1-2 days
- **Subplan 3**: 4-5 days
- **Subplan 4**: 2-3 days
- **Subplan 5**: 2-3 days
- **Subplan 6**: 2-3 days
- **Subplan 7**: 2-3 days

## Key Features

### New Entities (9)
1. Prediction - Prediction history with explanations
2. ApiKey - Programmatic API access
3. UserSession - Session management
4. Notification - In-app notifications
5. UserPreferences - User settings
6. ActivityLog - Audit logging
7. Webhook - Event callbacks
8. ExportJob - Data exports
9. Enhanced User - Profile, 2FA, account management

### Enhanced Entities (3)
- Dataset - Metadata, quality scoring, soft delete
- MLModel - Comprehensive metrics, versioning, feature importance
- User - Profile fields, 2FA, relationships

### New Services (10+)
- UserProfileService, NotificationService, ApiKeyService
- PredictionHistoryService, SessionService, ActivityLogService
- ModelComparisonService, DashboardService
- WebhookService, DataExportService

### Security Features
- API Key authentication
- Two-factor authentication (TOTP)
- Session management
- Enhanced security configuration

### Frontend Integration
- TypeScript types
- API client with interceptors
- Zustand state management
- React Query integration
- Component-to-API wiring

## Notes

- All plans are detailed with specific file paths and method signatures
- Each subplan includes validation checklists
- Dependencies are clearly marked
- Test thoroughly after each subplan
- Maintain backward compatibility

## Related Documentation

- Original implementation guide: `../backend-overhaul/prompt.md`
- Frontend components: `../frontend-overhaul/`
