package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for audit logging all user actions.
 * Provides a complete audit trail for security and compliance.
 * 
 * All significant actions are logged including:
 * - Authentication events (login, logout, failed attempts)
 * - CRUD operations on datasets, models, predictions
 * - Settings changes
 * - API key operations
 * - Export operations
 */
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_user_id", columnList = "user_id"),
    @Index(name = "idx_activity_created", columnList = "user_id, created_at DESC"),
    @Index(name = "idx_activity_action", columnList = "user_id, action"),
    @Index(name = "idx_activity_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_activity_date_range", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who performed the action.
     * Can be null for system-generated events or failed login attempts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Type of action performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private ActionType action;

    /**
     * Type of resource affected (e.g., "DATASET", "MODEL", "USER")
     */
    @Column(name = "resource_type", length = 30)
    private String resourceType;

    /**
     * ID of the affected resource.
     */
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * Name of the resource for display purposes.
     * Stored at time of action since resource might be deleted later.
     */
    @Column(name = "resource_name", length = 200)
    private String resourceName;

    /**
     * Detailed description of the action.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Additional metadata about the action.
     * Structure varies by action type:
     * 
     * LOGIN: {"success": true, "method": "password"}
     * MODEL_TRAIN: {"algorithm": "LOGISTIC_REGRESSION", "features": [...], "accuracy": 0.89}
     * SETTINGS_CHANGE: {"changed": ["theme", "timezone"], "oldValues": {...}, "newValues": {...}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether the action was successful.
     */
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;

    /**
     * Error message if action failed.
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * IP address from which the action was performed.
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * User agent string from the request.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Parsed device/browser info for display.
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * Geographic location based on IP.
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Session ID if applicable.
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * API key ID if action was performed via API.
     */
    @Column(name = "api_key_id")
    private Long apiKeyId;

    /**
     * When the action occurred.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * How long the action took in milliseconds.
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * Action type enum covering all loggable actions.
     */
    public enum ActionType {
        // Authentication
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        PASSWORD_CHANGED,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED,
        
        // Datasets
        DATASET_UPLOADED,
        DATASET_DELETED,
        DATASET_UPDATED,
        
        // Models
        MODEL_TRAINING_STARTED,
        MODEL_TRAINING_COMPLETED,
        MODEL_TRAINING_FAILED,
        MODEL_DELETED,
        MODEL_ARCHIVED,
        MODEL_EXPORTED,
        
        // Predictions
        PREDICTION_MADE,
        PREDICTION_DELETED,
        EXPLANATION_GENERATED,
        
        // API Keys
        API_KEY_CREATED,
        API_KEY_REVOKED,
        API_KEY_USED,
        
        // Webhooks
        WEBHOOK_CREATED,
        WEBHOOK_DELETED,
        WEBHOOK_TRIGGERED,
        
        // Settings
        PROFILE_UPDATED,
        PREFERENCES_UPDATED,
        
        // Export
        EXPORT_REQUESTED,
        EXPORT_COMPLETED,
        EXPORT_DOWNLOADED,
        
        // Sessions
        SESSION_REVOKED,
        ALL_SESSIONS_REVOKED,
        
        // Account
        ACCOUNT_DELETED
    }
}
