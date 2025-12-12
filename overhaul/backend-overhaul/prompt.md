XAI-Forge Complete Feature Expansion
Ultra-Detailed Implementation Guide

PROJECT CONTEXT & CURRENT STATE
Repository: https://github.com/Mukaan17/xai-forge
Current Tech Stack:

Backend: Java 17, Spring Boot 3.2+, Spring Security 6.x (JWT), Spring Data JPA, PostgreSQL 14+, Tribuo 4.3.2 (Oracle Labs ML library)
Frontend: React 18.2+, Material-UI 5.x, Chart.js 4.x, Axios 1.x, React Router 6.x
Build: Maven 3.8+ (backend), npm/yarn (frontend)
Containerization: Docker, Docker Compose

Existing Design Patterns in Use:

Builder Pattern: PredictionResponseBuilder, TrainRequestBuilder
Factory Pattern: AlgorithmFactory, ModelFactory
Strategy Pattern: ClassificationStrategy, RegressionStrategy
Repository Pattern: Spring Data JPA repositories
DTO Pattern: Separate request/response objects

Existing Exception Hierarchy (11 exceptions):

XaiAppException (base)
ResourceNotFoundException
DatasetNotFoundException
ModelNotFoundException
InvalidDatasetException
ModelTrainingException
PredictionException
ExplanationException
AuthenticationException
AuthorizationException
FileStorageException

Current Project Structure:
xai-forge/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/xaiapp/
│       │   │   ├── XaiAppApplication.java
│       │   │   ├── builder/
│       │   │   │   ├── PredictionResponseBuilder.java
│       │   │   │   └── TrainRequestBuilder.java
│       │   │   ├── config/
│       │   │   │   ├── AsyncConfig.java
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   └── WebConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── DatasetController.java
│       │   │   │   └── ModelController.java
│       │   │   ├── dto/
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── AuthResponse.java
│       │   │   │   ├── DatasetDTO.java
│       │   │   │   ├── ModelDTO.java
│       │   │   │   ├── TrainRequest.java
│       │   │   │   ├── PredictRequest.java
│       │   │   │   └── ExplanationDTO.java
│       │   │   ├── entity/
│       │   │   │   ├── User.java
│       │   │   │   ├── Dataset.java
│       │   │   │   └── MLModel.java
│       │   │   ├── exception/
│       │   │   │   ├── XaiAppException.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   ├── DatasetNotFoundException.java
│       │   │   │   ├── ModelNotFoundException.java
│       │   │   │   ├── InvalidDatasetException.java
│       │   │   │   ├── ModelTrainingException.java
│       │   │   │   ├── PredictionException.java
│       │   │   │   ├── ExplanationException.java
│       │   │   │   ├── AuthenticationException.java
│       │   │   │   ├── AuthorizationException.java
│       │   │   │   ├── FileStorageException.java
│       │   │   │   └── GlobalExceptionHandler.java
│       │   │   ├── factory/
│       │   │   │   ├── AlgorithmFactory.java
│       │   │   │   └── ModelFactory.java
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── DatasetRepository.java
│       │   │   │   └── MLModelRepository.java
│       │   │   ├── security/
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   └── UserDetailsServiceImpl.java
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── DatasetService.java
│       │   │   │   ├── ModelService.java
│       │   │   │   └── XaiService.java
│       │   │   └── strategy/
│       │   │       ├── TrainingStrategy.java
│       │   │       ├── ClassificationStrategy.java
│       │   │       └── RegressionStrategy.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/example/xaiapp/
│               └── ... (50 test files)
│
├── frontend/
│   ├── package.json
│   ├── public/
│   └── src/
│       ├── index.js
│       ├── App.js
│       ├── api/
│       │   └── api.js
│       ├── components/
│       │   ├── auth/
│       │   │   ├── Login.jsx
│       │   │   └── Register.jsx
│       │   ├── dashboard/
│       │   │   ├── DatasetUpload.jsx
│       │   │   ├── ModelTrainer.jsx
│       │   │   ├── Predictor.jsx
│       │   │   └── XaiDisplay.jsx
│       │   └── layout/
│       │       └── Navbar.jsx
│       ├── contexts/
│       │   └── AuthContext.jsx
│       └── pages/
│           ├── HomePage.jsx
│           ├── LoginPage.jsx
│           ├── RegisterPage.jsx
│           └── DashboardPage.jsx
│
├── docker-compose.yml
├── Dockerfile
├── setup-database.sql
└── pom.xml (parent)

PHASE 1: DATABASE SCHEMA EXTENSIONS
1.1 New Entity: Prediction
File: backend/src/main/java/com/example/xaiapp/entity/Prediction.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a single prediction made by a trained model.
 * Stores the input data, prediction result, confidence score, and LIME explanation.
 * 
 * Each prediction is associated with:
 * - A trained ML model (required)
 * - The user who made the prediction (required)
 * 
 * The inputData and explanation fields are stored as JSONB in PostgreSQL
 * for efficient querying and flexible schema.
 */
@Entity
@Table(name = "predictions", indexes = {
    @Index(name = "idx_prediction_user_id", columnList = "user_id"),
    @Index(name = "idx_prediction_model_id", columnList = "model_id"),
    @Index(name = "idx_prediction_created_at", columnList = "created_at"),
    @Index(name = "idx_prediction_user_created", columnList = "user_id, created_at DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The model used to generate this prediction.
     * Lazy loaded to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private MLModel model;

    /**
     * The user who requested this prediction.
     * Used for access control and history queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The input feature values used for prediction.
     * Stored as JSON: {"feature1": value1, "feature2": value2, ...}
     * Values can be String, Number, or Boolean depending on feature type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> inputData;

    /**
     * The predicted value or class.
     * For classification: the predicted class label (e.g., "Will Churn", "Won't Churn")
     * For regression: the predicted numeric value as string (e.g., "125420.50")
     */
    @Column(name = "prediction_result", nullable = false, length = 500)
    private String predictionResult;

    /**
     * Confidence score of the prediction (0.0 to 1.0).
     * For classification: probability of the predicted class
     * For regression: R² score or similar confidence metric
     */
    @Column(name = "confidence", nullable = false)
    private Double confidence;

    /**
     * LIME explanation data stored as JSON.
     * Structure:
     * {
     *   "featureImportances": [
     *     {"feature": "age", "importance": 0.32, "direction": "positive", "value": 35},
     *     {"feature": "tenure", "importance": -0.18, "direction": "negative", "value": 6}
     *   ],
     *   "baseValue": 0.5,
     *   "predictionValue": 0.87,
     *   "summary": "Human-readable explanation text..."
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation", columnDefinition = "jsonb")
    private Map<String, Object> explanation;

    /**
     * Optional human-readable summary of the explanation.
     * Generated from the LIME results for quick display.
     */
    @Column(name = "explanation_summary", columnDefinition = "TEXT")
    private String explanationSummary;

    /**
     * Timestamp when the prediction was created.
     * Automatically set on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Time taken to generate the prediction in milliseconds.
     * Useful for performance monitoring.
     */
    @Column(name = "prediction_time_ms")
    private Long predictionTimeMs;

    /**
     * Time taken to generate the explanation in milliseconds.
     * Separate from prediction time as explanations are computationally expensive.
     */
    @Column(name = "explanation_time_ms")
    private Long explanationTimeMs;
}

1.2 New Entity: ApiKey
File: backend/src/main/java/com/example/xaiapp/entity/ApiKey.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing an API key for programmatic access to XAI-Forge.
 * 
 * Security considerations:
 * - The actual key is NEVER stored; only a SHA-256 hash is stored
 * - The key prefix (first 8 chars) is stored for identification in UI
 * - Full key is shown ONLY once at creation time
 * 
 * Key format: xai_{env}_sk_{32_random_chars}
 * Example: xai_live_sk_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_key_user_id", columnList = "user_id"),
    @Index(name = "idx_api_key_hash", columnList = "key_hash", unique = true),
    @Index(name = "idx_api_key_active", columnList = "user_id, active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this API key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Human-readable name for the key (e.g., "Production Server", "Development")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * SHA-256 hash of the full API key.
     * Used for validation during API requests.
     */
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    /**
     * First 12 characters of the key for display purposes.
     * Format: "xai_live_sk_" or "xai_test_sk_"
     * This allows users to identify which key is which without exposing the full key.
     */
    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    /**
     * Last 4 characters of the key for additional identification.
     */
    @Column(name = "key_suffix", nullable = false, length = 4)
    private String keySuffix;

    /**
     * Environment this key is intended for.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private ApiKeyEnvironment environment;

    /**
     * Set of permissions granted to this key.
     * Stored as JSON array: ["datasets:read", "datasets:write", "models:read", ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", columnDefinition = "jsonb", nullable = false)
    private Set<String> permissions;

    /**
     * Whether this key is currently active.
     * Revoked keys have active = false.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Optional expiration date for the key.
     * Null means the key never expires.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Timestamp of the last time this key was used.
     * Updated on each successful API request.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * IP address from which the key was last used.
     */
    @Column(name = "last_used_ip", length = 45)
    private String lastUsedIp;

    /**
     * Number of times this key has been used.
     * Useful for monitoring and analytics.
     */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Long usageCount = 0L;

    /**
     * Creation timestamp.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional description of what this key is used for.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Check if this key has a specific permission.
     */
    public boolean hasPermission(String permission) {
        if (permissions == null) return false;
        // Check for exact match or wildcard
        return permissions.contains(permission) || 
               permissions.contains("*") ||
               permissions.contains(permission.split(":")[0] + ":*");
    }

    /**
     * Check if this key is valid (active and not expired).
     */
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) return false;
        return true;
    }

    /**
     * API Key environment enum.
     */
    public enum ApiKeyEnvironment {
        PRODUCTION,
        DEVELOPMENT,
        STAGING
    }
}

1.3 New Entity: UserSession
File: backend/src/main/java/com/example/xaiapp/entity/UserSession.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an active user session.
 * Used for session management, security monitoring, and "active sessions" display.
 * 
 * A new session is created on each successful login.
 * Sessions can be revoked individually or in bulk.
 */
@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_session_user_id", columnList = "user_id"),
    @Index(name = "idx_session_token", columnList = "session_token", unique = true),
    @Index(name = "idx_session_active", columnList = "user_id, is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique session token (JWT jti claim or separate token).
     * Used to identify and revoke specific sessions.
     */
    @Column(name = "session_token", nullable = false, unique = true, length = 100)
    private String sessionToken;

    /**
     * Hash of the refresh token if using refresh token rotation.
     */
    @Column(name = "refresh_token_hash", length = 64)
    private String refreshTokenHash;

    /**
     * Device/browser information from User-Agent header.
     * Parsed and formatted for display (e.g., "Chrome on MacOS")
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * Raw User-Agent string for detailed analysis.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP address from which the session was created.
     * Supports both IPv4 and IPv6.
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * Approximate geographic location based on IP.
     * Format: "City, Country" (e.g., "New York, US")
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Country code for the location (e.g., "US", "GB")
     */
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /**
     * Whether this session is currently active.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether this is the session making the current request.
     * This is a transient field, not stored in database.
     */
    @Transient
    private Boolean isCurrentSession;

    /**
     * Timestamp of the last activity in this session.
     * Updated on each authenticated request.
     */
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    /**
     * When the session was created (login time).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the session expires.
     * Based on JWT expiration or session timeout policy.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * When the session was revoked (if revoked).
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Reason for revocation if revoked.
     */
    @Column(name = "revocation_reason", length = 100)
    private String revocationReason;

    /**
     * Check if session is valid (active and not expired).
     */
    public boolean isValid() {
        if (!isActive) return false;
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) return false;
        return true;
    }

    /**
     * Revoke this session.
     */
    public void revoke(String reason) {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
        this.revocationReason = reason;
    }
}

1.4 New Entity: Notification
File: backend/src/main/java/com/example/xaiapp/entity/Notification.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing a notification for a user.
 * Notifications are created by various system events and displayed in the UI.
 * 
 * Types of notifications:
 * - MODEL_TRAINED: Model training completed successfully
 * - MODEL_FAILED: Model training failed
 * - DATASET_UPLOADED: Dataset upload and processing completed
 * - DATASET_FAILED: Dataset processing failed
 * - SECURITY_ALERT: Security-related event (new login, failed attempts)
 * - WEEKLY_SUMMARY: Weekly usage summary
 * - EXPORT_READY: Data export is ready for download
 * - SYSTEM: System announcements
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_read", columnList = "user_id, is_read"),
    @Index(name = "idx_notification_created", columnList = "user_id, created_at DESC"),
    @Index(name = "idx_notification_type", columnList = "user_id, type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User to whom this notification belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of notification for categorization and filtering.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    /**
     * Short title for the notification.
     * E.g., "Model Training Complete", "Security Alert"
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Detailed message content.
     * E.g., '"Churn Predictor v3" finished training with 89.2% accuracy'
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Additional metadata as JSON.
     * Structure depends on notification type:
     * 
     * MODEL_TRAINED: {"modelId": 123, "modelName": "...", "accuracy": 0.89}
     * DATASET_UPLOADED: {"datasetId": 456, "datasetName": "...", "rowCount": 5000}
     * SECURITY_ALERT: {"eventType": "new_login", "ip": "...", "location": "..."}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Whether the user has read this notification.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * When the notification was read (null if unread).
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Priority level for sorting and display.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * Optional action URL to navigate when notification is clicked.
     * E.g., "/models/123" for model-related notifications
     */
    @Column(name = "action_url", length = 500)
    private String actionUrl;

    /**
     * Optional label for the action button.
     * E.g., "View Model", "Download Export"
     */
    @Column(name = "action_label", length = 50)
    private String actionLabel;

    /**
     * When the notification was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional expiration time after which notification is auto-deleted.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Mark notification as read.
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Notification types enum.
     */
    public enum NotificationType {
        MODEL_TRAINED,
        MODEL_FAILED,
        DATASET_UPLOADED,
        DATASET_FAILED,
        PREDICTION_COMPLETE,
        SECURITY_ALERT,
        WEEKLY_SUMMARY,
        EXPORT_READY,
        SYSTEM,
        API_KEY_CREATED,
        API_KEY_USED,
        STORAGE_WARNING
    }

    /**
     * Notification priority enum.
     */
    public enum NotificationPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}

1.5 New Entity: UserPreferences
File: backend/src/main/java/com/example/xaiapp/entity/UserPreferences.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * Entity storing user preferences for the application.
 * One-to-one relationship with User entity.
 * 
 * Includes:
 * - Appearance settings (theme, accent color, density)
 * - Notification preferences (email, in-app, push per event type)
 * - Default values for ML operations
 * - Data retention policies
 */
@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User these preferences belong to.
     * One-to-one relationship.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ==================== APPEARANCE SETTINGS ====================

    /**
     * Theme preference: DARK, LIGHT, or SYSTEM (follow OS setting)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    @Builder.Default
    private Theme theme = Theme.DARK;

    /**
     * Accent color for UI elements (hex code without #)
     */
    @Column(name = "accent_color", nullable = false, length = 6)
    @Builder.Default
    private String accentColor = "00d9ff"; // Electric teal

    /**
     * Display density: COMFORTABLE, DEFAULT, COMPACT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "display_density", nullable = false, length = 15)
    @Builder.Default
    private DisplayDensity displayDensity = DisplayDensity.DEFAULT;

    /**
     * Whether to reduce motion/animations
     */
    @Column(name = "reduce_motion", nullable = false)
    @Builder.Default
    private Boolean reduceMotion = false;

    /**
     * Whether to use high contrast mode
     */
    @Column(name = "high_contrast", nullable = false)
    @Builder.Default
    private Boolean highContrast = false;

    /**
     * Font size multiplier (1.0 = normal, 1.25 = large, etc.)
     */
    @Column(name = "font_size_multiplier", nullable = false)
    @Builder.Default
    private Double fontSizeMultiplier = 1.0;

    // ==================== NOTIFICATION SETTINGS ====================

    /**
     * Email notification preferences per event type.
     * Structure: {"MODEL_TRAINED": true, "MODEL_FAILED": true, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "email_notifications", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> emailNotifications = Map.of(
        "MODEL_TRAINED", true,
        "MODEL_FAILED", true,
        "DATASET_UPLOADED", false,
        "SECURITY_ALERT", true,
        "WEEKLY_SUMMARY", true,
        "EXPORT_READY", true
    );

    /**
     * In-app notification preferences per event type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "in_app_notifications", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> inAppNotifications = Map.of(
        "MODEL_TRAINED", true,
        "MODEL_FAILED", true,
        "DATASET_UPLOADED", true,
        "SECURITY_ALERT", true,
        "WEEKLY_SUMMARY", false,
        "EXPORT_READY", true
    );

    /**
     * Push notification preferences per event type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "push_notifications", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> pushNotifications = Map.of(
        "MODEL_TRAINED", false,
        "MODEL_FAILED", true,
        "DATASET_UPLOADED", false,
        "SECURITY_ALERT", true,
        "WEEKLY_SUMMARY", false,
        "EXPORT_READY", false
    );

    /**
     * Whether quiet hours are enabled.
     */
    @Column(name = "quiet_hours_enabled", nullable = false)
    @Builder.Default
    private Boolean quietHoursEnabled = false;

    /**
     * Start time for quiet hours.
     */
    @Column(name = "quiet_hours_start")
    @Builder.Default
    private LocalTime quietHoursStart = LocalTime.of(22, 0); // 10 PM

    /**
     * End time for quiet hours.
     */
    @Column(name = "quiet_hours_end")
    @Builder.Default
    private LocalTime quietHoursEnd = LocalTime.of(7, 0); // 7 AM

    /**
     * User's timezone for quiet hours calculation.
     */
    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "America/New_York";

    // ==================== ML DEFAULT SETTINGS ====================

    /**
     * Default algorithm for classification tasks.
     */
    @Column(name = "default_classification_algorithm", nullable = false, length = 50)
    @Builder.Default
    private String defaultClassificationAlgorithm = "LOGISTIC_REGRESSION";

    /**
     * Default algorithm for regression tasks.
     */
    @Column(name = "default_regression_algorithm", nullable = false, length = 50)
    @Builder.Default
    private String defaultRegressionAlgorithm = "LINEAR_REGRESSION";

    /**
     * Whether to auto-detect column types on upload.
     */
    @Column(name = "auto_detect_column_types", nullable = false)
    @Builder.Default
    private Boolean autoDetectColumnTypes = true;

    /**
     * Whether to automatically exclude ID-like columns.
     */
    @Column(name = "auto_exclude_id_columns", nullable = false)
    @Builder.Default
    private Boolean autoExcludeIdColumns = true;

    /**
     * Default number of rows to show in dataset preview.
     */
    @Column(name = "default_preview_rows", nullable = false)
    @Builder.Default
    private Integer defaultPreviewRows = 5;

    // ==================== DATA RETENTION SETTINGS ====================

    /**
     * Number of days to retain prediction history.
     * 0 = forever, -1 = delete immediately
     */
    @Column(name = "prediction_retention_days", nullable = false)
    @Builder.Default
    private Integer predictionRetentionDays = 90;

    /**
     * Number of days to retain failed training logs.
     */
    @Column(name = "failed_training_retention_days", nullable = false)
    @Builder.Default
    private Integer failedTrainingRetentionDays = 30;

    /**
     * Number of days deleted datasets can be recovered.
     */
    @Column(name = "deleted_dataset_retention_days", nullable = false)
    @Builder.Default
    private Integer deletedDatasetRetentionDays = 7;

    // ==================== UI PREFERENCES ====================

    /**
     * Whether the sidebar is collapsed by default.
     */
    @Column(name = "sidebar_collapsed", nullable = false)
    @Builder.Default
    private Boolean sidebarCollapsed = false;

    /**
     * Preferred view for datasets list: GRID or LIST.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_view", nullable = false, length = 10)
    @Builder.Default
    private ViewType datasetView = ViewType.GRID;

    /**
     * Number of items per page in tables.
     */
    @Column(name = "items_per_page", nullable = false)
    @Builder.Default
    private Integer itemsPerPage = 20;

    /**
     * Whether to show the onboarding wizard.
     */
    @Column(name = "show_onboarding", nullable = false)
    @Builder.Default
    private Boolean showOnboarding = true;

    // ==================== METADATA ====================

    /**
     * When preferences were last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== ENUMS ====================

    public enum Theme {
        DARK,
        LIGHT,
        SYSTEM
    }

    public enum DisplayDensity {
        COMFORTABLE,
        DEFAULT,
        COMPACT
    }

    public enum ViewType {
        GRID,
        LIST
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if email notifications are enabled for a specific event.
     */
    public boolean isEmailEnabledFor(String eventType) {
        return emailNotifications.getOrDefault(eventType, false);
    }

    /**
     * Check if in-app notifications are enabled for a specific event.
     */
    public boolean isInAppEnabledFor(String eventType) {
        return inAppNotifications.getOrDefault(eventType, true);
    }

    /**
     * Check if currently in quiet hours.
     */
    public boolean isInQuietHours() {
        if (!quietHoursEnabled) return false;
        LocalTime now = LocalTime.now(); // Should use timezone
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return !now.isBefore(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Handles overnight quiet hours (e.g., 10 PM to 7 AM)
            return !now.isBefore(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }
}

1.6 New Entity: ActivityLog
File: backend/src/main/java/com/example/xaiapp/entity/ActivityLog.java
javapackage com.example.xaiapp.entity;

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

1.7 New Entity: Webhook
File: backend/src/main/java/com/example/xaiapp/entity/Webhook.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing a webhook endpoint for event notifications.
 * Webhooks allow users to receive real-time HTTP callbacks when events occur.
 * 
 * Security:
 * - Each webhook has a unique secret for HMAC signature verification
 * - Payloads are signed with HMAC-SHA256
 * - Failed deliveries are retried with exponential backoff
 */
@Entity
@Table(name = "webhooks", indexes = {
    @Index(name = "idx_webhook_user_id", columnList = "user_id"),
    @Index(name = "idx_webhook_active", columnList = "user_id, active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this webhook.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Human-readable name for the webhook.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * URL to send webhook payloads to.
     * Must be HTTPS in production.
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /**
     * Secret key for HMAC signature generation.
     * Stored encrypted. Shown once at creation.
     */
    @Column(name = "secret", nullable = false, length = 100)
    private String secret;

    /**
     * Set of event types this webhook subscribes to.
     * E.g., ["model.trained", "model.failed", "dataset.uploaded"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "events", columnDefinition = "jsonb", nullable = false)
    private Set<String> events;

    /**
     * Whether this webhook is active.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Optional description of what this webhook is for.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * When the webhook was last triggered successfully.
     */
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    /**
     * HTTP status code from last delivery attempt.
     */
    @Column(name = "last_response_code")
    private Integer lastResponseCode;

    /**
     * Response body from last delivery (truncated).
     */
    @Column(name = "last_response_body", length = 1000)
    private String lastResponseBody;

    /**
     * Count of consecutive failures.
     * Reset to 0 on successful delivery.
     */
    @Column(name = "failure_count", nullable = false)
    @Builder.Default
    private Integer failureCount = 0;

    /**
     * Total number of successful deliveries.
     */
    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private Long successCount = 0L;

    /**
     * Whether the webhook is currently disabled due to failures.
     * Auto-disabled after max failures (e.g., 10 consecutive).
     */
    @Column(name = "auto_disabled", nullable = false)
    @Builder.Default
    private Boolean autoDisabled = false;

    /**
     * When the webhook was auto-disabled.
     */
    @Column(name = "auto_disabled_at")
    private LocalDateTime autoDisabledAt;

    /**
     * Creation timestamp.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Available webhook event types.
     */
    public static final class Events {
        public static final String MODEL_TRAINED = "model.trained";
        public static final String MODEL_FAILED = "model.failed";
        public static final String DATASET_UPLOADED = "dataset.uploaded";
        public static final String DATASET_FAILED = "dataset.failed";
        public static final String PREDICTION_MADE = "prediction.made";
        public static final String EXPORT_READY = "export.ready";
        
        public static final Set<String> ALL = Set.of(
            MODEL_TRAINED, MODEL_FAILED, DATASET_UPLOADED, 
            DATASET_FAILED, PREDICTION_MADE, EXPORT_READY
        );
    }

    /**
     * Record a successful delivery.
     */
    public void recordSuccess(int responseCode, String responseBody) {
        this.lastTriggeredAt = LocalDateTime.now();
        this.lastResponseCode = responseCode;
        this.lastResponseBody = truncate(responseBody, 1000);
        this.failureCount = 0;
        this.successCount++;
    }

    /**
     * Record a failed delivery.
     */
    public void recordFailure(int responseCode, String responseBody) {
        this.lastTriggeredAt = LocalDateTime.now();
        this.lastResponseCode = responseCode;
        this.lastResponseBody = truncate(responseBody, 1000);
        this.failureCount++;
        
        // Auto-disable after 10 consecutive failures
        if (this.failureCount >= 10 && !this.autoDisabled) {
            this.autoDisabled = true;
            this.autoDisabledAt = LocalDateTime.now();
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}

1.8 New Entity: ExportJob
File: backend/src/main/java/com/example/xaiapp/entity/ExportJob.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing an async data export job.
 * Large exports are processed in the background and users are notified when complete.
 */
@Entity
@Table(name = "export_jobs", indexes = {
    @Index(name = "idx_export_user_id", columnList = "user_id"),
    @Index(name = "idx_export_status", columnList = "user_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who requested the export.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Current status of the export job.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExportStatus status = ExportStatus.PENDING;

    /**
     * Type of export: FULL, DATASETS, MODELS, PREDICTIONS, ACTIVITY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "export_type", nullable = false, length = 20)
    private ExportType exportType;

    /**
     * What to include in the export.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "include_items", columnDefinition = "jsonb", nullable = false)
    private Set<String> includeItems;

    /**
     * Export format: ZIP, JSON, CSV
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 10)
    @Builder.Default
    private ExportFormat format = ExportFormat.ZIP;

    /**
     * Progress percentage (0-100).
     */
    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0;

    /**
     * Current step description for progress display.
     */
    @Column(name = "current_step", length = 200)
    private String currentStep;

    /**
     * Path to the generated export file.
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * Size of the export file in bytes.
     */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /**
     * Error message if export failed.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Additional metadata about the export.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * When the job was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When processing started.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    /**
     * When processing completed (success or failure).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * When the export file expires and should be deleted.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Number of times the file has been downloaded.
     */
    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    public enum ExportType {
        FULL,
        DATASETS,
        MODELS,
        PREDICTIONS,
        ACTIVITY
    }

    public enum ExportFormat {
        ZIP,
        JSON,
        CSV
    }

    /**
     * Start processing the export.
     */
    public void startProcessing() {
        this.status = ExportStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Mark export as completed.
     */
    public void complete(String filePath, long fileSize) {
        this.status = ExportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.filePath = filePath;
        this.fileSizeBytes = fileSize;
        this.progress = 100;
        this.expiresAt = LocalDateTime.now().plusDays(7); // Expire in 7 days
    }

    /**
     * Mark export as failed.
     */
    public void fail(String errorMessage) {
        this.status = ExportStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * Update progress.
     */
    public void updateProgress(int progress, String currentStep) {
        this.progress = progress;
        this.currentStep = currentStep;
    }
}

1.9 Update Existing Entity: User
File: backend/src/main/java/com/example/xaiapp/entity/User.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing a registered user of XAI-Forge.
 * 
 * UPDATED: Added profile fields, relationships to new entities,
 * 2FA support, and additional metadata.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== AUTHENTICATION FIELDS ====================

    /**
     * User's email address. Used for login and notifications.
     * Must be unique across all users.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt hashed password.
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * Whether the email has been verified.
     */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Email verification token (null after verification).
     */
    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    /**
     * When the verification token expires.
     */
    @Column(name = "email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    // ==================== TWO-FACTOR AUTHENTICATION ====================

    /**
     * Whether 2FA is enabled for this user.
     */
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    /**
     * Encrypted TOTP secret for 2FA.
     */
    @Column(name = "two_factor_secret", length = 100)
    private String twoFactorSecret;

    /**
     * Backup codes for 2FA recovery (comma-separated, hashed).
     */
    @Column(name = "two_factor_backup_codes", length = 500)
    private String twoFactorBackupCodes;

    // ==================== PROFILE FIELDS ====================

    /**
     * User's first name.
     */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /**
     * User's last name.
     */
    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * Organization or company name.
     */
    @Column(name = "organization", length = 200)
    private String organization;

    /**
     * Job title or role.
     */
    @Column(name = "role", length = 100)
    private String role;

    /**
     * Location (City, Country or similar).
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * Short bio or description.
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * URL to profile image.
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // ==================== ACCOUNT STATUS ====================

    /**
     * Whether the account is active.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Whether the account is locked (due to failed login attempts).
     */
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    /**
     * When the account lock expires.
     */
    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;

    /**
     * Number of consecutive failed login attempts.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    // ==================== TIMESTAMPS ====================

    /**
     * When the user registered.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the user profile was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When the user last logged in.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * When the password was last changed.
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * User's datasets.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Dataset> datasets = new ArrayList<>();

    /**
     * User's trained models.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MLModel> models = new ArrayList<>();

    /**
     * User's predictions.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Prediction> predictions = new ArrayList<>();

    /**
     * User's API keys.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApiKey> apiKeys = new ArrayList<>();

    /**
     * User's sessions.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSession> sessions = new ArrayList<>();

    /**
     * User's notifications.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    /**
     * User's preferences (one-to-one).
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserPreferences preferences;

    /**
     * User's activity logs.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityLog> activityLogs = new ArrayList<>();

    /**
     * User's webhooks.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Webhook> webhooks = new ArrayList<>();

    /**
     * User's export jobs.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExportJob> exportJobs = new ArrayList<>();

    // ==================== HELPER METHODS ====================

    /**
     * Get full name.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) return email;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    /**
     * Get display name (full name or email).
     */
    public String getDisplayName() {
        String fullName = getFullName();
        return fullName.equals(email) ? email.split("@")[0] : fullName;
    }

    /**
     * Record a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
            this.lockExpiresAt = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Record a successful login.
     */
    public void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockExpiresAt = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if account is currently locked.
     */
    public boolean isCurrentlyLocked() {
        if (!accountLocked) return false;
        if (lockExpiresAt != null && lockExpiresAt.isBefore(LocalDateTime.now())) {
            // Lock has expired
            this.accountLocked = false;
            this.lockExpiresAt = null;
            this.failedLoginAttempts = 0;
            return false;
        }
        return true;
    }
}

1.10 Update Existing Entity: Dataset
File: backend/src/main/java/com/example/xaiapp/entity/Dataset.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity representing an uploaded dataset.
 * 
 * UPDATED: Added comprehensive metadata, column information,
 * processing status, and statistics.
 */
@Entity
@Table(name = "datasets", indexes = {
    @Index(name = "idx_dataset_user_id", columnList = "user_id"),
    @Index(name = "idx_dataset_status", columnList = "user_id, status"),
    @Index(name = "idx_dataset_created", columnList = "user_id, created_at DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who uploaded this dataset.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Display name for the dataset.
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Optional description of the dataset.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Original filename as uploaded.
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Path to stored file on disk.
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    /**
     * MIME type of the file.
     */
    @Column(name = "mime_type", length = 100)
    @Builder.Default
    private String mimeType = "text/csv";

    /**
     * Number of rows in the dataset (excluding header).
     */
    @Column(name = "row_count")
    private Integer rowCount;

    /**
     * Number of columns in the dataset.
     */
    @Column(name = "column_count")
    private Integer columnCount;

    /**
     * Current processing status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DatasetStatus status = DatasetStatus.UPLOADING;

    /**
     * Error message if processing failed.
     */
    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    /**
     * Detailed column metadata.
     * Structure:
     * [
     *   {
     *     "name": "age",
     *     "type": "NUMERIC",
     *     "nullable": false,
     *     "uniqueValues": 50,
     *     "missingCount": 0,
     *     "missingPercentage": 0.0,
     *     "min": 18,
     *     "max": 85,
     *     "mean": 42.5,
     *     "median": 41,
     *     "stdDev": 12.3
     *   },
     *   {
     *     "name": "region",
     *     "type": "CATEGORICAL",
     *     "nullable": false,
     *     "uniqueValues": 4,
     *     "categories": ["North", "South", "East", "West"],
     *     "distribution": {"North": 0.25, "South": 0.30, ...}
     *   }
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "column_metadata", columnDefinition = "jsonb")
    private List<Map<String, Object>> columnMetadata;

    /**
     * List of column names in order.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "column_names", columnDefinition = "jsonb")
    private List<String> columnNames;

    /**
     * Target column if selected for training.
     */
    @Column(name = "target_column", length = 100)
    private String targetColumn;

    /**
     * Recommended target column based on analysis.
     */
    @Column(name = "recommended_target", length = 100)
    private String recommendedTarget;

    /**
     * Inferred task type based on target column.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "inferred_task_type", length = 20)
    private TaskType inferredTaskType;

    /**
     * Overall data quality score (0-100).
     */
    @Column(name = "quality_score")
    private Integer qualityScore;

    /**
     * Data quality issues found.
     * E.g., ["high_missing_values:column_x", "low_variance:column_y"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality_issues", columnDefinition = "jsonb")
    private List<String> qualityIssues;

    /**
     * Sample rows for preview (first 5-10 rows).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sample_rows", columnDefinition = "jsonb")
    private List<Map<String, Object>> sampleRows;

    /**
     * Whether the dataset is marked as deleted (soft delete).
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * When the dataset was soft deleted.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * When the upload started.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the dataset was last modified.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When processing completed.
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Models trained on this dataset.
     */
    @OneToMany(mappedBy = "dataset", fetch = FetchType.LAZY)
    private List<MLModel> models;

    /**
     * Dataset status enum.
     */
    public enum DatasetStatus {
        UPLOADING,
        PROCESSING,
        READY,
        ERROR,
        DELETED
    }

    /**
     * Task type enum.
     */
    public enum TaskType {
        CLASSIFICATION,
        REGRESSION,
        UNKNOWN
    }

    /**
     * Get formatted file size for display.
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) return "Unknown";
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        if (fileSizeBytes < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024));
        return String.format("%.1f GB", fileSizeBytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Mark as ready after processing.
     */
    public void markReady(int rowCount, int columnCount, List<Map<String, Object>> columnMetadata) {
        this.status = DatasetStatus.READY;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.columnMetadata = columnMetadata;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark as failed.
     */
    public void markFailed(String error) {
        this.status = DatasetStatus.ERROR;
        this.processingError = error;
    }

    /**
     * Soft delete the dataset.
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = DatasetStatus.DELETED;
    }
}

1.11 Update Existing Entity: MLModel
File: backend/src/main/java/com/example/xaiapp/entity/MLModel.java
javapackage com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a trained machine learning model.
 * 
 * UPDATED: Added comprehensive metrics, status tracking,
 * feature importance, and versioning support.
 */
@Entity
@Table(name = "ml_models", indexes = {
    @Index(name = "idx_model_user_id", columnList = "user_id"),
    @Index(name = "idx_model_dataset_id", columnList = "dataset_id"),
    @Index(name = "idx_model_status", columnList = "user_id, status"),
    @Index(name = "idx_model_created", columnList = "user_id, created_at DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who created this model.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Dataset used to train this model.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    /**
     * Display name for the model.
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Optional description.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Version number (for versioned models).
     */
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Base name for versioning (e.g., "Churn Predictor").
     */
    @Column(name = "base_name", length = 200)
    private String baseName;

    /**
     * Type of model: CLASSIFICATION or REGRESSION.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 20)
    private ModelType modelType;

    /**
     * Algorithm used: LOGISTIC_REGRESSION, LINEAR_REGRESSION, etc.
     */
    @Column(name = "algorithm", nullable = false, length = 50)
    private String algorithm;

    /**
     * Target column name.
     */
    @Column(name = "target_column", nullable = false, length = 100)
    private String targetColumn;

    /**
     * List of feature column names used for training.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_columns", columnDefinition = "jsonb", nullable = false)
    private List<String> featureColumns;

    /**
     * Current status of the model.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ModelStatus status = ModelStatus.TRAINING;

    /**
     * Path to serialized model file.
     */
    @Column(name = "model_path", length = 500)
    private String modelPath;

    /**
     * Size of the model file in bytes.
     */
    @Column(name = "model_size_bytes")
    private Long modelSizeBytes;

    // ==================== PERFORMANCE METRICS ====================

    /**
     * Overall accuracy (classification) or R² (regression).
     */
    @Column(name = "accuracy")
    private Double accuracy;

    /**
     * Precision (classification only).
     */
    @Column(name = "precision_score")
    private Double precisionScore;

    /**
     * Recall (classification only).
     */
    @Column(name = "recall_score")
    private Double recallScore;

    /**
     * F1 score (classification only).
     */
    @Column(name = "f1_score")
    private Double f1Score;

    /**
     * Mean Squared Error (regression only).
     */
    @Column(name = "mse")
    private Double mse;

    /**
     * Root Mean Squared Error (regression only).
     */
    @Column(name = "rmse")
    private Double rmse;

    /**
     * Mean Absolute Error (regression only).
     */
    @Column(name = "mae")
    private Double mae;

    /**
     * R² score (regression only).
     */
    @Column(name = "r2_score")
    private Double r2Score;

    /**
     * Confusion matrix (classification only).
     * Structure: [[TN, FP], [FN, TP]] or multi-class equivalent
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "confusion_matrix", columnDefinition = "jsonb")
    private List<List<Integer>> confusionMatrix;

    /**
     * Class labels for confusion matrix.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "class_labels", columnDefinition = "jsonb")
    private List<String> classLabels;

    /**
     * Feature importance scores.
     * Structure: {"feature1": 0.35, "feature2": 0.28, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feature_importance", columnDefinition = "jsonb")
    private Map<String, Double> featureImportance;

    /**
     * Detailed training metrics over time (for charts).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "training_history", columnDefinition = "jsonb")
    private List<Map<String, Object>> trainingHistory;

    // ==================== TRAINING METADATA ====================

    /**
     * Training duration in milliseconds.
     */
    @Column(name = "training_duration_ms")
    private Long trainingDurationMs;

    /**
     * Number of training samples used.
     */
    @Column(name = "training_samples")
    private Integer trainingSamples;

    /**
     * Number of test samples used.
     */
    @Column(name = "test_samples")
    private Integer testSamples;

    /**
     * Train/test split ratio used.
     */
    @Column(name = "train_test_split")
    @Builder.Default
    private Double trainTestSplit = 0.8;

    /**
     * Hyperparameters used for training.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hyperparameters", columnDefinition = "jsonb")
    private Map<String, Object> hyperparameters;

    /**
     * Error message if training failed.
     */
    @Column(name = "training_error", columnDefinition = "TEXT")
    private String trainingError;

    /**
     * Current training progress (0-100).
     */
    @Column(name = "training_progress")
    @Builder.Default
    private Integer trainingProgress = 0;

    /**
     * Current training step description.
     */
    @Column(name = "training_step", length = 200)
    private String trainingStep;

    // ==================== TIMESTAMPS ====================

    /**
     * When training was initiated.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When model was last updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When training completed (success or failure).
     */
    @Column(name = "trained_at")
    private LocalDateTime trainedAt;

    /**
     * When the model was last used for prediction.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * When the model was archived.
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    // ==================== USAGE STATS ====================

    /**
     * Total number of predictions made with this model.
     */
    @Column(name = "prediction_count", nullable = false)
    @Builder.Default
    private Long predictionCount = 0L;

    // ==================== RELATIONSHIPS ====================

    /**
     * Predictions made with this model.
     */
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prediction> predictions;

    // ==================== ENUMS ====================

    public enum ModelType {
        CLASSIFICATION,
        REGRESSION
    }

    public enum ModelStatus {
        TRAINING,
        READY,
        FAILED,
        ARCHIVED
    }

    // ==================== HELPER METHODS ====================

    /**
     * Mark training as complete with metrics.
     */
    public void completeTraining(Double accuracy, Long durationMs) {
        this.status = ModelStatus.READY;
        this.accuracy = accuracy;
        this.trainingDurationMs = durationMs;
        this.trainedAt = LocalDateTime.now();
        this.trainingProgress = 100;
    }

    /**
     * Mark training as failed.
     */
    public void failTraining(String error) {
        this.status = ModelStatus.FAILED;
        this.trainingError = error;
        this.trainedAt = LocalDateTime.now();
    }

    /**
     * Archive the model.
     */
    public void archive() {
        this.status = ModelStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * Update training progress.
     */
    public void updateProgress(int progress, String step) {
        this.trainingProgress = progress;
        this.trainingStep = step;
    }

    /**
     * Record a prediction.
     */
    public void recordPrediction() {
        this.predictionCount++;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Get formatted training duration.
     */
    public String getFormattedTrainingDuration() {
        if (trainingDurationMs == null) return "Unknown";
        if (trainingDurationMs < 1000) return trainingDurationMs + "ms";
        if (trainingDurationMs < 60000) return String.format("%.1fs", trainingDurationMs / 1000.0);
        return String.format("%.1fm", trainingDurationMs / 60000.0);
    }
}

PHASE 2: REPOSITORIES
2.1 PredictionRepository
File: backend/src/main/java/com/example/xaiapp/repository/PredictionRepository.java
javapackage com.example.xaiapp.repository;

import com.example.xaiapp.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Prediction entity operations.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * Find all predictions for a user, ordered by creation date.
     */
    Page<Prediction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all predictions for a specific model.
     */
    Page<Prediction> findByModelIdOrderByCreatedAtDesc(Long modelId, Pageable pageable);

    /**
     * Find predictions for a user within a date range.
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    Page<Prediction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find predictions for a user filtered by model.
     */
    Page<Prediction> findByUserIdAndModelIdOrderByCreatedAtDesc(
        Long userId, Long modelId, Pageable pageable
    );

    /**
     * Find a prediction by ID ensuring it belongs to the user.
     */
    Optional<Prediction> findByIdAndUserId(Long id, Long userId);

    /**
     * Count predictions for a user.
     */
    long countByUserId(Long userId);

    /**
     * Count predictions for a model.
     */
    long countByModelId(Long modelId);

    /**
     * Count predictions for a user in the last N days.
     */
    @Query("SELECT COUNT(p) FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt >= :since")
    long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Delete all predictions for a model.
     */
    @Modifying
    @Query("DELETE FROM Prediction p WHERE p.model.id = :modelId")
    void deleteByModelId(@Param("modelId") Long modelId);

    /**
     * Delete predictions by IDs for a user.
     */
    @Modifying
    @Query("DELETE FROM Prediction p WHERE p.id IN :ids AND p.user.id = :userId")
    int deleteByIdInAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * Get daily prediction counts for a user (for charts).
     */
    @Query("SELECT CAST(p.createdAt AS date) as date, COUNT(p) as count " +
           "FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt >= :since " +
           "GROUP BY CAST(p.createdAt AS date) " +
           "ORDER BY date")
    List<Object[]> getDailyPredictionCounts(
        @Param("userId") Long userId, 
        @Param("since") LocalDateTime since
    );

    /**
     * Get average confidence by model for a user.
     */
    @Query("SELECT p.model.id, AVG(p.confidence) FROM Prediction p " +
           "WHERE p.user.id = :userId GROUP BY p.model.id")
    List<Object[]> getAverageConfidenceByModel(@Param("userId") Long userId);

    /**
     * Find predictions older than a certain date (for cleanup).
     */
    @Query("SELECT p FROM Prediction p WHERE p.user.id = :userId " +
           "AND p.createdAt < :before")
    List<Prediction> findOldPredictions(
        @Param("userId") Long userId, 
        @Param("before") LocalDateTime before
    );
}

2.2 ApiKeyRepository
File: backend/src/main/java/com/example/xaiapp/repository/ApiKeyRepository.java
javapackage com.example.xaiapp.repository;

import com.example.xaiapp.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiKey entity operations.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find an API key by its hash.
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Find all API keys for a user.
     */
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all active API keys for a user.
     */
    List<ApiKey> findByUserIdAndActiveTrue(Long userId);

    /**
     * Find an API key by ID for a specific user.
     */
    Optional<ApiKey> findByIdAndUserId(Long id, Long userId);

    /**
     * Count active API keys for a user.
     */
    long countByUserIdAndActiveTrue(Long userId);

    /**
     * Check if a key hash exists.
     */
    boolean existsByKeyHash(String keyHash);

    /**
     * Deactivate all keys for a user.
     */
    @Modifying
    @Query("UPDATE ApiKey k SET k.active = false WHERE k.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    /**
     * Update last used timestamp and increment usage count.
     */
    @Modifying
    @Query("UPDATE ApiKey k SET k.lastUsedAt = :timestamp, k.lastUsedIp = :ip, " +
           "k.usageCount = k.usageCount + 1 WHERE k.id = :keyId")
    void updateLastUsed(
        @Param("keyId") Long keyId,
        @Param("timestamp") LocalDateTime timestamp,
        @Param("ip") String ip
    );

    /**
     * Find expired keys that are still active.
     */
    @Query("SELECT k FROM ApiKey k WHERE k.active = true " +
           "AND k.expiresAt IS NOT NULL AND k.expiresAt < :now")
    List<ApiKey> findExpiredKeys(@Param("now") LocalDateTime now);
}

2.3 Additional Repositories
Create the following repositories following the same pattern:
File: backend/src/main/java/com/example/xaiapp/repository/UserSessionRepository.java
java// Methods needed:
// - findByUserId(Long userId) -> List<UserSession>
// - findByUserIdAndIsActiveTrue(Long userId) -> List<UserSession>
// - findBySessionToken(String token) -> Optional<UserSession>
// - findByIdAndUserId(Long id, Long userId) -> Optional<UserSession>
// - deactivateAllByUserIdExcept(Long userId, Long exceptSessionId) -> void
// - deactivateAllByUserId(Long userId) -> void
// - findExpiredSessions(LocalDateTime now) -> List<UserSession>
// - updateLastActiveAt(Long sessionId, LocalDateTime timestamp) -> void
File: backend/src/main/java/com/example/xaiapp/repository/NotificationRepository.java
java// Methods needed:
// - findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) -> Page<Notification>
// - findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId) -> List<Notification>
// - countByUserIdAndIsReadFalse(Long userId) -> long
// - findByIdAndUserId(Long id, Long userId) -> Optional<Notification>
// - markAllAsReadByUserId(Long userId) -> void
// - deleteOldNotifications(Long userId, LocalDateTime before) -> void
// - findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable) -> Page<Notification>
File: backend/src/main/java/com/example/xaiapp/repository/UserPreferencesRepository.java
java// Methods needed:
// - findByUserId(Long userId) -> Optional<UserPreferences>
// - existsByUserId(Long userId) -> boolean
File: backend/src/main/java/com/example/xaiapp/repository/ActivityLogRepository.java
java// Methods needed:
// - findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) -> Page<ActivityLog>
// - findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable) -> Page<ActivityLog>
// - findByUserIdAndActionOrderByCreatedAtDesc(Long userId, ActionType action, Pageable pageable) -> Page<ActivityLog>
// - findRecentByUserId(Long userId, int limit) -> List<ActivityLog>
// - countByUserIdAndAction(Long userId, ActionType action) -> long
// - deleteOldLogs(Long userId, LocalDateTime before) -> void
File: backend/src/main/java/com/example/xaiapp/repository/WebhookRepository.java
java// Methods needed:
// - findByUserIdOrderByCreatedAtDesc(Long userId) -> List<Webhook>
// - findByUserIdAndActiveTrue(Long userId) -> List<Webhook>
// - findByIdAndUserId(Long id, Long userId) -> Optional<Webhook>
// - findActiveWebhooksForEvent(String event) -> List<Webhook>
// - findAutoDisabledWebhooks() -> List<Webhook>
File: backend/src/main/java/com/example/xaiapp/repository/ExportJobRepository.java
java// Methods needed:
// - findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) -> Page<ExportJob>
// - findByIdAndUserId(Long id, Long userId) -> Optional<ExportJob>
// - findPendingJobs() -> List<ExportJob>
// - findExpiredJobs(LocalDateTime now) -> List<ExportJob>
// - findByUserIdAndStatus(Long userId, ExportStatus status) -> List<ExportJob>

PHASE 3: SERVICES
3.1 UserProfileService
File: backend/src/main/java/com/example/xaiapp/service/UserProfileService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.request.*;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.UserPreferences;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for user profile management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final TwoFactorAuthService twoFactorAuthService;

    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_AVATAR_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * Get user profile by ID.
     * 
     * @param userId User ID
     * @return User profile DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = findUserById(userId);
        return mapToProfileDTO(user);
    }

    /**
     * Update user profile.
     * 
     * @param userId User ID
     * @param request Update request with new profile data
     * @return Updated user profile DTO
     */
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        // Update fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getOrganization() != null) {
            user.setOrganization(request.getOrganization().trim());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole().trim());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }

        User savedUser = userRepository.save(user);
        
        // Log the activity
        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.PROFILE_UPDATED,
            "USER",
            userId,
            user.getEmail(),
            "Profile updated",
            null
        );

        log.info("User profile updated: userId={}", userId);
        return mapToProfileDTO(savedUser);
    }

    /**
     * Upload user avatar image.
     * 
     * @param userId User ID
     * @param file Image file
     * @return URL to the uploaded avatar
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        // Validate file
        validateAvatarFile(file);

        User user = findUserById(userId);

        // Delete existing avatar if present
        if (user.getProfileImageUrl() != null) {
            deleteAvatarFile(user.getProfileImageUrl());
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR, userId.toString());

        try {
            // Create directory if not exists
            Files.createDirectories(uploadPath);

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Update user
            String avatarUrl = "/api/users/" + userId + "/avatar/" + filename;
            user.setProfileImageUrl(avatarUrl);
            userRepository.save(user);

            log.info("Avatar uploaded: userId={}, filename={}", userId, filename);
            return avatarUrl;

        } catch (IOException e) {
            log.error("Failed to upload avatar: userId={}", userId, e);
            throw new FileStorageException("Failed to upload avatar image", e);
        }
    }

    /**
     * Delete user avatar.
     * 
     * @param userId User ID
     */
    public void deleteAvatar(Long userId) {
        User user = findUserById(userId);

        if (user.getProfileImageUrl() != null) {
            deleteAvatarFile(user.getProfileImageUrl());
            user.setProfileImageUrl(null);
            userRepository.save(user);
            log.info("Avatar deleted: userId={}", userId);
        }
    }

    /**
     * Get user statistics (counts and metrics).
     * 
     * @param userId User ID
     * @return User statistics DTO
     */
    @Transactional(readOnly = true)
    public UserStatisticsDTO getUserStatistics(Long userId) {
        findUserById(userId); // Verify user exists

        long datasetCount = datasetRepository.countByUserIdAndDeletedFalse(userId);
        long modelCount = modelRepository.countByUserIdAndStatusNot(userId, MLModel.ModelStatus.ARCHIVED);
        long predictionCount = predictionRepository.countByUserId(userId);
        
        // Calculate average accuracy of ready models
        Double avgAccuracy = modelRepository.getAverageAccuracyByUserId(userId);
        
        // Get counts for last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentPredictions = predictionRepository.countByUserIdSince(userId, thirtyDaysAgo);
        long recentModels = modelRepository.countByUserIdAndCreatedAtAfter(userId, thirtyDaysAgo);

        return UserStatisticsDTO.builder()
            .totalDatasets(datasetCount)
            .totalModels(modelCount)
            .totalPredictions(predictionCount)
            .averageModelAccuracy(avgAccuracy != null ? avgAccuracy : 0.0)
            .predictionsLast30Days(recentPredictions)
            .modelsTrainedLast30Days(recentModels)
            .build();
    }

    /**
     * Change user password.
     * 
     * @param userId User ID
     * @param request Password change request
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        // Validate new password
        validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Log activity
        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.PASSWORD_CHANGED,
            "USER",
            userId,
            user.getEmail(),
            "Password changed",
            null
        );

        log.info("Password changed: userId={}", userId);
    }

    /**
     * Enable two-factor authentication.
     * 
     * @param userId User ID
     * @return 2FA setup data including QR code
     */
    public TwoFactorSetupDTO enable2FA(Long userId) {
        User user = findUserById(userId);

        if (user.getTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is already enabled");
        }

        // Generate secret
        String secret = twoFactorAuthService.generateSecret();
        
        // Generate QR code data URI
        String qrCodeDataUri = twoFactorAuthService.generateQRCodeDataUri(secret, user.getEmail());
        
        // Generate backup codes
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();

        // Store secret temporarily (will be confirmed on verification)
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        return TwoFactorSetupDTO.builder()
            .secret(secret)
            .qrCodeDataUri(qrCodeDataUri)
            .backupCodes(backupCodes)
            .build();
    }

    /**
     * Verify and activate 2FA.
     * 
     * @param userId User ID
     * @param code Verification code from authenticator app
     * @return true if verification successful
     */
    public boolean verify2FA(Long userId, String code) {
        User user = findUserById(userId);

        if (user.getTwoFactorSecret() == null) {
            throw new ValidationException("Two-factor authentication setup not initiated");
        }

        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);

        if (valid) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);

            // Log activity
            activityLogService.logActivity(
                userId,
                ActivityLog.ActionType.TWO_FACTOR_ENABLED,
                "USER",
                userId,
                user.getEmail(),
                "Two-factor authentication enabled",
                null
            );

            log.info("2FA enabled: userId={}", userId);
        }

        return valid;
    }

    /**
     * Disable two-factor authentication.
     * 
     * @param userId User ID
     * @param code Verification code to confirm
     */
    public void disable2FA(Long userId, String code) {
        User user = findUserById(userId);

        if (!user.getTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is not enabled");
        }

        // Verify code before disabling
        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);

        if (!valid) {
            throw new ValidationException("Invalid verification code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorBackupCodes(null);
        userRepository.save(user);

        // Log activity
        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.TWO_FACTOR_DISABLED,
            "USER",
            userId,
            user.getEmail(),
            "Two-factor authentication disabled",
            null
        );

        log.info("2FA disabled: userId={}", userId);
    }

    /**
     * Delete user account.
     * 
     * @param userId User ID
     * @param password Password confirmation
     */
    public void deleteAccount(Long userId, String password) {
        User user = findUserById(userId);

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Password is incorrect");
        }

        // Log before deletion
        log.info("Account deletion initiated: userId={}, email={}", userId, user.getEmail());

        // Delete user (cascades to all related entities)
        userRepository.delete(user);

        log.info("Account deleted: userId={}", userId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserProfileDTO mapToProfileDTO(User user) {
        return UserProfileDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .organization(user.getOrganization())
            .role(user.getRole())
            .location(user.getLocation())
            .bio(user.getBio())
            .profileImageUrl(user.getProfileImageUrl())
            .emailVerified(user.getEmailVerified())
            .twoFactorEnabled(user.getTwoFactorEnabled())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("No file provided");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed (5MB)");
        }

        String contentType = file.getContentType();
        boolean validType = false;
        for (String allowedType : ALLOWED_AVATAR_TYPES) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }

        if (!validType) {
            throw new ValidationException("Invalid file type. Allowed: JPEG, PNG, GIF, WebP");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }

    private void deleteAvatarFile(String avatarUrl) {
        try {
            // Extract path from URL
            String relativePath = avatarUrl.replace("/api/users/", "")
                .replace("/avatar/", "/");
            Path filePath = Paths.get(AVATAR_UPLOAD_DIR, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete avatar file: {}", avatarUrl, e);
        }
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }

        // Check for at least one uppercase, one lowercase, one digit
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidationException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            );
        }
    }
}

3.2 NotificationService
File: backend/src/main/java/com/example/xaiapp/service/NotificationService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.response.NotificationDTO;
import com.example.xaiapp.entity.Notification;
import com.example.xaiapp.entity.Notification.NotificationPriority;
import com.example.xaiapp.entity.Notification.NotificationType;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.UserPreferences;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.NotificationRepository;
import com.example.xaiapp.repository.UserPreferencesRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing user notifications.
 * Handles creation, retrieval, and status updates for notifications.
 * Also handles email notifications based on user preferences.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final EmailService emailService; // Assume this exists

    /**
     * Create a new notification for a user.
     * This method checks user preferences before creating in-app notifications
     * and optionally sends email notifications.
     * 
     * @param userId User ID
     * @param type Notification type
     * @param title Notification title
     * @param message Notification message
     * @param metadata Additional metadata
     * @return Created notification
     */
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata) {
        
        return createNotification(userId, type, title, message, metadata, 
            NotificationPriority.NORMAL, null, null);
    }

    /**
     * Create a notification with full options.
     */
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            NotificationPriority priority,
            String actionUrl,
            String actionLabel) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Check if in-app notification is enabled for this type
        UserPreferences prefs = preferencesRepository.findByUserId(userId).orElse(null);
        boolean inAppEnabled = prefs == null || prefs.isInAppEnabledFor(type.name());

        Notification notification = null;

        if (inAppEnabled) {
            notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .metadata(metadata)
                .priority(priority)
                .actionUrl(actionUrl)
                .actionLabel(actionLabel)
                .isRead(false)
                .build();

            notification = notificationRepository.save(notification);
            log.debug("Notification created: userId={}, type={}, title={}", userId, type, title);
        }

        // Check if email notification should be sent
        boolean emailEnabled = prefs != null && prefs.isEmailEnabledFor(type.name());
        boolean inQuietHours = prefs != null && prefs.isInQuietHours();

        if (emailEnabled && !inQuietHours && priority != NotificationPriority.LOW) {
            sendEmailNotificationAsync(user, type, title, message, metadata);
        }

        return notification;
    }

    /**
     * Get paginated notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get unread notification count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark a notification as read.
     */
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.markAsRead();
        notificationRepository.save(notification);
        
        log.debug("Notification marked as read: id={}", notificationId);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        log.debug("All notifications marked as read: userId={}", userId);
    }

    /**
     * Delete a notification.
     */
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
        log.debug("Notification deleted: id={}", notificationId);
    }

    /**
     * Create notification for model training completion.
     */
    public void notifyModelTrained(Long userId, Long modelId, String modelName, Double accuracy) {
        createNotification(
            userId,
            NotificationType.MODEL_TRAINED,
            "Model Training Complete",
            String.format("\"%s\" finished training with %.1f%% accuracy", modelName, accuracy * 100),
            Map.of("modelId", modelId, "modelName", modelName, "accuracy", accuracy),
            NotificationPriority.NORMAL,
            "/models/" + modelId,
            "View Model"
        );
    }

    /**
     * Create notification for model training failure.
     */
    public void notifyModelFailed(Long userId, Long modelId, String modelName, String error) {
        createNotification(
            userId,
            NotificationType.MODEL_FAILED,
            "Training Failed",
            String.format("\"%s\" encountered an error: %s", modelName, truncate(error, 100)),
            Map.of("modelId", modelId, "modelName", modelName, "error", error),
            NotificationPriority.HIGH,
            "/models/" + modelId,
            "View Details"
        );
    }

    /**
     * Create notification for dataset upload completion.
     */
    public void notifyDatasetUploaded(Long userId, Long datasetId, String datasetName, 
                                       int rowCount, int columnCount) {
        createNotification(
            userId,
            NotificationType.DATASET_UPLOADED,
            "Dataset Upload Complete",
            String.format("\"%s\" processed successfully (%d rows, %d features)", 
                datasetName, rowCount, columnCount),
            Map.of("datasetId", datasetId, "datasetName", datasetName, 
                   "rowCount", rowCount, "columnCount", columnCount),
            NotificationPriority.NORMAL,
            "/datasets/" + datasetId,
            "View Dataset"
        );
    }

    /**
     * Create security alert notification.
     */
    public void notifySecurityAlert(Long userId, String eventType, String description, 
                                     Map<String, Object> details) {
        createNotification(
            userId,
            NotificationType.SECURITY_ALERT,
            "Security Alert",
            description,
            Map.of("eventType", eventType, "details", details),
            NotificationPriority.HIGH,
            "/settings/security",
            "Review Activity"
        );
    }

    /**
     * Create export ready notification.
     */
    public void notifyExportReady(Long userId, Long exportJobId, String exportType) {
        createNotification(
            userId,
            NotificationType.EXPORT_READY,
            "Export Ready",
            String.format("Your %s export is ready for download", exportType.toLowerCase()),
            Map.of("exportJobId", exportJobId, "exportType", exportType),
            NotificationPriority.NORMAL,
            "/settings/export/" + exportJobId,
            "Download Export"
        );
    }

    // ==================== PRIVATE HELPER METHODS ====================

    @Async
    protected void sendEmailNotificationAsync(User user, NotificationType type, 
                                              String title, String message, 
                                              Map<String, Object> metadata) {
        try {
            emailService.sendNotificationEmail(user.getEmail(), type, title, message, metadata);
            log.debug("Email notification sent: userId={}, type={}", user.getId(), type);
        } catch (Exception e) {
            log.error("Failed to send email notification: userId={}, type={}", user.getId(), type, e);
        }
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .type(notification.getType().name())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .metadata(notification.getMetadata())
            .isRead(notification.getIsRead())
            .readAt(notification.getReadAt())
            .priority(notification.getPriority().name())
            .actionUrl(notification.getActionUrl())
            .actionLabel(notification.getActionLabel())
            .createdAt(notification.getCreatedAt())
            .build();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}

3.3 ApiKeyService
File: backend/src/main/java/com/example/xaiapp/service/ApiKeyService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.request.CreateApiKeyRequest;
import com.example.xaiapp.dto.response.ApiKeyDTO;
import com.example.xaiapp.dto.response.ApiKeyResponseDTO;
import com.example.xaiapp.dto.response.ApiKeyValidationResult;
import com.example.xaiapp.entity.ApiKey;
import com.example.xaiapp.entity.ApiKey.ApiKeyEnvironment;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.ApiKeyRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for API key management.
 * Handles generation, validation, and revocation of API keys.
 * 
 * Security:
 * - Keys are generated with cryptographically secure random bytes
 * - Only SHA-256 hash is stored, never the plain key
 * - Full key is returned ONLY once at creation time
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    private static final int KEY_LENGTH = 32; // 32 characters of random data
    private static final int MAX_KEYS_PER_USER = 10;
    private static final String KEY_PREFIX_LIVE = "xai_live_sk_";
    private static final String KEY_PREFIX_TEST = "xai_test_sk_";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new API key for a user.
     * 
     * @param userId User ID
     * @param request Key creation request
     * @return API key response including the full key (shown only once)
     */
    public ApiKeyResponseDTO generateApiKey(Long userId, CreateApiKeyRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check limit
        long existingKeys = apiKeyRepository.countByUserIdAndActiveTrue(userId);
        if (existingKeys >= MAX_KEYS_PER_USER) {
            throw new ValidationException(
                "Maximum number of API keys reached (" + MAX_KEYS_PER_USER + ")"
            );
        }

        // Validate permissions
        validatePermissions(request.getPermissions());

        // Generate key
        String prefix = request.getEnvironment() == ApiKeyEnvironment.PRODUCTION 
            ? KEY_PREFIX_LIVE : KEY_PREFIX_TEST;
        String randomPart = generateRandomString(KEY_LENGTH);
        String fullKey = prefix + randomPart;
        String keyHash = hashKey(fullKey);
        String keySuffix = randomPart.substring(randomPart.length() - 4);

        // Create entity
        ApiKey apiKey = ApiKey.builder()
            .user(user)
            .name(request.getName())
            .keyHash(keyHash)
            .keyPrefix(prefix)
            .keySuffix(keySuffix)
            .environment(request.getEnvironment())
            .permissions(request.getPermissions())
            .description(request.getDescription())
            .expiresAt(request.getExpiresAt())
            .build();

        apiKey = apiKeyRepository.save(apiKey);

        // Log activity
        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.API_KEY_CREATED,
            "API_KEY",
            apiKey.getId(),
            apiKey.getName(),
            "API key created: " + apiKey.getName(),
            Map.of("environment", request.getEnvironment().name())
        );

        log.info("API key created: userId={}, keyId={}, name={}", userId, apiKey.getId(), apiKey.getName());

        // Return with full key (only time it's shown)
        return ApiKeyResponseDTO.builder()
            .id(apiKey.getId())
            .name(apiKey.getName())
            .key(fullKey) // Full key - shown only once!
            .keyPreview(prefix + "..." + keySuffix)
            .environment(apiKey.getEnvironment().name())
            .permissions(apiKey.getPermissions())
            .expiresAt(apiKey.getExpiresAt())
            .createdAt(apiKey.getCreatedAt())
            .build();
    }

    /**
     * Get all API keys for a user (masked).
     */
    @Transactional(readOnly = true)
    public List<ApiKeyDTO> getApiKeys(Long userId) {
        return apiKeyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Revoke an API key.
     */
    public void revokeApiKey(Long userId, Long keyId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUserId(keyId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        // Log activity
        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.API_KEY_REVOKED,
            "API_KEY",
            keyId,
            apiKey.getName(),
            "API key revoked: " + apiKey.getName(),
            null
        );

        log.info("API key revoked: userId={}, keyId={}", userId, keyId);
    }

    /**
     * Validate an API key and return validation result.
     * Called by the API key authentication filter.
     */
    @Transactional(readOnly = true)
    public Optional<ApiKeyValidationResult> validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }

        String keyHash = hashKey(apiKey);
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHash(keyHash);

        if (keyOpt.isEmpty()) {
            return Optional.empty();
        }

        ApiKey key = keyOpt.get();

        if (!key.isValid()) {
            log.debug("API key validation failed: key is invalid or expired");
            return Optional.empty();
        }

        return Optional.of(ApiKeyValidationResult.builder()
            .keyId(key.getId())
            .userId(key.getUser().getId())
            .permissions(key.getPermissions())
            .environment(key.getEnvironment())
            .build());
    }

    /**
     * Update last used timestamp for an API key.
     * Called after successful API key authentication.
     */
    public void updateLastUsed(Long keyId, String ipAddress) {
        apiKeyRepository.updateLastUsed(keyId, LocalDateTime.now(), ipAddress);
    }

    /**
     * Check if an API key has a specific permission.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long keyId, String permission) {
        return apiKeyRepository.findById(keyId)
            .map(key -> key.hasPermission(permission))
            .orElse(false);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void validatePermissions(Set<String> permissions) {
        Set<String> validPermissions = Set.of(
            "datasets:read", "datasets:write", "datasets:delete",
            "models:read", "models:write", "models:delete",
            "predictions:read", "predictions:write",
            "*"
        );

        for (String permission : permissions) {
            if (!validPermissions.contains(permission)) {
                throw new ValidationException("Invalid permission: " + permission);
            }
        }
    }

    private ApiKeyDTO mapToDTO(ApiKey apiKey) {
        return ApiKeyDTO.builder()
            .id(apiKey.getId())
            .name(apiKey.getName())
            .keyPreview(apiKey.getKeyPrefix() + "..." + apiKey.getKeySuffix())
            .environment(apiKey.getEnvironment().name())
            .permissions(apiKey.getPermissions())
            .active(apiKey.getActive())
            .lastUsedAt(apiKey.getLastUsedAt())
            .usageCount(apiKey.getUsageCount())
            .expiresAt(apiKey.getExpiresAt())
            .createdAt(apiKey.getCreatedAt())
            .build();
    }
}

3.4 PredictionHistoryService
File: backend/src/main/java/com/example/xaiapp/service/PredictionHistoryService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.request.PredictionFilterRequest;
import com.example.xaiapp.dto.response.ExplanationDTO;
import com.example.xaiapp.dto.response.PredictionDTO;
import com.example.xaiapp.dto.response.PredictionDetailDTO;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.Prediction;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.MLModelRepository;
import com.example.xaiapp.repository.PredictionRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing prediction history.
 * Handles storage, retrieval, filtering, and export of predictions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PredictionHistoryService {

    private final PredictionRepository predictionRepository;
    private final MLModelRepository modelRepository;
    private final UserRepository userRepository;
    private final XaiService xaiService;
    private final ActivityLogService activityLogService;

    /**
     * Save a new prediction to history.
     * Called after a prediction is made.
     *
     * @param userId User ID
     * @param modelId Model ID
     * @param inputData Input feature values
     * @param predictionResult Prediction result
     * @param confidence Confidence score
     * @param explanation LIME explanation data
     * @param predictionTimeMs Time taken for prediction
     * @param explanationTimeMs Time taken for explanation
     * @return Saved prediction entity
     */
    public Prediction savePrediction(
            Long userId,
            Long modelId,
            Map<String, Object> inputData,
            String predictionResult,
            Double confidence,
            Map<String, Object> explanation,
            Long predictionTimeMs,
            Long explanationTimeMs) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MLModel model = modelRepository.findByIdAndUserId(modelId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Model not found"));

        // Generate human-readable summary from explanation
        String explanationSummary = generateExplanationSummary(explanation, predictionResult);

        Prediction prediction = Prediction.builder()
            .user(user)
            .model(model)
            .inputData(inputData)
            .predictionResult(predictionResult)
            .confidence(confidence)
            .explanation(explanation)
            .explanationSummary(explanationSummary)
            .predictionTimeMs(predictionTimeMs)
            .explanationTimeMs(explanationTimeMs)
            .build();

        prediction = predictionRepository.save(prediction);

        // Update model usage stats
        model.recordPrediction();
        modelRepository.save(model);

        log.debug("Prediction saved: id={}, modelId={}, result={}", 
            prediction.getId(), modelId, predictionResult);

        return prediction;
    }

    /**
     * Get predictions for a user with optional filters.
     *
     * @param userId User ID
     * @param filter Filter criteria
     * @param pageable Pagination
     * @return Page of predictions
     */
    @Transactional(readOnly = true)
    public Page<PredictionDTO> getPredictions(
            Long userId, 
            PredictionFilterRequest filter, 
            Pageable pageable) {

        Page<Prediction> predictions;

        if (filter.getModelId() != null) {
            // Filter by model
            predictions = predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(
                userId, filter.getModelId(), pageable);
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            // Filter by date range
            predictions = predictionRepository.findByUserIdAndDateRange(
                userId, filter.getStartDate(), filter.getEndDate(), pageable);
        } else {
            // No filter - get all
            predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return predictions.map(this::mapToDTO);
    }

    /**
     * Get detailed prediction by ID.
     */
    @Transactional(readOnly = true)
    public PredictionDetailDTO getPrediction(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));

        return mapToDetailDTO(prediction);
    }

    /**
     * Get predictions for a specific model.
     */
    @Transactional(readOnly = true)
    public Page<PredictionDTO> getPredictionsByModel(Long modelId, Pageable pageable) {
        return predictionRepository.findByModelIdOrderByCreatedAtDesc(modelId, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Delete a prediction.
     */
    public void deletePrediction(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));

        predictionRepository.delete(prediction);

        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.PREDICTION_DELETED,
            "PREDICTION",
            predictionId,
            null,
            "Prediction deleted",
            Map.of("modelId", prediction.getModel().getId())
        );

        log.info("Prediction deleted: id={}, userId={}", predictionId, userId);
    }

    /**
     * Bulk delete predictions.
     */
    public int bulkDeletePredictions(Long userId, List<Long> predictionIds) {
        int deleted = predictionRepository.deleteByIdInAndUserId(predictionIds, userId);

        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.PREDICTION_DELETED,
            "PREDICTION",
            null,
            null,
            "Bulk delete: " + deleted + " predictions",
            Map.of("count", deleted)
        );

        log.info("Bulk delete predictions: userId={}, count={}", userId, deleted);
        return deleted;
    }

    /**
     * Export predictions to CSV.
     *
     * @param userId User ID
     * @param filter Filter criteria
     * @return CSV content as byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportPredictionsToCsv(Long userId, PredictionFilterRequest filter) {
        // Get all matching predictions (no pagination for export)
        List<Prediction> predictions;

        if (filter.getModelId() != null) {
            predictions = predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(
                userId, filter.getModelId(), Pageable.unpaged()).getContent();
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            predictions = predictionRepository.findByUserIdAndDateRange(
                userId, filter.getStartDate(), filter.getEndDate(), Pageable.unpaged()).getContent();
        } else {
            predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();
        }

        // Generate CSV
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // Header
        writer.println("ID,Model,Prediction,Confidence,Created At,Input Data");

        // Data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Prediction p : predictions) {
            writer.printf("%d,\"%s\",\"%s\",%.4f,%s,\"%s\"%n",
                p.getId(),
                escapeCSV(p.getModel().getName()),
                escapeCSV(p.getPredictionResult()),
                p.getConfidence(),
                p.getCreatedAt().format(formatter),
                escapeCSV(p.getInputData().toString())
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    /**
     * Export predictions to JSON.
     */
    @Transactional(readOnly = true)
    public List<PredictionDetailDTO> exportPredictionsToJson(Long userId, PredictionFilterRequest filter) {
        List<Prediction> predictions;

        if (filter.getModelId() != null) {
            predictions = predictionRepository.findByUserIdAndModelIdOrderByCreatedAtDesc(
                userId, filter.getModelId(), Pageable.unpaged()).getContent();
        } else {
            predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();
        }

        return predictions.stream()
            .map(this::mapToDetailDTO)
            .collect(Collectors.toList());
    }

    /**
     * Regenerate explanation for an existing prediction.
     * Useful if explanation algorithm has been updated.
     */
    public ExplanationDTO regenerateExplanation(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndUserId(predictionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Prediction not found"));

        MLModel model = prediction.getModel();

        // Regenerate explanation using XAI service
        long startTime = System.currentTimeMillis();
        Map<String, Object> newExplanation = xaiService.generateExplanation(
            model, prediction.getInputData(), prediction.getPredictionResult());
        long explanationTime = System.currentTimeMillis() - startTime;

        // Update prediction
        prediction.setExplanation(newExplanation);
        prediction.setExplanationSummary(
            generateExplanationSummary(newExplanation, prediction.getPredictionResult()));
        prediction.setExplanationTimeMs(explanationTime);
        predictionRepository.save(prediction);

        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.EXPLANATION_GENERATED,
            "PREDICTION",
            predictionId,
            null,
            "Explanation regenerated",
            Map.of("modelId", model.getId())
        );

        log.info("Explanation regenerated: predictionId={}", predictionId);

        return mapToExplanationDTO(prediction);
    }

    /**
     * Get daily prediction statistics for charts.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyPredictionStats(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rawData = predictionRepository.getDailyPredictionCounts(userId, since);

        return rawData.stream()
            .map(row -> Map.<String, Object>of(
                "date", row[0].toString(),
                "count", ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String generateExplanationSummary(Map<String, Object> explanation, String predictionResult) {
        if (explanation == null || !explanation.containsKey("featureImportances")) {
            return "No explanation available.";
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> importances = 
            (List<Map<String, Object>>) explanation.get("featureImportances");

        if (importances == null || importances.isEmpty()) {
            return "No significant features identified.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("This prediction of \"").append(predictionResult)
               .append("\" is primarily influenced by:\n\n");

        // Top positive factors
        List<Map<String, Object>> positive = importances.stream()
            .filter(f -> "positive".equals(f.get("direction")))
            .sorted((a, b) -> Double.compare(
                Math.abs((Double) b.get("importance")),
                Math.abs((Double) a.get("importance"))))
            .limit(3)
            .collect(Collectors.toList());

        if (!positive.isEmpty()) {
            summary.append("Factors supporting this prediction:\n");
            for (Map<String, Object> factor : positive) {
                summary.append("• ").append(factor.get("feature"))
                       .append(" (").append(factor.get("value")).append(")\n");
            }
        }

        // Top negative factors
        List<Map<String, Object>> negative = importances.stream()
            .filter(f -> "negative".equals(f.get("direction")))
            .sorted((a, b) -> Double.compare(
                Math.abs((Double) b.get("importance")),
                Math.abs((Double) a.get("importance"))))
            .limit(3)
            .collect(Collectors.toList());

        if (!negative.isEmpty()) {
            summary.append("\nFactors against this prediction:\n");
            for (Map<String, Object> factor : negative) {
                summary.append("• ").append(factor.get("feature"))
                       .append(" (").append(factor.get("value")).append(")\n");
            }
        }

        return summary.toString();
    }

    private PredictionDTO mapToDTO(Prediction prediction) {
        return PredictionDTO.builder()
            .id(prediction.getId())
            .modelId(prediction.getModel().getId())
            .modelName(prediction.getModel().getName())
            .predictionResult(prediction.getPredictionResult())
            .confidence(prediction.getConfidence())
            .inputSummary(generateInputSummary(prediction.getInputData()))
            .createdAt(prediction.getCreatedAt())
            .build();
    }

    private PredictionDetailDTO mapToDetailDTO(Prediction prediction) {
        return PredictionDetailDTO.builder()
            .id(prediction.getId())
            .modelId(prediction.getModel().getId())
            .modelName(prediction.getModel().getName())
            .modelType(prediction.getModel().getModelType().name())
            .inputData(prediction.getInputData())
            .predictionResult(prediction.getPredictionResult())
            .confidence(prediction.getConfidence())
            .explanation(prediction.getExplanation())
            .explanationSummary(prediction.getExplanationSummary())
            .predictionTimeMs(prediction.getPredictionTimeMs())
            .explanationTimeMs(prediction.getExplanationTimeMs())
            .createdAt(prediction.getCreatedAt())
            .build();
    }

    private ExplanationDTO mapToExplanationDTO(Prediction prediction) {
        return ExplanationDTO.builder()
            .predictionId(prediction.getId())
            .predictionResult(prediction.getPredictionResult())
            .confidence(prediction.getConfidence())
            .explanation(prediction.getExplanation())
            .explanationSummary(prediction.getExplanationSummary())
            .generatedAt(LocalDateTime.now())
            .build();
    }

    private String generateInputSummary(Map<String, Object> inputData) {
        if (inputData == null || inputData.isEmpty()) {
            return "No input data";
        }

        return inputData.entrySet().stream()
            .limit(3)
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining(", "));
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}

3.5 SessionService
File: backend/src/main/java/com/example/xaiapp/service/SessionService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.response.LoginHistoryDTO;
import com.example.xaiapp.dto.response.SessionDTO;
import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.UserSession;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.ActivityLogRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.repository.UserSessionRepository;
import com.example.xaiapp.util.DeviceParser;
import com.example.xaiapp.util.GeoIpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user sessions.
 * Handles session creation, tracking, and revocation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final GeoIpService geoIpService;
    private final DeviceParser deviceParser;

    @Value("${app.session.expiration-hours:24}")
    private int sessionExpirationHours;

    /**
     * Create a new session after successful login.
     *
     * @param userId User ID
     * @param request HTTP request for extracting device info
     * @return Created session
     */
    public UserSession createSession(Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = deviceParser.parseUserAgent(userAgent);
        String location = geoIpService.getLocation(ipAddress);

        UserSession session = UserSession.builder()
            .user(user)
            .sessionToken(UUID.randomUUID().toString())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .countryCode(geoIpService.getCountryCode(ipAddress))
            .isActive(true)
            .lastActiveAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(sessionExpirationHours))
            .build();

        session = sessionRepository.save(session);

        log.info("Session created: userId={}, sessionId={}, device={}", 
            userId, session.getId(), deviceInfo);

        return session;
    }

    /**
     * Get all active sessions for a user.
     *
     * @param userId User ID
     * @param currentSessionToken Token of the current session (to mark as current)
     * @return List of sessions
     */
    @Transactional(readOnly = true)
    public List<SessionDTO> getActiveSessions(Long userId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveTrue(userId);

        return sessions.stream()
            .map(session -> {
                SessionDTO dto = mapToDTO(session);
                dto.setIsCurrentSession(session.getSessionToken().equals(currentSessionToken));
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Update last active timestamp for a session.
     */
    public void updateLastActive(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
            .ifPresent(session -> {
                session.setLastActiveAt(LocalDateTime.now());
                sessionRepository.save(session);
            });
    }

    /**
     * Revoke a specific session.
     */
    public void revokeSession(Long userId, Long sessionId) {
        UserSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.revoke("User requested");
        sessionRepository.save(session);

        log.info("Session revoked: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * Revoke all sessions except the current one.
     */
    public void revokeAllOtherSessions(Long userId, String currentSessionToken) {
        UserSession currentSession = sessionRepository.findBySessionToken(currentSessionToken)
            .orElseThrow(() -> new ResourceNotFoundException("Current session not found"));

        sessionRepository.deactivateAllByUserIdExcept(userId, currentSession.getId());

        log.info("All other sessions revoked: userId={}", userId);
    }

    /**
     * Revoke all sessions for a user (logout everywhere).
     */
    public void revokeAllSessions(Long userId) {
        sessionRepository.deactivateAllByUserId(userId);
        log.info("All sessions revoked: userId={}", userId);
    }

    /**
     * Get login history for a user.
     */
    @Transactional(readOnly = true)
    public List<LoginHistoryDTO> getLoginHistory(Long userId, int limit) {
        List<ActivityLog> loginActivities = activityLogRepository.findByUserIdAndActionIn(
            userId,
            List.of(ActivityLog.ActionType.LOGIN_SUCCESS, ActivityLog.ActionType.LOGIN_FAILED),
            limit
        );

        return loginActivities.stream()
            .map(this::mapToLoginHistoryDTO)
            .collect(Collectors.toList());
    }

    /**
     * Record a login attempt (for security monitoring).
     */
    public void recordLoginAttempt(String email, boolean success, HttpServletRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = deviceParser.parseUserAgent(userAgent);
        String location = geoIpService.getLocation(ipAddress);

        User user = userRepository.findByEmail(email).orElse(null);

        ActivityLog.ActionType action = success 
            ? ActivityLog.ActionType.LOGIN_SUCCESS 
            : ActivityLog.ActionType.LOGIN_FAILED;

        ActivityLog log = ActivityLog.builder()
            .user(user)
            .action(action)
            .resourceType("USER")
            .resourceId(user != null ? user.getId() : null)
            .description(success ? "Successful login" : "Failed login attempt")
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .success(success)
            .metadata(java.util.Map.of("email", email))
            .build();

        activityLogRepository.save(log);
    }

    /**
     * Validate a session token.
     */
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
            .map(UserSession::isValid)
            .orElse(false);
    }

    /**
     * Clean up expired sessions (scheduled task).
     */
    public int cleanupExpiredSessions() {
        List<UserSession> expired = sessionRepository.findExpiredSessions(LocalDateTime.now());
        
        for (UserSession session : expired) {
            session.revoke("Session expired");
        }
        
        sessionRepository.saveAll(expired);
        
        log.info("Cleaned up {} expired sessions", expired.size());
        return expired.size();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private SessionDTO mapToDTO(UserSession session) {
        return SessionDTO.builder()
            .id(session.getId())
            .deviceInfo(session.getDeviceInfo())
            .ipAddress(maskIpAddress(session.getIpAddress()))
            .location(session.getLocation())
            .lastActiveAt(session.getLastActiveAt())
            .createdAt(session.getCreatedAt())
            .isCurrentSession(false) // Set by caller
            .build();
    }

    private LoginHistoryDTO mapToLoginHistoryDTO(ActivityLog log) {
        return LoginHistoryDTO.builder()
            .success(log.getSuccess())
            .deviceInfo(log.getDeviceInfo())
            .ipAddress(maskIpAddress(log.getIpAddress()))
            .location(log.getLocation())
            .timestamp(log.getCreatedAt())
            .build();
    }

    private String maskIpAddress(String ip) {
        if (ip == null) return null;
        // Mask last octet for privacy: 192.168.1.100 -> 192.168.1.xxx
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return ip;
    }
}

3.6 ActivityLogService
File: backend/src/main/java/com/example/xaiapp/service/ActivityLogService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.response.ActivityLogDTO;
import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.ActivityLog.ActionType;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.ActivityLogRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.DeviceParser;
import com.example.xaiapp.util.GeoIpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for audit logging all user actions.
 * Provides comprehensive activity tracking for security and compliance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final GeoIpService geoIpService;
    private final DeviceParser deviceParser;

    /**
     * Log an activity with full context.
     * Automatically extracts IP and device info from current request.
     */
    public void logActivity(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata) {

        logActivity(userId, action, resourceType, resourceId, resourceName, 
            description, metadata, true, null, null);
    }

    /**
     * Log an activity with explicit success/failure status.
     */
    public void logActivity(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata,
            boolean success,
            String errorMessage,
            Long durationMs) {

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        // Extract request info
        String ipAddress = "unknown";
        String userAgent = null;
        String deviceInfo = null;
        String location = null;

        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = extractIpAddress(request);
                userAgent = request.getHeader("User-Agent");
                deviceInfo = deviceParser.parseUserAgent(userAgent);
                location = geoIpService.getLocation(ipAddress);
            }
        } catch (Exception e) {
            log.debug("Could not extract request info for activity log", e);
        }

        ActivityLog activityLog = ActivityLog.builder()
            .user(user)
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .resourceName(resourceName)
            .description(description)
            .metadata(metadata)
            .success(success)
            .errorMessage(errorMessage)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .durationMs(durationMs)
            .build();

        activityLogRepository.save(activityLog);

        log.debug("Activity logged: userId={}, action={}, resource={}:{}", 
            userId, action, resourceType, resourceId);
    }

    /**
     * Log activity asynchronously (for non-critical logging).
     */
    @Async
    public void logActivityAsync(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata) {
        
        logActivity(userId, action, resourceType, resourceId, resourceName, description, metadata);
    }

    /**
     * Get paginated activity logs for a user.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogs(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get activity logs within a date range.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogs(
            Long userId, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable) {
        
        return activityLogRepository.findByUserIdAndCreatedAtBetween(userId, start, end, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get activity logs filtered by action type.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsByAction(
            Long userId, 
            ActionType action, 
            Pageable pageable) {
        
        return activityLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(userId, action, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Get recent activity for dashboard (limited).
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getRecentActivity(Long userId, int limit) {
        return activityLogRepository.findRecentByUserId(userId, limit).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Export activity logs to CSV.
     */
    @Transactional(readOnly = true)
    public byte[] exportActivityLogsToCsv(Long userId, LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository
            .findByUserIdAndCreatedAtBetween(userId, start, end, Pageable.unpaged())
            .getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // Header
        writer.println("Timestamp,Action,Resource Type,Resource Name,Description,IP Address,Location,Success");

        // Data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ActivityLog log : logs) {
            writer.printf("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                log.getCreatedAt().format(formatter),
                log.getAction().name(),
                nullSafe(log.getResourceType()),
                escapeCSV(log.getResourceName()),
                escapeCSV(log.getDescription()),
                nullSafe(log.getIpAddress()),
                escapeCSV(log.getLocation()),
                log.getSuccess()
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    /**
     * Get activity statistics for a user.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getActivityStatistics(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        return Map.of(
            "logins", activityLogRepository.countByUserIdAndActionSince(
                userId, ActionType.LOGIN_SUCCESS, since),
            "predictions", activityLogRepository.countByUserIdAndActionSince(
                userId, ActionType.PREDICTION_MADE, since),
            "modelsTrainded", activityLogRepository.countByUserIdAndActionSince(
                userId, ActionType.MODEL_TRAINING_COMPLETED, since),
            "datasetsUploaded", activityLogRepository.countByUserIdAndActionSince(
                userId, ActionType.DATASET_UPLOADED, since)
        );
    }

    /**
     * Delete old activity logs (data retention).
     */
    public int deleteOldLogs(Long userId, int retentionDays) {
        LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
        int deleted = activityLogRepository.deleteByUserIdAndCreatedAtBefore(userId, before);
        log.info("Deleted {} old activity logs for user {}", deleted, userId);
        return deleted;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private ActivityLogDTO mapToDTO(ActivityLog log) {
        return ActivityLogDTO.builder()
            .id(log.getId())
            .action(log.getAction().name())
            .actionDisplayName(formatActionName(log.getAction()))
            .resourceType(log.getResourceType())
            .resourceId(log.getResourceId())
            .resourceName(log.getResourceName())
            .description(log.getDescription())
            .metadata(log.getMetadata())
            .success(log.getSuccess())
            .errorMessage(log.getErrorMessage())
            .ipAddress(maskIpAddress(log.getIpAddress()))
            .deviceInfo(log.getDeviceInfo())
            .location(log.getLocation())
            .durationMs(log.getDurationMs())
            .createdAt(log.getCreatedAt())
            .build();
    }

    private String formatActionName(ActionType action) {
        String name = action.name().replace("_", " ");
        return name.substring(0, 1) + name.substring(1).toLowerCase();
    }

    private String maskIpAddress(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return ip;
    }

    private String nullSafe(String str) {
        return str != null ? str : "";
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}

3.7 ModelComparisonService
File: backend/src/main/java/com/example/xaiapp/service/ModelComparisonService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.MLModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comparing multiple ML models.
 * Provides metrics comparison, feature importance analysis, and recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ModelComparisonService {

    private final MLModelRepository modelRepository;

    private static final int MAX_MODELS_TO_COMPARE = 5;

    /**
     * Compare multiple models and return comprehensive comparison data.
     *
     * @param userId User ID (for access control)
     * @param modelIds List of model IDs to compare
     * @return Comparison result
     */
    public ModelComparisonDTO compareModels(Long userId, List<Long> modelIds) {
        // Validate
        if (modelIds == null || modelIds.size() < 2) {
            throw new ValidationException("At least 2 models are required for comparison");
        }
        if (modelIds.size() > MAX_MODELS_TO_COMPARE) {
            throw new ValidationException("Maximum " + MAX_MODELS_TO_COMPARE + " models can be compared");
        }

        // Fetch models
        List<MLModel> models = modelIds.stream()
            .map(id -> modelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Model not found: " + id)))
            .collect(Collectors.toList());

        // Validate all models are ready
        for (MLModel model : models) {
            if (model.getStatus() != MLModel.ModelStatus.READY) {
                throw new ValidationException("Model " + model.getName() + " is not ready for comparison");
            }
        }

        // Check if all models are same type
        MLModel.ModelType firstType = models.get(0).getModelType();
        boolean sameType = models.stream().allMatch(m -> m.getModelType() == firstType);
        if (!sameType) {
            throw new ValidationException("All models must be of the same type (classification or regression)");
        }

        // Build comparison
        return ModelComparisonDTO.builder()
            .models(models.stream().map(this::mapToModelSummary).collect(Collectors.toList()))
            .metricsComparison(buildMetricsComparison(models))
            .featureImportanceComparison(buildFeatureImportanceComparison(models))
            .bestModelId(determineBestModel(models))
            .recommendations(generateRecommendations(models))
            .modelType(firstType.name())
            .build();
    }

    /**
     * Get all versions of a model (by base name).
     */
    public List<ModelVersionDTO> getModelVersions(Long userId, String baseName) {
        List<MLModel> models = modelRepository.findByUserIdAndBaseNameOrderByVersionDesc(userId, baseName);
        
        return models.stream()
            .map(this::mapToVersionDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get performance trend for a model over its versions.
     */
    public PerformanceTrendDTO getPerformanceTrend(Long userId, Long modelId) {
        MLModel model = modelRepository.findByIdAndUserId(modelId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Model not found"));

        List<MLModel> versions = modelRepository.findByUserIdAndBaseNameOrderByVersionAsc(
            userId, model.getBaseName());

        List<Map<String, Object>> trendData = versions.stream()
            .map(m -> {
                Map<String, Object> point = new HashMap<>();
                point.put("version", m.getVersion());
                point.put("accuracy", m.getAccuracy());
                point.put("trainedAt", m.getTrainedAt());
                point.put("featuresCount", m.getFeatureColumns().size());
                return point;
            })
            .collect(Collectors.toList());

        return PerformanceTrendDTO.builder()
            .modelBaseName(model.getBaseName())
            .currentVersion(model.getVersion())
            .trendData(trendData)
            .improvement(calculateImprovement(versions))
            .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private ModelSummaryDTO mapToModelSummary(MLModel model) {
        return ModelSummaryDTO.builder()
            .id(model.getId())
            .name(model.getName())
            .version(model.getVersion())
            .algorithm(model.getAlgorithm())
            .accuracy(model.getAccuracy())
            .trainedAt(model.getTrainedAt())
            .featureCount(model.getFeatureColumns().size())
            .build();
    }

    private List<MetricComparisonDTO> buildMetricsComparison(List<MLModel> models) {
        List<MetricComparisonDTO> comparisons = new ArrayList<>();
        
        boolean isClassification = models.get(0).getModelType() == MLModel.ModelType.CLASSIFICATION;

        // Common metrics
        comparisons.add(buildMetricRow("Accuracy", models, MLModel::getAccuracy));

        if (isClassification) {
            comparisons.add(buildMetricRow("Precision", models, MLModel::getPrecisionScore));
            comparisons.add(buildMetricRow("Recall", models, MLModel::getRecallScore));
            comparisons.add(buildMetricRow("F1 Score", models, MLModel::getF1Score));
        } else {
            comparisons.add(buildMetricRow("MSE", models, MLModel::getMse));
            comparisons.add(buildMetricRow("RMSE", models, MLModel::getRmse));
            comparisons.add(buildMetricRow("MAE", models, MLModel::getMae));
            comparisons.add(buildMetricRow("R² Score", models, MLModel::getR2Score));
        }

        // Training metrics
        comparisons.add(buildMetricRow("Training Time (s)", models, 
            m -> m.getTrainingDurationMs() != null ? m.getTrainingDurationMs() / 1000.0 : null));
        comparisons.add(buildMetricRow("Features Used", models, 
            m -> (double) m.getFeatureColumns().size()));

        return comparisons;
    }

    private MetricComparisonDTO buildMetricRow(
            String metricName, 
            List<MLModel> models, 
            java.util.function.Function<MLModel, Double> extractor) {
        
        Map<Long, Double> values = new LinkedHashMap<>();
        Long bestModelId = null;
        Double bestValue = null;
        boolean higherIsBetter = !metricName.contains("MSE") && !metricName.contains("MAE") 
                                 && !metricName.contains("Time");

        for (MLModel model : models) {
            Double value = extractor.apply(model);
            values.put(model.getId(), value);

            if (value != null) {
                if (bestValue == null || 
                    (higherIsBetter && value > bestValue) ||
                    (!higherIsBetter && value < bestValue)) {
                    bestValue = value;
                    bestModelId = model.getId();
                }
            }
        }

        return MetricComparisonDTO.builder()
            .metricName(metricName)
            .values(values)
            .bestModelId(bestModelId)
            .higherIsBetter(higherIsBetter)
            .build();
    }

    private Map<String, Map<Long, Double>> buildFeatureImportanceComparison(List<MLModel> models) {
        // Collect all unique features across all models
        Set<String> allFeatures = new LinkedHashSet<>();
        for (MLModel model : models) {
            if (model.getFeatureImportance() != null) {
                allFeatures.addAll(model.getFeatureImportance().keySet());
            }
        }

        // Build comparison map
        Map<String, Map<Long, Double>> comparison = new LinkedHashMap<>();
        
        for (String feature : allFeatures) {
            Map<Long, Double> featureValues = new LinkedHashMap<>();
            for (MLModel model : models) {
                Double importance = model.getFeatureImportance() != null 
                    ? model.getFeatureImportance().get(feature) 
                    : null;
                featureValues.put(model.getId(), importance);
            }
            comparison.put(feature, featureValues);
        }

        // Sort by average importance
        return comparison.entrySet().stream()
            .sorted((e1, e2) -> {
                double avg1 = e1.getValue().values().stream()
                    .filter(Objects::nonNull).mapToDouble(d -> d).average().orElse(0);
                double avg2 = e2.getValue().values().stream()
                    .filter(Objects::nonNull).mapToDouble(d -> d).average().orElse(0);
                return Double.compare(avg2, avg1); // Descending
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (a, b) -> a, 
                LinkedHashMap::new));
    }

    private Long determineBestModel(List<MLModel> models) {
        // Simple: highest accuracy
        return models.stream()
            .filter(m -> m.getAccuracy() != null)
            .max(Comparator.comparingDouble(MLModel::getAccuracy))
            .map(MLModel::getId)
            .orElse(null);
    }

    private List<String> generateRecommendations(List<MLModel> models) {
        List<String> recommendations = new ArrayList<>();

        // Find best model
        Optional<MLModel> bestModel = models.stream()
            .filter(m -> m.getAccuracy() != null)
            .max(Comparator.comparingDouble(MLModel::getAccuracy));

        if (bestModel.isPresent()) {
            MLModel best = bestModel.get();
            recommendations.add(String.format(
                "%s achieves the highest accuracy (%.1f%%) and is recommended for production use.",
                best.getName(), best.getAccuracy() * 100));

            // Check if more features helped
            OptionalInt maxFeatures = models.stream()
                .mapToInt(m -> m.getFeatureColumns().size())
                .max();

            if (maxFeatures.isPresent() && 
                best.getFeatureColumns().size() < maxFeatures.getAsInt()) {
                recommendations.add(
                    "Consider adding more features - some models with more features show potential.");
            }
        }

        // Check for overfitting indicators
        for (MLModel model : models) {
            if (model.getAccuracy() != null && model.getAccuracy() > 0.99) {
                recommendations.add(String.format(
                    "%s shows very high accuracy (%.1f%%) - verify this is not overfitting.",
                    model.getName(), model.getAccuracy() * 100));
            }
        }

        // Training time comparison
        OptionalDouble avgTime = models.stream()
            .filter(m -> m.getTrainingDurationMs() != null)
            .mapToLong(MLModel::getTrainingDurationMs)
            .average();

        if (avgTime.isPresent() && avgTime.getAsDouble() > 60000) {
            recommendations.add(
                "Training times are relatively long. Consider feature selection to improve efficiency.");
        }

        return recommendations;
    }

    private ModelVersionDTO mapToVersionDTO(MLModel model) {
        return ModelVersionDTO.builder()
            .id(model.getId())
            .version(model.getVersion())
            .accuracy(model.getAccuracy())
            .featureCount(model.getFeatureColumns().size())
            .trainedAt(model.getTrainedAt())
            .status(model.getStatus().name())
            .build();
    }

    private Double calculateImprovement(List<MLModel> versions) {
        if (versions.size() < 2) return null;

        MLModel first = versions.get(0);
        MLModel last = versions.get(versions.size() - 1);

        if (first.getAccuracy() == null || last.getAccuracy() == null) return null;

        return (last.getAccuracy() - first.getAccuracy()) * 100; // Percentage points improvement
    }
}

3.8 DashboardService
File: backend/src/main/java/com/example/xaiapp/service/DashboardService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard data aggregation.
 * Provides KPIs, statistics, and activity feeds for the dashboard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Get dashboard summary with all KPIs.
     */
    public DashboardSummaryDTO getDashboardSummary(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Core counts
        long datasetCount = datasetRepository.countByUserIdAndDeletedFalse(userId);
        long modelCount = modelRepository.countByUserIdAndStatusIn(userId, 
            List.of(MLModel.ModelStatus.READY, MLModel.ModelStatus.TRAINING));
        long predictionCount = predictionRepository.countByUserId(userId);

        // Recent activity counts
        long datasetsThisWeek = datasetRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long modelsThisWeek = modelRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long predictionsThisMonth = predictionRepository.countByUserIdSince(userId, thirtyDaysAgo);

        // Average accuracy
        Double avgAccuracy = modelRepository.getAverageAccuracyByUserId(userId);

        // Active models (ready status)
        long activeModels = modelRepository.countByUserIdAndStatus(userId, MLModel.ModelStatus.READY);

        return DashboardSummaryDTO.builder()
            .totalDatasets(datasetCount)
            .totalModels(modelCount)
            .totalPredictions(predictionCount)
            .averageModelAccuracy(avgAccuracy != null ? avgAccuracy : 0.0)
            .datasetsThisWeek(datasetsThisWeek)
            .modelsThisWeek(modelsThisWeek)
            .predictionsLast30Days(predictionsThisMonth)
            .activeModels(activeModels)
            .build();
    }

    /**
     * Get recent activity feed for dashboard.
     */
    public List<ActivityFeedItemDTO> getRecentActivity(Long userId, int limit) {
        List<ActivityLog> activities = activityLogRepository.findRecentByUserId(userId, limit);

        return activities.stream()
            .map(this::mapToFeedItem)
            .collect(Collectors.toList());
    }

    /**
     * Get model distribution by type.
     */
    public Map<String, Long> getModelsByType(Long userId) {
        long classification = modelRepository.countByUserIdAndModelType(
            userId, MLModel.ModelType.CLASSIFICATION);
        long regression = modelRepository.countByUserIdAndModelType(
            userId, MLModel.ModelType.REGRESSION);

        return Map.of(
            "classification", classification,
            "regression", regression
        );
    }

    /**
     * Get usage trend over time.
     */
    public List<UsageTrendDTO> getUsageTrend(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        // Get daily prediction counts
        List<Object[]> predictionData = predictionRepository.getDailyPredictionCounts(userId, startDate);
        
        // Convert to DTOs
        Map<String, Long> predictionsByDate = new HashMap<>();
        for (Object[] row : predictionData) {
            predictionsByDate.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        // Generate all dates in range
        List<UsageTrendDTO> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.toLocalDate().toString();
            
            trend.add(UsageTrendDTO.builder()
                .date(dateStr)
                .predictions(predictionsByDate.getOrDefault(dateStr, 0L))
                .build());
        }

        return trend;
    }

    /**
     * Get recent models for dashboard table.
     */
    public List<RecentModelDTO> getRecentModels(Long userId, int limit) {
        return modelRepository.findTopByUserIdOrderByCreatedAtDesc(userId, limit).stream()
            .map(this::mapToRecentModel)
            .collect(Collectors.toList());
    }

    /**
     * Get quick stats for sidebar.
     */
    public QuickStatsDTO getQuickStats(Long userId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        long predictionsToday = predictionRepository.countByUserIdSince(userId, today);
        long modelsInTraining = modelRepository.countByUserIdAndStatus(
            userId, MLModel.ModelStatus.TRAINING);

        // Storage usage (simplified)
        long totalDatasetSize = datasetRepository.getTotalFileSizeByUserId(userId);
        long totalModelSize = modelRepository.getTotalModelSizeByUserId(userId);

        return QuickStatsDTO.builder()
            .predictionsToday(predictionsToday)
            .modelsInTraining(modelsInTraining)
            .storageUsedBytes(totalDatasetSize + totalModelSize)
            .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private ActivityFeedItemDTO mapToFeedItem(ActivityLog log) {
        return ActivityFeedItemDTO.builder()
            .id(log.getId())
            .type(mapActionToFeedType(log.getAction()))
            .icon(mapActionToIcon(log.getAction()))
            .title(formatActivityTitle(log))
            .subtitle(log.getResourceName())
            .timestamp(log.getCreatedAt())
            .actionUrl(buildActionUrl(log))
            .build();
    }

    private String mapActionToFeedType(ActivityLog.ActionType action) {
        return switch (action) {
            case MODEL_TRAINING_COMPLETED, MODEL_TRAINING_STARTED -> "model";
            case DATASET_UPLOADED -> "dataset";
            case PREDICTION_MADE -> "prediction";
            case LOGIN_SUCCESS -> "security";
            default -> "activity";
        };
    }

    private String mapActionToIcon(ActivityLog.ActionType action) {
        return switch (action) {
            case MODEL_TRAINING_COMPLETED -> "🤖";
            case MODEL_TRAINING_FAILED -> "⚠️";
            case DATASET_UPLOADED -> "📁";
            case PREDICTION_MADE -> "🔮";
            case LOGIN_SUCCESS -> "🔐";
            case API_KEY_CREATED -> "🔑";
            default -> "📋";
        };
    }

    private String formatActivityTitle(ActivityLog log) {
        return switch (log.getAction()) {
            case MODEL_TRAINING_COMPLETED -> "Model Training Complete";
            case MODEL_TRAINING_FAILED -> "Training Failed";
            case MODEL_TRAINING_STARTED -> "Model Training Started";
            case DATASET_UPLOADED -> "Dataset Uploaded";
            case PREDICTION_MADE -> "Prediction Made";
            case LOGIN_SUCCESS -> "Successful Login";
            case API_KEY_CREATED -> "API Key Created";
            default -> log.getAction().name().replace("_", " ");
        };
    }

    private String buildActionUrl(ActivityLog log) {
        if (log.getResourceType() == null || log.getResourceId() == null) {
            return null;
        }

        return switch (log.getResourceType()) {
            case "MODEL" -> "/models/" + log.getResourceId();
            case "DATASET" -> "/datasets/" + log.getResourceId();
            case "PREDICTION" -> "/predictions/" + log.getResourceId();
            default -> null;
        };
    }

    private RecentModelDTO mapToRecentModel(MLModel model) {
        return RecentModelDTO.builder()
            .id(model.getId())
            .name(model.getName())
            .type(model.getModelType().name())
            .algorithm(model.getAlgorithm())
            .accuracy(model.getAccuracy())
            .status(model.getStatus().name())
            .createdAt(model.getCreatedAt())
            .datasetName(model.getDataset().getName())
            .predictionCount(model.getPredictionCount())
            .build();
    }
}

3.9 WebhookService
File: backend/src/main/java/com/example/xaiapp/service/WebhookService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.request.CreateWebhookRequest;
import com.example.xaiapp.dto.request.UpdateWebhookRequest;
import com.example.xaiapp.dto.response.WebhookDTO;
import com.example.xaiapp.dto.response.WebhookTestResultDTO;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.Webhook;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for webhook management and delivery.
 * Handles webhook CRUD, payload signing, and async delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;

    private static final int SECRET_LENGTH = 32;
    private static final int MAX_WEBHOOKS_PER_USER = 10;
    private static final int WEBHOOK_TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create a new webhook.
     */
    public WebhookDTO createWebhook(Long userId, CreateWebhookRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check limit
        long existingCount = webhookRepository.countByUserId(userId);
        if (existingCount >= MAX_WEBHOOKS_PER_USER) {
            throw new ValidationException("Maximum webhooks limit reached (" + MAX_WEBHOOKS_PER_USER + ")");
        }

        // Validate URL
        validateWebhookUrl(request.getUrl());

        // Validate events
        validateEvents(request.getEvents());

        // Generate secret
        String secret = generateSecret();

        Webhook webhook = Webhook.builder()
            .user(user)
            .name(request.getName())
            .url(request.getUrl())
            .secret(secret)
            .events(request.getEvents())
            .description(request.getDescription())
            .active(true)
            .build();

        webhook = webhookRepository.save(webhook);

        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.WEBHOOK_CREATED,
            "WEBHOOK",
            webhook.getId(),
            webhook.getName(),
            "Webhook created: " + webhook.getName(),
            Map.of("events", request.getEvents())
        );

        log.info("Webhook created: userId={}, webhookId={}, url={}", 
            userId, webhook.getId(), webhook.getUrl());

        // Return DTO with secret (shown only once)
        WebhookDTO dto = mapToDTO(webhook);
        dto.setSecret(secret); // Only included on creation
        return dto;
    }

    /**
     * Get all webhooks for a user.
     */
    @Transactional(readOnly = true)
    public List<WebhookDTO> getWebhooks(Long userId) {
        return webhookRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update a webhook.
     */
    public WebhookDTO updateWebhook(Long userId, Long webhookId, UpdateWebhookRequest request) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        if (request.getName() != null) {
            webhook.setName(request.getName());
        }
        if (request.getUrl() != null) {
            validateWebhookUrl(request.getUrl());
            webhook.setUrl(request.getUrl());
        }
        if (request.getEvents() != null) {
            validateEvents(request.getEvents());
            webhook.setEvents(request.getEvents());
        }
        if (request.getDescription() != null) {
            webhook.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            webhook.setActive(request.getActive());
            // Reset auto-disabled if manually re-enabled
            if (request.getActive()) {
                webhook.setAutoDisabled(false);
                webhook.setAutoDisabledAt(null);
                webhook.setFailureCount(0);
            }
        }

        webhook = webhookRepository.save(webhook);
        log.info("Webhook updated: webhookId={}", webhookId);

        return mapToDTO(webhook);
    }

    /**
     * Delete a webhook.
     */
    public void deleteWebhook(Long userId, Long webhookId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        webhookRepository.delete(webhook);

        activityLogService.logActivity(
            userId,
            ActivityLog.ActionType.WEBHOOK_DELETED,
            "WEBHOOK",
            webhookId,
            webhook.getName(),
            "Webhook deleted: " + webhook.getName(),
            null
        );

        log.info("Webhook deleted: webhookId={}", webhookId);
    }

    /**
     * Test a webhook by sending a test payload.
     */
    public WebhookTestResultDTO testWebhook(Long userId, Long webhookId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        // Create test payload
        Map<String, Object> payload = Map.of(
            "event", "webhook.test",
            "timestamp", LocalDateTime.now().toString(),
            "data", Map.of(
                "message", "This is a test webhook delivery from XAI-Forge",
                "webhookId", webhookId
            )
        );

        // Attempt delivery
        WebhookDeliveryResult result = deliverWebhook(webhook, payload);

        return WebhookTestResultDTO.builder()
            .success(result.success)
            .statusCode(result.statusCode)
            .responseBody(truncate(result.responseBody, 500))
            .responseTimeMs(result.responseTimeMs)
            .errorMessage(result.errorMessage)
            .build();
    }

    /**
     * Trigger webhooks for a specific event.
     * Called asynchronously by other services when events occur.
     */
    @Async
    public void triggerWebhooks(String event, Map<String, Object> data) {
        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(event);

        for (Webhook webhook : webhooks) {
            Map<String, Object> payload = Map.of(
                "event", event,
                "timestamp", LocalDateTime.now().toString(),
                "data", data
            );

            deliverWithRetry(webhook, payload);
        }
    }

    /**
     * Trigger webhooks for a specific user and event.
     */
    @Async
    public void triggerUserWebhooks(Long userId, String event, Map<String, Object> data) {
        List<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(userId).stream()
            .filter(w -> w.getEvents().contains(event))
            .collect(Collectors.toList());

        for (Webhook webhook : webhooks) {
            Map<String, Object> payload = Map.of(
                "event", event,
                "timestamp", LocalDateTime.now().toString(),
                "data", data
            );

            deliverWithRetry(webhook, payload);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void deliverWithRetry(Webhook webhook, Map<String, Object> payload) {
        int attempts = 0;
        WebhookDeliveryResult result = null;

        while (attempts < MAX_RETRIES) {
            attempts++;
            result = deliverWebhook(webhook, payload);

            if (result.success) {
                webhook.recordSuccess(result.statusCode, result.responseBody);
                webhookRepository.save(webhook);
                log.debug("Webhook delivered: webhookId={}, event={}", 
                    webhook.getId(), payload.get("event"));
                return;
            }

            // Wait before retry (exponential backoff)
            if (attempts < MAX_RETRIES) {
                try {
                    Thread.sleep((long) Math.pow(2, attempts) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries failed
        if (result != null) {
            webhook.recordFailure(result.statusCode, result.errorMessage);
            webhookRepository.save(webhook);
            log.warn("Webhook delivery failed after {} attempts: webhookId={}, error={}", 
                attempts, webhook.getId(), result.errorMessage);
        }
    }

    private WebhookDeliveryResult deliverWebhook(Webhook webhook, Map<String, Object> payload) {
        long startTime = System.currentTimeMillis();

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String signature = generateSignature(payloadJson, webhook.getSecret());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-XAI-Signature", "sha256=" + signature);
            headers.set("X-XAI-Event", (String) payload.get("event"));
            headers.set("X-XAI-Delivery", UUID.randomUUID().toString());

            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                URI.create(webhook.getUrl()),
                HttpMethod.POST,
                entity,
                String.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            return new WebhookDeliveryResult(
                response.getStatusCode().is2xxSuccessful(),
                response.getStatusCode().value(),
                response.getBody(),
                responseTime,
                null
            );

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.debug("Webhook delivery error: webhookId={}, error={}", webhook.getId(), e.getMessage());

            return new WebhookDeliveryResult(
                false,
                0,
                null,
                responseTime,
                e.getMessage()
            );
        }
    }

    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validateWebhookUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new ValidationException("Webhook URL is required");
        }
        if (!url.startsWith("https://") && !url.startsWith("http://localhost")) {
            throw new ValidationException("Webhook URL must use HTTPS (except localhost for testing)");
        }
        try {
            new URI(url);
        } catch (Exception e) {
            throw new ValidationException("Invalid webhook URL");
        }
    }

    private void validateEvents(Set<String> events) {
        if (events == null || events.isEmpty()) {
            throw new ValidationException("At least one event must be selected");
        }
        for (String event : events) {
            if (!Webhook.Events.ALL.contains(event)) {
                throw new ValidationException("Invalid event type: " + event);
            }
        }
    }

    private WebhookDTO mapToDTO(Webhook webhook) {
        return WebhookDTO.builder()
            .id(webhook.getId())
            .name(webhook.getName())
            .url(webhook.getUrl())
            // Secret is NOT included by default (only on creation)
            .events(webhook.getEvents())
            .description(webhook.getDescription())
            .active(webhook.getActive())
            .autoDisabled(webhook.getAutoDisabled())
            .lastTriggeredAt(webhook.getLastTriggeredAt())
            .lastResponseCode(webhook.getLastResponseCode())
            .failureCount(webhook.getFailureCount())
            .successCount(webhook.getSuccessCount())
            .createdAt(webhook.getCreatedAt())
            .build();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }

    // Inner class for delivery result
    private record WebhookDeliveryResult(
        boolean success,
        int statusCode,
        String responseBody,
        long responseTimeMs,
        String errorMessage
    ) {}
}

3.10 DataExportService
File: backend/src/main/java/com/example/xaiapp/service/DataExportService.java
javapackage com.example.xaiapp.service;

import com.example.xaiapp.dto.request.FullExportRequest;
import com.example.xaiapp.dto.response.ExportJobDTO;
import com.example.xaiapp.entity.*;
import com.example.xaiapp.entity.ExportJob.ExportFormat;
import com.example.xaiapp.entity.ExportJob.ExportStatus;
import com.example.xaiapp.entity.ExportJob.ExportType;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for exporting user data.
 * Handles full data exports, individual exports, and scheduled backups.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${app.export.temp-directory:/tmp/xai-exports}")
    private String exportTempDirectory;

    @Value("${app.export.retention-days:7}")
    private int exportRetentionDays;

    /**
     * Request a full data export.
     * Returns immediately with job ID; processing happens async.
     */
    @Transactional
    public ExportJobDTO requestFullExport(Long userId, FullExportRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check for existing pending export
        List<ExportJob> pendingJobs = exportJobRepository.findByUserIdAndStatus(
            userId, ExportStatus.PENDING);
        if (!pendingJobs.isEmpty()) {
            throw new ValidationException("An export is already in progress");
        }

        // Create job
        ExportJob job = ExportJob.builder()
            .user(user)
            .exportType(ExportType.FULL)
            .includeItems(request.getIncludeItems())
            .format(request.getFormat() != null ? request.getFormat() : ExportFormat.ZIP)
            .status(ExportStatus.PENDING)
            .progress(0)
            .build();

        job = exportJobRepository.save(job);

        // Start async processing
        processExportAsync(job.getId());

        log.info("Export job created: userId={}, jobId={}", userId, job.getId());

        return mapToDTO(job);
    }

    /**
     * Get export job status.
     */
    @Transactional(readOnly = true)
    public ExportJobDTO getExportStatus(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Export job not found"));

        return mapToDTO(job);
    }

    /**
     * Download completed export file.
     */
    @Transactional
    public Resource downloadExport(Long userId, Long jobId) {
        ExportJob job = exportJobRepository.findByIdAndUserId(jobId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Export job not found"));

        if (job.getStatus() != ExportStatus.COMPLETED) {
            throw new ValidationException("Export is not ready for download");
        }

        if (job.getFilePath() == null || !Files.exists(Path.of(job.getFilePath()))) {
            throw new ResourceNotFoundException("Export file not found");
        }

        // Increment download count
        job.setDownloadCount(job.getDownloadCount() + 1);
        exportJobRepository.save(job);

        return new FileSystemResource(job.getFilePath());
    }

    /**
     * Process export asynchronously.
     */
    @Async
    @Transactional
    public void processExportAsync(Long jobId) {
        ExportJob job = exportJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Export job not found: {}", jobId);
            return;
        }

        try {
            job.startProcessing();
            exportJobRepository.save(job);

            // Create temp directory
            Path exportDir = Paths.get(exportTempDirectory, job.getUser().getId().toString());
            Files.createDirectories(exportDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path zipPath = exportDir.resolve("xai_export_" + timestamp + ".zip");

            // Create ZIP file
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                int totalSteps = job.getIncludeItems().size();
                int currentStep = 0;

                for (String item : job.getIncludeItems()) {
                    currentStep++;
                    int progress = (currentStep * 100) / totalSteps;
                    job.updateProgress(progress, "Exporting " + item + "...");
                    exportJobRepository.save(job);

                    switch (item.toLowerCase()) {
                        case "datasets" -> exportDatasets(zos, job.getUser().getId());
                        case "models" -> exportModels(zos, job.getUser().getId());
                        case "predictions" -> exportPredictions(zos, job.getUser().getId());
                        case "activity" -> exportActivityLogs(zos, job.getUser().getId());
                    }
                }

                // Add metadata file
                addMetadataFile(zos, job);
            }

            // Update job as completed
            long fileSize = Files.size(zipPath);
            job.complete(zipPath.toString(), fileSize);
            exportJobRepository.save(job);

            // Notify user
            notificationService.notifyExportReady(
                job.getUser().getId(), job.getId(), job.getExportType().name());

            log.info("Export completed: jobId={}, size={}", jobId, fileSize);

        } catch (Exception e) {
            log.error("Export failed: jobId={}", jobId, e);
            job.fail(e.getMessage());
            exportJobRepository.save(job);
        }
    }

    /**
     * Clean up expired export files.
     */
    @Transactional
    public int cleanupExpiredExports() {
        List<ExportJob> expired = exportJobRepository.findExpiredJobs(LocalDateTime.now());
        int cleaned = 0;

        for (ExportJob job : expired) {
            try {
                if (job.getFilePath() != null) {
                    Files.deleteIfExists(Path.of(job.getFilePath()));
                }
                job.setStatus(ExportStatus.EXPIRED);
                exportJobRepository.save(job);
                cleaned++;
            } catch (IOException e) {
                log.warn("Failed to delete expired export file: {}", job.getFilePath(), e);
            }
        }

        log.info("Cleaned up {} expired exports", cleaned);
        return cleaned;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void exportDatasets(ZipOutputStream zos, Long userId) throws IOException {
        List<Dataset> datasets = datasetRepository.findByUserIdAndDeletedFalse(userId);

        // Export metadata
        List<Map<String, Object>> datasetData = new ArrayList<>();
        for (Dataset d : datasets) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", d.getId());
            data.put("name", d.getName());
            data.put("description", d.getDescription());
            data.put("originalFilename", d.getOriginalFilename());
            data.put("rowCount", d.getRowCount());
            data.put("columnCount", d.getColumnCount());
            data.put("columnNames", d.getColumnNames());
            data.put("columnMetadata", d.getColumnMetadata());
            data.put("createdAt", d.getCreatedAt().toString());
            datasetData.add(data);
        }

        addJsonEntry(zos, "datasets/datasets.json", datasetData);

        // Copy actual dataset files
        for (Dataset d : datasets) {
            if (d.getFilePath() != null && Files.exists(Path.of(d.getFilePath()))) {
                addFileEntry(zos, "datasets/files/" + d.getOriginalFilename(), 
                    Path.of(d.getFilePath()));
            }
        }
    }

    private void exportModels(ZipOutputStream zos, Long userId) throws IOException {
        List<MLModel> models = modelRepository.findByUserId(userId);

        List<Map<String, Object>> modelData = new ArrayList<>();
        for (MLModel m : models) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", m.getId());
            data.put("name", m.getName());
            data.put("description", m.getDescription());
            data.put("modelType", m.getModelType().name());
            data.put("algorithm", m.getAlgorithm());
            data.put("targetColumn", m.getTargetColumn());
            data.put("featureColumns", m.getFeatureColumns());
            data.put("accuracy", m.getAccuracy());
            data.put("precisionScore", m.getPrecisionScore());
            data.put("recallScore", m.getRecallScore());
            data.put("f1Score", m.getF1Score());
            data.put("featureImportance", m.getFeatureImportance());
            data.put("status", m.getStatus().name());
            data.put("createdAt", m.getCreatedAt().toString());
            data.put("trainedAt", m.getTrainedAt() != null ? m.getTrainedAt().toString() : null);
            modelData.add(data);
        }

        addJsonEntry(zos, "models/models.json", modelData);
    }

    private void exportPredictions(ZipOutputStream zos, Long userId) throws IOException {
        List<Prediction> predictions = predictionRepository
            .findByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
            .getContent();

        List<Map<String, Object>> predictionData = new ArrayList<>();
        for (Prediction p : predictions) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", p.getId());
            data.put("modelId", p.getModel().getId());
            data.put("modelName", p.getModel().getName());
            data.put("inputData", p.getInputData());
            data.put("predictionResult", p.getPredictionResult());
            data.put("confidence", p.getConfidence());
            data.put("explanation", p.getExplanation());
            data.put("explanationSummary", p.getExplanationSummary());
            data.put("createdAt", p.getCreatedAt().toString());
            predictionData.add(data);
        }

        addJsonEntry(zos, "predictions/predictions.json", predictionData);
    }

    private void exportActivityLogs(ZipOutputStream zos, Long userId) throws IOException {
        List<ActivityLog> logs = activityLogRepository
            .findByUserIdOrderByCreatedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
            .getContent();

        List<Map<String, Object>> logData = new ArrayList<>();
        for (ActivityLog l : logs) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", l.getId());
            data.put("action", l.getAction().name());
            data.put("resourceType", l.getResourceType());
            data.put("resourceId", l.getResourceId());
            data.put("resourceName", l.getResourceName());
            data.put("description", l.getDescription());
            data.put("success", l.getSuccess());
            data.put("ipAddress", l.getIpAddress());
            data.put("location", l.getLocation());
            data.put("createdAt", l.getCreatedAt().toString());
            logData.add(data);
        }

        addJsonEntry(zos, "activity/activity_log.json", logData);
    }

    private void addMetadataFile(ZipOutputStream zos, ExportJob job) throws IOException {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("exportedAt", LocalDateTime.now().toString());
        metadata.put("exportType", job.getExportType().name());
        metadata.put("includedItems", job.getIncludeItems());
        metadata.put("userId", job.getUser().getId());
        metadata.put("userEmail", job.getUser().getEmail());
        metadata.put("xaiForgeVersion", "1.0.0");

        addJsonEntry(zos, "export_metadata.json", metadata);
    }

    private void addJsonEntry(ZipOutputStream zos, String entryName, Object data) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(zos, data);
        zos.closeEntry();
    }

    private void addFileEntry(ZipOutputStream zos, String entryName, Path filePath) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private ExportJobDTO mapToDTO(ExportJob job) {
        return ExportJobDTO.builder()
            .id(job.getId())
            .status(job.getStatus().name())
            .exportType(job.getExportType().name())
            .format(job.getFormat().name())
            .includeItems(job.getIncludeItems())
            .progress(job.getProgress())
            .currentStep(job.getCurrentStep())
            .fileSizeBytes(job.getFileSizeBytes())
            .errorMessage(job.getErrorMessage())
            .createdAt(job.getCreatedAt())
            .startedAt(job.getStartedAt())
            .completedAt(job.getCompletedAt())
            .expiresAt(job.getExpiresAt())
            .downloadCount(job.getDownloadCount())
            .build();
    }
}

PHASE 4: REST CONTROLLERS
4.1 UserProfileController
File: backend/src/main/java/com/example/xaiapp/controller/UserProfileController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.*;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Get current user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(@CurrentUser UserPrincipal currentUser) {
        UserProfileDTO profile = userProfileService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile.
     */
    @PutMapping("/me")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDTO profile = userProfileService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Upload profile avatar.
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userProfileService.uploadAvatar(currentUser.getId(), file);
        return ResponseEntity.ok(new AvatarUploadResponse(avatarUrl));
    }

    /**
     * Delete profile avatar.
     */
    @DeleteMapping("/me/avatar")
    @Operation(summary = "Delete profile avatar")
    public ResponseEntity<Void> deleteAvatar(@CurrentUser UserPrincipal currentUser) {
        userProfileService.deleteAvatar(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user statistics.
     */
    @GetMapping("/me/statistics")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@CurrentUser UserPrincipal currentUser) {
        UserStatisticsDTO stats = userProfileService.getUserStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * Change password.
     */
    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<MessageResponse> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * Enable two-factor authentication.
     */
    @PostMapping("/me/2fa/enable")
    @Operation(summary = "Enable 2FA - returns QR code and backup codes")
    public ResponseEntity<TwoFactorSetupDTO> enable2FA(@CurrentUser UserPrincipal currentUser) {
        TwoFactorSetupDTO setup = userProfileService.enable2FA(currentUser.getId());
        return ResponseEntity.ok(setup);
    }

    /**
     * Verify and activate 2FA.
     */
    @PostMapping("/me/2fa/verify")
    @Operation(summary = "Verify 2FA code and activate")
    public ResponseEntity<TwoFactorVerifyResponse> verify2FA(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        boolean valid = userProfileService.verify2FA(currentUser.getId(), request.getCode());
        return ResponseEntity.ok(new TwoFactorVerifyResponse(valid, 
            valid ? "2FA enabled successfully" : "Invalid verification code"));
    }

    /**
     * Disable 2FA.
     */
    @DeleteMapping("/me/2fa")
    @Operation(summary = "Disable 2FA")
    public ResponseEntity<MessageResponse> disable2FA(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        userProfileService.disable2FA(currentUser.getId(), request.getCode());
        return ResponseEntity.ok(new MessageResponse("2FA disabled successfully"));
    }

    /**
     * Delete account.
     */
    @DeleteMapping("/me")
    @Operation(summary = "Delete user account")
    public ResponseEntity<MessageResponse> deleteAccount(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody DeleteAccountRequest request) {
        userProfileService.deleteAccount(currentUser.getId(), request.getPassword());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}

4.2 SessionController
File: backend/src/main/java/com/example/xaiapp/controller/SessionController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for session management.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Session management endpoints")
public class SessionController {

    private final SessionService sessionService;

    /**
     * Get all active sessions.
     */
    @GetMapping
    @Operation(summary = "List active sessions")
    public ResponseEntity<List<SessionDTO>> getActiveSessions(
            @CurrentUser UserPrincipal currentUser,
            @RequestHeader("Authorization") String authHeader) {
        // Extract session token from JWT (simplified - actual implementation may vary)
        String sessionToken = extractSessionToken(authHeader);
        List<SessionDTO> sessions = sessionService.getActiveSessions(
            currentUser.getId(), sessionToken);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Revoke a specific session.
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Revoke a session")
    public ResponseEntity<MessageResponse> revokeSession(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        sessionService.revokeSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(new MessageResponse("Session revoked successfully"));
    }

    /**
     * Revoke all other sessions (keep current).
     */
    @DeleteMapping("/others")
    @Operation(summary = "Revoke all other sessions")
    public ResponseEntity<MessageResponse> revokeOtherSessions(
            @CurrentUser UserPrincipal currentUser,
            @RequestHeader("Authorization") String authHeader) {
        String sessionToken = extractSessionToken(authHeader);
        sessionService.revokeAllOtherSessions(currentUser.getId(), sessionToken);
        return ResponseEntity.ok(new MessageResponse("All other sessions revoked"));
    }

    /**
     * Get login history.
     */
    @GetMapping("/history")
    @Operation(summary = "Get login history")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistory(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "20") int limit) {
        List<LoginHistoryDTO> history = sessionService.getLoginHistory(currentUser.getId(), limit);
        return ResponseEntity.ok(history);
    }

    private String extractSessionToken(String authHeader) {
        // Implementation depends on how session token is stored
        // Could be in JWT claims or separate header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

4.3 NotificationController
File: backend/src/main/java/com/example/xaiapp/controller/NotificationController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get notifications (paginated).
     */
    @GetMapping
    @Operation(summary = "List notifications")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationDTO> notifications = notificationService.getNotifications(
            currentUser.getId(), pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread count.
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    /**
     * Mark notification as read.
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(currentUser.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mark all notifications as read.
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete notification.
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Void> deleteNotification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long notificationId) {
        notificationService.deleteNotification(currentUser.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }
}

4.4 ApiKeyController
File: backend/src/main/java/com/example/xaiapp/controller/ApiKeyController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.CreateApiKeyRequest;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for API key management.
 */
@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "API key management endpoints")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * List all API keys (masked).
     */
    @GetMapping
    @Operation(summary = "List API keys")
    public ResponseEntity<List<ApiKeyDTO>> getApiKeys(@CurrentUser UserPrincipal currentUser) {
        List<ApiKeyDTO> keys = apiKeyService.getApiKeys(currentUser.getId());
        return ResponseEntity.ok(keys);
    }

    /**
     * Generate new API key.
     */
    @PostMapping
    @Operation(summary = "Generate new API key", 
               description = "Returns the full key ONLY on creation. Store it securely.")
    public ResponseEntity<ApiKeyResponseDTO> generateApiKey(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyResponseDTO response = apiKeyService.generateApiKey(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Revoke API key.
     */
    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke API key")
    public ResponseEntity<MessageResponse> revokeApiKey(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long keyId) {
        apiKeyService.revokeApiKey(currentUser.getId(), keyId);
        return ResponseEntity.ok(new MessageResponse("API key revoked successfully"));
    }
}

4.5 PredictionController
File: backend/src/main/java/com/example/xaiapp/controller/PredictionController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.BulkDeleteRequest;
import com.example.xaiapp.dto.request.PredictionFilterRequest;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.PredictionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for prediction history management.
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Tag(name = "Predictions", description = "Prediction history endpoints")
public class PredictionController {

    private final PredictionHistoryService predictionHistoryService;

    /**
     * List predictions with filters.
     */
    @GetMapping
    @Operation(summary = "List predictions")
    public ResponseEntity<Page<PredictionDTO>> getPredictions(
            @CurrentUser UserPrincipal currentUser,
            @ModelAttribute PredictionFilterRequest filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PredictionDTO> predictions = predictionHistoryService.getPredictions(
            currentUser.getId(), filter, pageable);
        return ResponseEntity.ok(predictions);
    }

    /**
     * Get prediction details.
     */
    @GetMapping("/{predictionId}")
    @Operation(summary = "Get prediction details")
    public ResponseEntity<PredictionDetailDTO> getPrediction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        PredictionDetailDTO prediction = predictionHistoryService.getPrediction(
            currentUser.getId(), predictionId);
        return ResponseEntity.ok(prediction);
    }

    /**
     * Delete prediction.
     */
    @DeleteMapping("/{predictionId}")
    @Operation(summary = "Delete prediction")
    public ResponseEntity<Void> deletePrediction(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        predictionHistoryService.deletePrediction(currentUser.getId(), predictionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk delete predictions.
     */
    @PostMapping("/bulk-delete")
    @Operation(summary = "Bulk delete predictions")
    public ResponseEntity<BulkDeleteResponse> bulkDelete(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody BulkDeleteRequest request) {
        int deleted = predictionHistoryService.bulkDeletePredictions(
            currentUser.getId(), request.getIds());
        return ResponseEntity.ok(new BulkDeleteResponse(deleted));
    }

    /**
     * Export predictions.
     */
    @GetMapping("/export")
    @Operation(summary = "Export predictions")
    public ResponseEntity<byte[]> exportPredictions(
            @CurrentUser UserPrincipal currentUser,
            @ModelAttribute PredictionFilterRequest filter,
            @RequestParam(defaultValue = "csv") String format) {
        
        byte[] data;
        String contentType;
        String filename;

        if ("json".equalsIgnoreCase(format)) {
            // For JSON, we'd serialize the DTOs
            data = predictionHistoryService.exportPredictionsToCsv(currentUser.getId(), filter); // Simplified
            contentType = MediaType.APPLICATION_JSON_VALUE;
            filename = "predictions_export.json";
        } else {
            data = predictionHistoryService.exportPredictionsToCsv(currentUser.getId(), filter);
            contentType = "text/csv";
            filename = "predictions_export.csv";
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(data);
    }

    /**
     * Regenerate explanation for a prediction.
     */
    @PostMapping("/{predictionId}/re-explain")
    @Operation(summary = "Regenerate explanation")
    public ResponseEntity<ExplanationDTO> regenerateExplanation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long predictionId) {
        ExplanationDTO explanation = predictionHistoryService.regenerateExplanation(
            currentUser.getId(), predictionId);
        return ResponseEntity.ok(explanation);
    }
}

4.6 DashboardController
File: backend/src/main/java/com/example/xaiapp/controller/DashboardController.java
javapackage com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for dashboard data.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard data endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get dashboard summary with all KPIs.
     */
    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
            @CurrentUser UserPrincipal currentUser) {
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(currentUser.getId());
        return ResponseEntity.ok(summary);
    }

    /**
     * Get recent activity feed.
     */
    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity")
    public ResponseEntity<List<ActivityFeedItemDTO>> getRecentActivity(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        List<ActivityFeedItemDTO> activity = dashboardService.getRecentActivity(
            currentUser.getId(), limit);
        return ResponseEntity.ok(activity);
    }

    /**
     * Get models by type distribution.
     */
    @GetMapping("/models-by-type")
    @Operation(summary = "Get model type distribution")
    public ResponseEntity<Map<String, Long>> getModelsByType(
            @CurrentUser UserPrincipal currentUser) {
        Map<String, Long> distribution = dashboardService.getModelsByType(currentUser.getId());
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get usage trend over time.
     */
    @GetMapping("/usage-trend")
    @Operation(summary = "Get usage trend")
    public ResponseEntity<List<UsageTrendDTO>> getUsageTrend(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "30") int days) {
        List<UsageTrendDTO> trend = dashboardService.getUsageTrend(currentUser.getId(), days);
        return ResponseEntity.ok(trend);
    }

    /**
     * Get recent models.
     */
    @GetMapping("/recent-models")
    @Operation(summary = "Get recent models")
    public ResponseEntity<List<RecentModelDTO>> getRecentModels(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecentModelDTO> models = dashboardService.getRecentModels(currentUser.getId(), limit);
        return ResponseEntity.ok(models);
    }

    /**
     * Get quick stats.
     */
    @GetMapping("/quick-stats")
    @Operation(summary = "Get quick stats")
    public ResponseEntity<QuickStatsDTO> getQuickStats(@CurrentUser UserPrincipal currentUser) {
        QuickStatsDTO stats = dashboardService.getQuickStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }
}

4.7 Additional Controllers
Create these controllers following the same pattern:
File: backend/src/main/java/com/example/xaiapp/controller/ModelComparisonController.java
java// Endpoints:
// POST /api/models/compare - Compare multiple models
// GET /api/models/{id}/versions - Get model versions
// GET /api/models/{id}/trend - Get performance trend
File: backend/src/main/java/com/example/xaiapp/controller/SettingsController.java
java// Endpoints:
// GET /api/settings/preferences - Get user preferences
// PUT /api/settings/preferences - Update preferences
// PUT /api/settings/notifications - Update notification preferences
// PUT /api/settings/appearance - Update appearance preferences
// POST /api/settings/reset - Reset to defaults
// GET /api/settings/storage - Get storage usage
// GET /api/settings/retention - Get retention policies
// PUT /api/settings/retention - Update retention policies
File: backend/src/main/java/com/example/xaiapp/controller/ExportController.java
java// Endpoints:
// POST /api/export/full - Request full data export
// GET /api/export/{jobId}/status - Check export status
// GET /api/export/{jobId}/download - Download export file
File: backend/src/main/java/com/example/xaiapp/controller/ActivityLogController.java
java// Endpoints:
// GET /api/activity - Get activity log (paginated)
// GET /api/activity/export - Export activity log
File: backend/src/main/java/com/example/xaiapp/controller/WebhookController.java
java// Endpoints:
// GET /api/webhooks - List webhooks
// POST /api/webhooks - Create webhook
// PUT /api/webhooks/{id} - Update webhook
// DELETE /api/webhooks/{id} - Delete webhook
// POST /api/webhooks/{id}/test - Test webhook
File: backend/src/main/java/com/example/xaiapp/controller/SearchController.java
java// Endpoints:
// GET /api/search?q={query} - Global search

PHASE 5: DTOs
5.1 Request DTOs
File: backend/src/main/java/com/example/xaiapp/dto/request/UpdateProfileRequest.java
javapackage com.example.xaiapp.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 200, message = "Organization must be at most 200 characters")
    private String organization;

    @Size(max = 100, message = "Role must be at most 100 characters")
    private String role;

    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;
}
File: backend/src/main/java/com/example/xaiapp/dto/request/ChangePasswordRequest.java
javapackage com.example.xaiapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
File: backend/src/main/java/com/example/xaiapp/dto/request/CreateApiKeyRequest.java
javapackage com.example.xaiapp.dto.request;

import com.example.xaiapp.entity.ApiKey.ApiKeyEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiKeyRequest {

    @NotBlank(message = "Key name is required")
    @Size(max = 100, message = "Key name must be at most 100 characters")
    private String name;

    @NotNull(message = "Environment is required")
    private ApiKeyEnvironment environment;

    @NotEmpty(message = "At least one permission is required")
    private Set<String> permissions;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private LocalDateTime expiresAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/request/CreateWebhookRequest.java
javapackage com.example.xaiapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWebhookRequest {

    @NotBlank(message = "Webhook name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    @NotEmpty(message = "At least one event must be selected")
    private Set<String> events;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
File: backend/src/main/java/com/example/xaiapp/dto/request/PredictionFilterRequest.java
javapackage com.example.xaiapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionFilterRequest {

    private Long modelId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String predictionResult;
}
File: backend/src/main/java/com/example/xaiapp/dto/request/FullExportRequest.java
javapackage com.example.xaiapp.dto.request;

import com.example.xaiapp.entity.ExportJob.ExportFormat;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullExportRequest {

    @NotEmpty(message = "At least one item must be selected for export")
    private Set<String> includeItems; // datasets, models, predictions, activity

    private ExportFormat format; // ZIP, JSON, CSV
}

5.2 Response DTOs
File: backend/src/main/java/com/example/xaiapp/dto/response/UserProfileDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String organization;
    private String role;
    private String location;
    private String bio;
    private String profileImageUrl;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/DashboardSummaryDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private Long totalDatasets;
    private Long totalModels;
    private Long totalPredictions;
    private Double averageModelAccuracy;
    private Long datasetsThisWeek;
    private Long modelsThisWeek;
    private Long predictionsLast30Days;
    private Long activeModels;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/PredictionDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDTO {
    private Long id;
    private Long modelId;
    private String modelName;
    private String predictionResult;
    private Double confidence;
    private String inputSummary;
    private LocalDateTime createdAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/PredictionDetailDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDetailDTO {
    private Long id;
    private Long modelId;
    private String modelName;
    private String modelType;
    private Map<String, Object> inputData;
    private String predictionResult;
    private Double confidence;
    private Map<String, Object> explanation;
    private String explanationSummary;
    private Long predictionTimeMs;
    private Long explanationTimeMs;
    private LocalDateTime createdAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/NotificationDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String priority;
    private String actionUrl;
    private String actionLabel;
    private LocalDateTime createdAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/ApiKeyDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDTO {
    private Long id;
    private String name;
    private String keyPreview; // e.g., "xai_live_sk_...a1b2"
    private String environment;
    private Set<String> permissions;
    private Boolean active;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/ApiKeyResponseDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response for API key creation.
 * Includes the full key which is shown ONLY ONCE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponseDTO {
    private Long id;
    private String name;
    private String key; // Full key - shown only on creation!
    private String keyPreview;
    private String environment;
    private Set<String> permissions;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
File: backend/src/main/java/com/example/xaiapp/dto/response/ModelComparisonDTO.java
javapackage com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelComparisonDTO {
    private List<ModelSummaryDTO> models;
    private List<MetricComparisonDTO> metricsComparison;
    private Map<String, Map<Long, Double>> featureImportanceComparison;
    private Long bestModelId;
    private List<String> recommendations;
    private String modelType;
}
Create additional response DTOs following the same pattern for:

SessionDTO
LoginHistoryDTO
ActivityLogDTO
WebhookDTO
WebhookTestResultDTO
ExportJobDTO
UserStatisticsDTO
TwoFactorSetupDTO
ActivityFeedItemDTO
UsageTrendDTO
RecentModelDTO
QuickStatsDTO
MessageResponse
UnreadCountResponse
BulkDeleteResponse


PHASE 6: SECURITY ENHANCEMENTS
6.1 API Key Authentication Filter
File: backend/src/main/java/com/example/xaiapp/security/ApiKeyAuthenticationFilter.java
javapackage com.example.xaiapp.security;

import com.example.xaiapp.dto.response.ApiKeyValidationResult;
import com.example.xaiapp.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Filter for API key authentication.
 * Checks for X-API-Key header and validates the key.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null && !apiKey.isEmpty()) {
            Optional<ApiKeyValidationResult> validationResult = apiKeyService.validateApiKey(apiKey);

            if (validationResult.isPresent()) {
                ApiKeyValidationResult result = validationResult.get();

                // Convert permissions to authorities
                List<SimpleGrantedAuthority> authorities = result.getPermissions().stream()
                    .map(perm -> new SimpleGrantedAuthority("PERM_" + perm.toUpperCase().replace(":", "_")))
                    .collect(Collectors.toList());

                // Create authentication token
                ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                    result.getUserId(),
                    result.getKeyId(),
                    authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Update last used
                String ipAddress = extractIpAddress(request);
                apiKeyService.updateLastUsed(result.getKeyId(), ipAddress);

                log.debug("API key authenticated: userId={}, keyId={}", 
                    result.getUserId(), result.getKeyId());
            } else {
                log.debug("Invalid API key provided");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid API key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to API paths that support API key auth
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/"); // Future versioned API endpoints
    }
}
File: backend/src/main/java/com/example/xaiapp/security/ApiKeyAuthenticationToken.java
javapackage com.example.xaiapp.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Authentication token for API key authentication.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final Long apiKeyId;

    public ApiKeyAuthenticationToken(
            Long userId,
            Long apiKeyId,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKeyId;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }
}

6.2 Two-Factor Authentication Service
File: backend/src/main/java/com/example/xaiapp/security/TwoFactorAuthService.java
javapackage com.example.xaiapp.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Service for two-factor authentication using TOTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.2fa.issuer:XAI-Forge}")
    private String issuer;

    /**
     * Generate a new TOTP secret.
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Generate QR code data URI for authenticator app setup.
     */
    public String generateQRCodeDataUri(String secret, String email) {
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            issuer, email, new GoogleAuthenticatorKey.Builder(secret).build());
        
        // In production, generate actual QR code image
        // For now, return the URL that can be used with a QR code library on frontend
        return otpAuthUrl;
    }

    /**
     * Verify a TOTP code.
     */
    public boolean verifyCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Generate backup codes for account recovery.
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            byte[] bytes = new byte[5];
            secureRandom.nextBytes(bytes);
            String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, 8).toUpperCase();
            codes.add(code);
        }
        return codes;
    }

    /**
     * Hash backup codes for storage.
     */
    public String hashBackupCodes(List<String> codes) {
        // In production, hash each code individually
        return String.join(",", codes);
    }

    /**
     * Verify a backup code.
     */
    public boolean verifyBackupCode(String storedCodes, String providedCode) {
        if (storedCodes == null || providedCode == null) return false;
        String[] codes = storedCodes.split(",");
        for (String code : codes) {
            if (code.equalsIgnoreCase(providedCode.trim())) {
                return true;
            }
        }
        return false;
    }
}

6.3 Update Security Config
File: backend/src/main/java/com/example/xaiapp/config/SecurityConfig.java
javapackage com.example.xaiapp.config;

import com.example.xaiapp.security.ApiKeyAuthenticationFilter;
import com.example.xaiapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Add API key filter before JWT filter
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, ApiKeyAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-API-Key"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

PHASE 7: DATABASE MIGRATIONS
File: backend/src/main/resources/db/migration/V2__add_user_profile_fields.sql
sql-- Add profile fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS location VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expires TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_backup_codes VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS lock_expires_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
File: backend/src/main/resources/db/migration/V3__create_new_tables.sql
sql-- Predictions table
CREATE TABLE IF NOT EXISTS predictions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    model_id BIGINT NOT NULL REFERENCES ml_models(id) ON DELETE CASCADE,
    input_data JSONB NOT NULL,
    prediction_result VARCHAR(500) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    explanation JSONB,
    explanation_summary TEXT,
    prediction_time_ms BIGINT,
    explanation_time_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prediction_user_id ON predictions(user_id);
CREATE INDEX idx_prediction_model_id ON predictions(model_id);
CREATE INDEX idx_prediction_created_at ON predictions(created_at);
CREATE INDEX idx_prediction_user_created ON predictions(user_id, created_at DESC);

-- API Keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    key_prefix VARCHAR(20) NOT NULL,
    key_suffix VARCHAR(4) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    permissions JSONB NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    last_used_ip VARCHAR(45),
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_api_key_user_id ON api_keys(user_id);
CREATE INDEX idx_api_key_hash ON api_keys(key_hash);
CREATE INDEX idx_api_key_active ON api_keys(user_id, active);

-- User Sessions table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(100) NOT NULL UNIQUE,
    refresh_token_hash VARCHAR(64),
    device_info VARCHAR(200),
    user_agent VARCHAR(500),
    ip_address VARCHAR(45) NOT NULL,
    location VARCHAR(200),
    country_code VARCHAR(2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_active_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    revocation_reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_user_id ON user_sessions(user_id);
CREATE INDEX idx_session_token ON user_sessions(session_token);
CREATE INDEX idx_session_active ON user_sessions(user_id, is_active);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    action_url VARCHAR(500),
    action_label VARCHAR(50),
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_read ON notifications(user_id, is_read);
CREATE INDEX idx_notification_created ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notification_type ON notifications(user_id, type);

-- User Preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    theme VARCHAR(10) NOT NULL DEFAULT 'DARK',
    accent_color VARCHAR(6) NOT NULL DEFAULT '00d9ff',
    display_density VARCHAR(15) NOT NULL DEFAULT 'DEFAULT',
    reduce_motion BOOLEAN NOT NULL DEFAULT FALSE,
    high_contrast BOOLEAN NOT NULL DEFAULT FALSE,
    font_size_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    email_notifications JSONB NOT NULL DEFAULT '{}',
    in_app_notifications JSONB NOT NULL DEFAULT '{}',
    push_notifications JSONB NOT NULL DEFAULT '{}',
    quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start TIME DEFAULT '22:00',
    quiet_hours_end TIME DEFAULT '07:00',
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/New_York',
    default_classification_algorithm VARCHAR(50) NOT NULL DEFAULT 'LOGISTIC_REGRESSION',
    default_regression_algorithm VARCHAR(50) NOT NULL DEFAULT 'LINEAR_REGRESSION',
    auto_detect_column_types BOOLEAN NOT NULL DEFAULT TRUE,
    auto_exclude_id_columns BOOLEAN NOT NULL DEFAULT TRUE,
    default_preview_rows INTEGER NOT NULL DEFAULT 5,
    prediction_retention_days INTEGER NOT NULL DEFAULT 90,
    failed_training_retention_days INTEGER NOT NULL DEFAULT 30,
    deleted_dataset_retention_days INTEGER NOT NULL DEFAULT 7,
    sidebar_collapsed BOOLEAN NOT NULL DEFAULT FALSE,
    dataset_view VARCHAR(10) NOT NULL DEFAULT 'GRID',
    items_per_page INTEGER NOT NULL DEFAULT 20,
    show_onboarding BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Activity Logs table
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(30) NOT NULL,
    resource_type VARCHAR(30),
    resource_id BIGINT,
    resource_name VARCHAR(200),
    description VARCHAR(500),
    metadata JSONB,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message VARCHAR(1000),
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    device_info VARCHAR(200),
    location VARCHAR(200),
    session_id VARCHAR(100),
    api_key_id BIGINT,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activity_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_created ON activity_logs(user_id, created_at DESC);
CREATE INDEX idx_activity_action ON activity_logs(user_id, action);
CREATE INDEX idx_activity_resource ON activity_logs(resource_type, resource_id);
CREATE INDEX idx_activity_date_range ON activity_logs(created_at);

-- Webhooks table
CREATE TABLE IF NOT EXISTS webhooks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    events JSONB NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    auto_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    auto_disabled_at TIMESTAMP,
    last_triggered_at TIMESTAMP,
    last_response_code INTEGER,
    last_response_body VARCHAR(1000),
    failure_count INTEGER NOT NULL DEFAULT 0,
    success_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_user_id ON webhooks(user_id);
CREATE INDEX idx_webhook_active ON webhooks(user_id, active);

-- Export Jobs table
CREATE TABLE IF NOT EXISTS export_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    export_type VARCHAR(20) NOT NULL,
    include_items JSONB NOT NULL,
    format VARCHAR(10) NOT NULL DEFAULT 'ZIP',
    progress INTEGER NOT NULL DEFAULT 0,
    current_step VARCHAR(200),
    file_path VARCHAR(500),
    file_size_bytes BIGINT,
    error_message TEXT,
    metadata JSONB,
    download_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_export_user_id ON export_jobs(user_id);
CREATE INDEX idx_export_status ON export_jobs(user_id, status);
File: backend/src/main/resources/db/migration/V4__update_datasets_models.sql
sql-- Update datasets table
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS mime_type VARCHAR(100) DEFAULT 'text/csv';
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS row_count INTEGER;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_count INTEGER;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'READY';
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS processing_error TEXT;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_metadata JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS column_names JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS target_column VARCHAR(100);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS recommended_target VARCHAR(100);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS inferred_task_type VARCHAR(20);
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS quality_score INTEGER;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS quality_issues JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS sample_rows JSONB;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE datasets ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_dataset_status ON datasets(user_id, status);
CREATE INDEX IF NOT EXISTS idx_dataset_created ON datasets(user_id, created_at DESC);

-- Update ml_models table

-- Add description field
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS description TEXT;

-- Add versioning fields
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS base_name VARCHAR(200);

-- Add model file metadata
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS model_size_bytes BIGINT;

-- Add classification metrics
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS precision_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS recall_score DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS f1_score DOUBLE PRECISION;

-- Add regression metrics
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS mse DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS rmse DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS mae DOUBLE PRECISION;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS r2_score DOUBLE PRECISION;

-- Add confusion matrix and class labels (for classification)
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS confusion_matrix JSONB;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS class_labels JSONB;

-- Add feature importance
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS feature_importance JSONB;

-- Add training history (for charts)
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_history JSONB;

-- Add training metadata
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_duration_ms BIGINT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_samples INTEGER;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS test_samples INTEGER;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS train_test_split DOUBLE PRECISION DEFAULT 0.8;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS hyperparameters JSONB;

-- Add training progress fields
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_error TEXT;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_progress INTEGER DEFAULT 0;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS training_step VARCHAR(200);

-- Add timestamps
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS trained_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP;
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add usage statistics
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS prediction_count BIGINT NOT NULL DEFAULT 0;

-- Update status column to include new statuses
-- First check if status column exists, if not add it
ALTER TABLE ml_models ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'READY';

-- Create indexes for ml_models
CREATE INDEX IF NOT EXISTS idx_model_user_id ON ml_models(user_id);
CREATE INDEX IF NOT EXISTS idx_model_dataset_id ON ml_models(dataset_id);
CREATE INDEX IF NOT EXISTS idx_model_status ON ml_models(user_id, status);
CREATE INDEX IF NOT EXISTS idx_model_created ON ml_models(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_model_base_name ON ml_models(user_id, base_name);
CREATE INDEX IF NOT EXISTS idx_model_type ON ml_models(user_id, model_type);

File: backend/src/main/resources/db/migration/V5__create_user_preferences_defaults.sql
sql-- ============================================================
-- V5: Create default user preferences for existing users
-- ============================================================

-- Insert default preferences for all existing users who don't have preferences
INSERT INTO user_preferences (
    user_id,
    theme,
    accent_color,
    display_density,
    reduce_motion,
    high_contrast,
    font_size_multiplier,
    email_notifications,
    in_app_notifications,
    push_notifications,
    quiet_hours_enabled,
    quiet_hours_start,
    quiet_hours_end,
    timezone,
    default_classification_algorithm,
    default_regression_algorithm,
    auto_detect_column_types,
    auto_exclude_id_columns,
    default_preview_rows,
    prediction_retention_days,
    failed_training_retention_days,
    deleted_dataset_retention_days,
    sidebar_collapsed,
    dataset_view,
    items_per_page,
    show_onboarding,
    updated_at
)
SELECT 
    u.id,
    'DARK',
    '00d9ff',
    'DEFAULT',
    FALSE,
    FALSE,
    1.0,
    '{"MODEL_TRAINED": true, "MODEL_FAILED": true, "DATASET_UPLOADED": false, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": true, "EXPORT_READY": true}'::jsonb,
    '{"MODEL_TRAINED": true, "MODEL_FAILED": true, "DATASET_UPLOADED": true, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": false, "EXPORT_READY": true}'::jsonb,
    '{"MODEL_TRAINED": false, "MODEL_FAILED": true, "DATASET_UPLOADED": false, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": false, "EXPORT_READY": false}'::jsonb,
    FALSE,
    '22:00'::TIME,
    '07:00'::TIME,
    'America/New_York',
    'LOGISTIC_REGRESSION',
    'LINEAR_REGRESSION',
    TRUE,
    TRUE,
    5,
    90,
    30,
    7,
    FALSE,
    'GRID',
    20,
    TRUE,
    CURRENT_TIMESTAMP
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_preferences up WHERE up.user_id = u.id
);

-- Create a trigger to automatically create preferences for new users
CREATE OR REPLACE FUNCTION create_default_preferences()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_preferences (
        user_id,
        theme,
        accent_color,
        display_density,
        email_notifications,
        in_app_notifications,
        push_notifications,
        timezone,
        default_classification_algorithm,
        default_regression_algorithm
    ) VALUES (
        NEW.id,
        'DARK',
        '00d9ff',
        'DEFAULT',
        '{"MODEL_TRAINED": true, "MODEL_FAILED": true, "DATASET_UPLOADED": false, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": true, "EXPORT_READY": true}'::jsonb,
        '{"MODEL_TRAINED": true, "MODEL_FAILED": true, "DATASET_UPLOADED": true, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": false, "EXPORT_READY": true}'::jsonb,
        '{"MODEL_TRAINED": false, "MODEL_FAILED": true, "DATASET_UPLOADED": false, "SECURITY_ALERT": true, "WEEKLY_SUMMARY": false, "EXPORT_READY": false}'::jsonb,
        'America/New_York',
        'LOGISTIC_REGRESSION',
        'LINEAR_REGRESSION'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS create_user_preferences_trigger ON users;
CREATE TRIGGER create_user_preferences_trigger
    AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION create_default_preferences();

File: backend/src/main/resources/db/migration/V6__add_foreign_key_constraints.sql
sql-- ============================================================
-- V6: Add foreign key constraints and cascade rules
-- ============================================================

-- Note: Some constraints may already exist from entity definitions
-- Using IF NOT EXISTS pattern where possible

-- ===========================================
-- PREDICTIONS TABLE CONSTRAINTS
-- ===========================================

-- Ensure foreign key to users exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_prediction_user' AND table_name = 'predictions'
    ) THEN
        ALTER TABLE predictions 
        ADD CONSTRAINT fk_prediction_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ensure foreign key to ml_models exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_prediction_model' AND table_name = 'predictions'
    ) THEN
        ALTER TABLE predictions 
        ADD CONSTRAINT fk_prediction_model 
        FOREIGN KEY (model_id) REFERENCES ml_models(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- API_KEYS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_api_key_user' AND table_name = 'api_keys'
    ) THEN
        ALTER TABLE api_keys 
        ADD CONSTRAINT fk_api_key_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- USER_SESSIONS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_session_user' AND table_name = 'user_sessions'
    ) THEN
        ALTER TABLE user_sessions 
        ADD CONSTRAINT fk_session_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- NOTIFICATIONS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_notification_user' AND table_name = 'notifications'
    ) THEN
        ALTER TABLE notifications 
        ADD CONSTRAINT fk_notification_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- USER_PREFERENCES TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_preferences_user' AND table_name = 'user_preferences'
    ) THEN
        ALTER TABLE user_preferences 
        ADD CONSTRAINT fk_preferences_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- ACTIVITY_LOGS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_activity_log_user' AND table_name = 'activity_logs'
    ) THEN
        ALTER TABLE activity_logs 
        ADD CONSTRAINT fk_activity_log_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- ===========================================
-- WEBHOOKS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_webhook_user' AND table_name = 'webhooks'
    ) THEN
        ALTER TABLE webhooks 
        ADD CONSTRAINT fk_webhook_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- EXPORT_JOBS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_export_job_user' AND table_name = 'export_jobs'
    ) THEN
        ALTER TABLE export_jobs 
        ADD CONSTRAINT fk_export_job_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- DATASETS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_dataset_user' AND table_name = 'datasets'
    ) THEN
        ALTER TABLE datasets 
        ADD CONSTRAINT fk_dataset_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

-- ===========================================
-- ML_MODELS TABLE CONSTRAINTS
-- ===========================================

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_model_user' AND table_name = 'ml_models'
    ) THEN
        ALTER TABLE ml_models 
        ADD CONSTRAINT fk_model_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_model_dataset' AND table_name = 'ml_models'
    ) THEN
        ALTER TABLE ml_models 
        ADD CONSTRAINT fk_model_dataset 
        FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE CASCADE;
    END IF;
END $$;

File: backend/src/main/resources/db/migration/V7__add_check_constraints.sql
sql-- ============================================================
-- V7: Add check constraints for data validation
-- ============================================================

-- ===========================================
-- PREDICTIONS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Confidence must be between 0 and 1
ALTER TABLE predictions DROP CONSTRAINT IF EXISTS chk_prediction_confidence;
ALTER TABLE predictions ADD CONSTRAINT chk_prediction_confidence 
    CHECK (confidence >= 0 AND confidence <= 1);

-- Prediction time must be positive
ALTER TABLE predictions DROP CONSTRAINT IF EXISTS chk_prediction_time;
ALTER TABLE predictions ADD CONSTRAINT chk_prediction_time 
    CHECK (prediction_time_ms IS NULL OR prediction_time_ms >= 0);

-- ===========================================
-- API_KEYS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Environment must be valid
ALTER TABLE api_keys DROP CONSTRAINT IF EXISTS chk_api_key_environment;
ALTER TABLE api_keys ADD CONSTRAINT chk_api_key_environment 
    CHECK (environment IN ('PRODUCTION', 'DEVELOPMENT', 'STAGING'));

-- Usage count must be non-negative
ALTER TABLE api_keys DROP CONSTRAINT IF EXISTS chk_api_key_usage;
ALTER TABLE api_keys ADD CONSTRAINT chk_api_key_usage 
    CHECK (usage_count >= 0);

-- ===========================================
-- NOTIFICATIONS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Type must be valid
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_type;
ALTER TABLE notifications ADD CONSTRAINT chk_notification_type 
    CHECK (type IN (
        'MODEL_TRAINED', 'MODEL_FAILED', 'DATASET_UPLOADED', 'DATASET_FAILED',
        'PREDICTION_COMPLETE', 'SECURITY_ALERT', 'WEEKLY_SUMMARY', 'EXPORT_READY',
        'SYSTEM', 'API_KEY_CREATED', 'API_KEY_USED', 'STORAGE_WARNING'
    ));

-- Priority must be valid
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS chk_notification_priority;
ALTER TABLE notifications ADD CONSTRAINT chk_notification_priority 
    CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'));

-- ===========================================
-- USER_PREFERENCES TABLE CHECK CONSTRAINTS
-- ===========================================

-- Theme must be valid
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_theme;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_theme 
    CHECK (theme IN ('DARK', 'LIGHT', 'SYSTEM'));

-- Display density must be valid
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_density;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_density 
    CHECK (display_density IN ('COMFORTABLE', 'DEFAULT', 'COMPACT'));

-- Font size multiplier must be reasonable
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_font_size;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_font_size 
    CHECK (font_size_multiplier >= 0.5 AND font_size_multiplier <= 2.0);

-- Items per page must be reasonable
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_items_per_page;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_items_per_page 
    CHECK (items_per_page >= 5 AND items_per_page <= 100);

-- Retention days must be valid
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_retention;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_retention 
    CHECK (prediction_retention_days >= 0 AND prediction_retention_days <= 365);

-- Dataset view must be valid
ALTER TABLE user_preferences DROP CONSTRAINT IF EXISTS chk_preferences_view;
ALTER TABLE user_preferences ADD CONSTRAINT chk_preferences_view 
    CHECK (dataset_view IN ('GRID', 'LIST'));

-- ===========================================
-- ACTIVITY_LOGS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Action must be valid
ALTER TABLE activity_logs DROP CONSTRAINT IF EXISTS chk_activity_action;
ALTER TABLE activity_logs ADD CONSTRAINT chk_activity_action 
    CHECK (action IN (
        'LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'PASSWORD_CHANGED',
        'TWO_FACTOR_ENABLED', 'TWO_FACTOR_DISABLED',
        'DATASET_UPLOADED', 'DATASET_DELETED', 'DATASET_UPDATED',
        'MODEL_TRAINING_STARTED', 'MODEL_TRAINING_COMPLETED', 'MODEL_TRAINING_FAILED',
        'MODEL_DELETED', 'MODEL_ARCHIVED', 'MODEL_EXPORTED',
        'PREDICTION_MADE', 'PREDICTION_DELETED', 'EXPLANATION_GENERATED',
        'API_KEY_CREATED', 'API_KEY_REVOKED', 'API_KEY_USED',
        'WEBHOOK_CREATED', 'WEBHOOK_DELETED', 'WEBHOOK_TRIGGERED',
        'PROFILE_UPDATED', 'PREFERENCES_UPDATED',
        'EXPORT_REQUESTED', 'EXPORT_COMPLETED', 'EXPORT_DOWNLOADED',
        'SESSION_REVOKED', 'ALL_SESSIONS_REVOKED', 'ACCOUNT_DELETED'
    ));

-- ===========================================
-- WEBHOOKS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Failure count must be non-negative
ALTER TABLE webhooks DROP CONSTRAINT IF EXISTS chk_webhook_failure_count;
ALTER TABLE webhooks ADD CONSTRAINT chk_webhook_failure_count 
    CHECK (failure_count >= 0);

-- Success count must be non-negative
ALTER TABLE webhooks DROP CONSTRAINT IF EXISTS chk_webhook_success_count;
ALTER TABLE webhooks ADD CONSTRAINT chk_webhook_success_count 
    CHECK (success_count >= 0);

-- ===========================================
-- EXPORT_JOBS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Status must be valid
ALTER TABLE export_jobs DROP CONSTRAINT IF EXISTS chk_export_status;
ALTER TABLE export_jobs ADD CONSTRAINT chk_export_status 
    CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'EXPIRED'));

-- Export type must be valid
ALTER TABLE export_jobs DROP CONSTRAINT IF EXISTS chk_export_type;
ALTER TABLE export_jobs ADD CONSTRAINT chk_export_type 
    CHECK (export_type IN ('FULL', 'DATASETS', 'MODELS', 'PREDICTIONS', 'ACTIVITY'));

-- Format must be valid
ALTER TABLE export_jobs DROP CONSTRAINT IF EXISTS chk_export_format;
ALTER TABLE export_jobs ADD CONSTRAINT chk_export_format 
    CHECK (format IN ('ZIP', 'JSON', 'CSV'));

-- Progress must be 0-100
ALTER TABLE export_jobs DROP CONSTRAINT IF EXISTS chk_export_progress;
ALTER TABLE export_jobs ADD CONSTRAINT chk_export_progress 
    CHECK (progress >= 0 AND progress <= 100);

-- Download count must be non-negative
ALTER TABLE export_jobs DROP CONSTRAINT IF EXISTS chk_export_download_count;
ALTER TABLE export_jobs ADD CONSTRAINT chk_export_download_count 
    CHECK (download_count >= 0);

-- ===========================================
-- DATASETS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Status must be valid
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_status;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_status 
    CHECK (status IN ('UPLOADING', 'PROCESSING', 'READY', 'ERROR', 'DELETED'));

-- Task type must be valid
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_task_type;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_task_type 
    CHECK (inferred_task_type IS NULL OR inferred_task_type IN ('CLASSIFICATION', 'REGRESSION', 'UNKNOWN'));

-- Quality score must be 0-100
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_quality_score;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_quality_score 
    CHECK (quality_score IS NULL OR (quality_score >= 0 AND quality_score <= 100));

-- Row count must be positive
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_row_count;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_row_count 
    CHECK (row_count IS NULL OR row_count > 0);

-- Column count must be positive
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_column_count;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_column_count 
    CHECK (column_count IS NULL OR column_count > 0);

-- File size must be positive
ALTER TABLE datasets DROP CONSTRAINT IF EXISTS chk_dataset_file_size;
ALTER TABLE datasets ADD CONSTRAINT chk_dataset_file_size 
    CHECK (file_size_bytes IS NULL OR file_size_bytes > 0);

-- ===========================================
-- ML_MODELS TABLE CHECK CONSTRAINTS
-- ===========================================

-- Status must be valid
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_status;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_status 
    CHECK (status IN ('TRAINING', 'READY', 'FAILED', 'ARCHIVED'));

-- Model type must be valid
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_type;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_type 
    CHECK (model_type IN ('CLASSIFICATION', 'REGRESSION'));

-- Accuracy must be between 0 and 1
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_accuracy;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_accuracy 
    CHECK (accuracy IS NULL OR (accuracy >= 0 AND accuracy <= 1));

-- Precision must be between 0 and 1
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_precision;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_precision 
    CHECK (precision_score IS NULL OR (precision_score >= 0 AND precision_score <= 1));

-- Recall must be between 0 and 1
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_recall;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_recall 
    CHECK (recall_score IS NULL OR (recall_score >= 0 AND recall_score <= 1));

-- F1 score must be between 0 and 1
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_f1;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_f1 
    CHECK (f1_score IS NULL OR (f1_score >= 0 AND f1_score <= 1));

-- R2 score typically between -inf and 1, but usually 0-1 for good models
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_r2;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_r2 
    CHECK (r2_score IS NULL OR r2_score <= 1);

-- Training progress must be 0-100
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_progress;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_progress 
    CHECK (training_progress >= 0 AND training_progress <= 100);

-- Train/test split must be between 0.1 and 0.9
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_split;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_split 
    CHECK (train_test_split IS NULL OR (train_test_split >= 0.1 AND train_test_split <= 0.9));

-- Version must be positive
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_version;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_version 
    CHECK (version > 0);

-- Prediction count must be non-negative
ALTER TABLE ml_models DROP CONSTRAINT IF EXISTS chk_model_prediction_count;
ALTER TABLE ml_models ADD CONSTRAINT chk_model_prediction_count 
    CHECK (prediction_count >= 0);

File: backend/src/main/resources/db/migration/V8__create_indexes_for_performance.sql
sql-- ============================================================
-- V8: Create additional indexes for query performance
-- ============================================================

-- ===========================================
-- COMPOSITE INDEXES FOR COMMON QUERIES
-- ===========================================

-- Predictions: User's recent predictions with model info
CREATE INDEX IF NOT EXISTS idx_prediction_user_model_created 
    ON predictions(user_id, model_id, created_at DESC);

-- Predictions: Filter by confidence threshold
CREATE INDEX IF NOT EXISTS idx_prediction_confidence 
    ON predictions(user_id, confidence DESC);

-- Models: User's models by accuracy (for ranking)
CREATE INDEX IF NOT EXISTS idx_model_user_accuracy 
    ON ml_models(user_id, accuracy DESC) 
    WHERE status = 'READY';

-- Models: User's models by type and status
CREATE INDEX IF NOT EXISTS idx_model_user_type_status 
    ON ml_models(user_id, model_type, status);

-- Datasets: User's active datasets
CREATE INDEX IF NOT EXISTS idx_dataset_user_active 
    ON datasets(user_id, created_at DESC) 
    WHERE deleted = FALSE;

-- Notifications: User's unread notifications
CREATE INDEX IF NOT EXISTS idx_notification_user_unread 
    ON notifications(user_id, created_at DESC) 
    WHERE is_read = FALSE;

-- Activity logs: Recent activity by type
CREATE INDEX IF NOT EXISTS idx_activity_user_action_created 
    ON activity_logs(user_id, action, created_at DESC);

-- Sessions: Active sessions for user
CREATE INDEX IF NOT EXISTS idx_session_user_active_expires 
    ON user_sessions(user_id, expires_at) 
    WHERE is_active = TRUE;

-- API Keys: Active keys for user
CREATE INDEX IF NOT EXISTS idx_api_key_user_active_expires 
    ON api_keys(user_id, expires_at) 
    WHERE active = TRUE;

-- Webhooks: Active webhooks by event
CREATE INDEX IF NOT EXISTS idx_webhook_events 
    ON webhooks USING GIN (events) 
    WHERE active = TRUE AND auto_disabled = FALSE;

-- ===========================================
-- PARTIAL INDEXES FOR SPECIFIC QUERIES
-- ===========================================

-- Models in training (for monitoring)
CREATE INDEX IF NOT EXISTS idx_model_training 
    ON ml_models(user_id, created_at DESC) 
    WHERE status = 'TRAINING';

-- Failed models (for troubleshooting)
CREATE INDEX IF NOT EXISTS idx_model_failed 
    ON ml_models(user_id, trained_at DESC) 
    WHERE status = 'FAILED';

-- Export jobs pending/processing
CREATE INDEX IF NOT EXISTS idx_export_pending 
    ON export_jobs(created_at) 
    WHERE status IN ('PENDING', 'PROCESSING');

-- Export jobs ready for cleanup
CREATE INDEX IF NOT EXISTS idx_export_expired 
    ON export_jobs(expires_at) 
    WHERE status = 'COMPLETED';

-- ===========================================
-- TEXT SEARCH INDEXES
-- ===========================================

-- Full text search on dataset names and descriptions
CREATE INDEX IF NOT EXISTS idx_dataset_search 
    ON datasets USING GIN (to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '')));

-- Full text search on model names
CREATE INDEX IF NOT EXISTS idx_model_search 
    ON ml_models USING GIN (to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '')));

-- ===========================================
-- JSONB INDEXES FOR METADATA QUERIES
-- ===========================================

-- Index on feature importance keys (for feature analysis)
CREATE INDEX IF NOT EXISTS idx_model_feature_importance 
    ON ml_models USING GIN (feature_importance);

-- Index on column metadata (for column type queries)
CREATE INDEX IF NOT EXISTS idx_dataset_column_metadata 
    ON datasets USING GIN (column_metadata);

-- Index on prediction input data (for debugging)
CREATE INDEX IF NOT EXISTS idx_prediction_input_data 
    ON predictions USING GIN (input_data);

-- Index on notification metadata
CREATE INDEX IF NOT EXISTS idx_notification_metadata 
    ON notifications USING GIN (metadata);

-- ===========================================
-- STATISTICS FOR QUERY OPTIMIZER
-- ===========================================

-- Analyze tables to update statistics
ANALYZE users;
ANALYZE datasets;
ANALYZE ml_models;
ANALYZE predictions;
ANALYZE notifications;
ANALYZE activity_logs;
ANALYZE api_keys;
ANALYZE user_sessions;
ANALYZE webhooks;
ANALYZE export_jobs;
ANALYZE user_preferences;

File: backend/src/main/resources/db/migration/V9__create_views_and_functions.sql
sql-- ============================================================
-- V9: Create views and utility functions
-- ============================================================

-- ===========================================
-- VIEWS FOR COMMON QUERIES
-- ===========================================

-- User dashboard summary view
CREATE OR REPLACE VIEW v_user_dashboard_summary AS
SELECT 
    u.id AS user_id,
    COUNT(DISTINCT d.id) FILTER (WHERE d.deleted = FALSE) AS total_datasets,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status IN ('READY', 'TRAINING')) AS total_models,
    COUNT(DISTINCT p.id) AS total_predictions,
    AVG(m.accuracy) FILTER (WHERE m.status = 'READY') AS avg_model_accuracy,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status = 'READY') AS ready_models,
    COUNT(DISTINCT m.id) FILTER (WHERE m.status = 'TRAINING') AS training_models,
    COALESCE(SUM(d.file_size_bytes) FILTER (WHERE d.deleted = FALSE), 0) AS total_dataset_size,
    COALESCE(SUM(m.model_size_bytes), 0) AS total_model_size,
    MAX(p.created_at) AS last_prediction_at,
    MAX(m.trained_at) AS last_model_trained_at
FROM users u
LEFT JOIN datasets d ON d.user_id = u.id
LEFT JOIN ml_models m ON m.user_id = u.id
LEFT JOIN predictions p ON p.user_id = u.id
GROUP BY u.id;

-- Model performance summary view
CREATE OR REPLACE VIEW v_model_performance AS
SELECT 
    m.id AS model_id,
    m.user_id,
    m.name,
    m.model_type,
    m.algorithm,
    m.status,
    m.accuracy,
    m.precision_score,
    m.recall_score,
    m.f1_score,
    m.mse,
    m.rmse,
    m.mae,
    m.r2_score,
    m.training_duration_ms,
    m.training_samples,
    m.test_samples,
    m.prediction_count,
    m.created_at,
    m.trained_at,
    d.name AS dataset_name,
    d.row_count AS dataset_rows,
    array_length(m.feature_columns::text[]::text[], 1) AS feature_count
FROM ml_models m
JOIN datasets d ON m.dataset_id = d.id;

-- Recent activity view with formatted data
CREATE OR REPLACE VIEW v_recent_activity AS
SELECT 
    al.id,
    al.user_id,
    al.action,
    CASE 
        WHEN al.action = 'MODEL_TRAINING_COMPLETED' THEN 'Model Training Complete'
        WHEN al.action = 'MODEL_TRAINING_FAILED' THEN 'Training Failed'
        WHEN al.action = 'DATASET_UPLOADED' THEN 'Dataset Uploaded'
        WHEN al.action = 'PREDICTION_MADE' THEN 'Prediction Made'
        WHEN al.action = 'LOGIN_SUCCESS' THEN 'Successful Login'
        ELSE REPLACE(al.action::text, '_', ' ')
    END AS action_display,
    CASE 
        WHEN al.action LIKE 'MODEL%' THEN '🤖'
        WHEN al.action LIKE 'DATASET%' THEN '📁'
        WHEN al.action LIKE 'PREDICTION%' THEN '🔮'
        WHEN al.action LIKE 'LOGIN%' THEN '🔐'
        WHEN al.action LIKE 'API_KEY%' THEN '🔑'
        ELSE '📋'
    END AS icon,
    al.resource_type,
    al.resource_id,
    al.resource_name,
    al.description,
    al.success,
    al.ip_address,
    al.device_info,
    al.location,
    al.created_at,
    CASE 
        WHEN al.resource_type = 'MODEL' THEN '/models/' || al.resource_id
        WHEN al.resource_type = 'DATASET' THEN '/datasets/' || al.resource_id
        WHEN al.resource_type = 'PREDICTION' THEN '/predictions/' || al.resource_id
        ELSE NULL
    END AS action_url
FROM activity_logs al
ORDER BY al.created_at DESC;

-- ===========================================
-- UTILITY FUNCTIONS
-- ===========================================

-- Function to get user storage usage
CREATE OR REPLACE FUNCTION get_user_storage_usage(p_user_id BIGINT)
RETURNS TABLE(
    dataset_size BIGINT,
    model_size BIGINT,
    total_size BIGINT,
    dataset_count INTEGER,
    model_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(SUM(d.file_size_bytes), 0)::BIGINT AS dataset_size,
        COALESCE(SUM(m.model_size_bytes), 0)::BIGINT AS model_size,
        (COALESCE(SUM(d.file_size_bytes), 0) + COALESCE(SUM(m.model_size_bytes), 0))::BIGINT AS total_size,
        COUNT(DISTINCT d.id)::INTEGER AS dataset_count,
        COUNT(DISTINCT m.id)::INTEGER AS model_count
    FROM users u
    LEFT JOIN datasets d ON d.user_id = u.id AND d.deleted = FALSE
    LEFT JOIN ml_models m ON m.user_id = u.id AND m.status != 'ARCHIVED'
    WHERE u.id = p_user_id
    GROUP BY u.id;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up expired data
CREATE OR REPLACE FUNCTION cleanup_expired_data()
RETURNS TABLE(
    expired_sessions INTEGER,
    expired_exports INTEGER,
    expired_notifications INTEGER
) AS $$
DECLARE
    v_expired_sessions INTEGER;
    v_expired_exports INTEGER;
    v_expired_notifications INTEGER;
BEGIN
    -- Deactivate expired sessions
    UPDATE user_sessions 
    SET is_active = FALSE, revoked_at = NOW(), revocation_reason = 'Session expired'
    WHERE is_active = TRUE AND expires_at < NOW();
    GET DIAGNOSTICS v_expired_sessions = ROW_COUNT;

    -- Mark expired exports
    UPDATE export_jobs 
    SET status = 'EXPIRED'
    WHERE status = 'COMPLETED' AND expires_at < NOW();
    GET DIAGNOSTICS v_expired_exports = ROW_COUNT;

    -- Delete old expired notifications (older than 90 days)
    DELETE FROM notifications 
    WHERE expires_at IS NOT NULL AND expires_at < NOW() - INTERVAL '90 days';
    GET DIAGNOSTICS v_expired_notifications = ROW_COUNT;

    RETURN QUERY SELECT v_expired_sessions, v_expired_exports, v_expired_notifications;
END;
$$ LANGUAGE plpgsql;

-- Function to get prediction statistics by date
CREATE OR REPLACE FUNCTION get_prediction_stats_by_date(
    p_user_id BIGINT,
    p_days INTEGER DEFAULT 30
)
RETURNS TABLE(
    prediction_date DATE,
    prediction_count BIGINT,
    avg_confidence DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        DATE(p.created_at) AS prediction_date,
        COUNT(*) AS prediction_count,
        AVG(p.confidence) AS avg_confidence
    FROM predictions p
    WHERE p.user_id = p_user_id
    AND p.created_at >= NOW() - (p_days || ' days')::INTERVAL
    GROUP BY DATE(p.created_at)
    ORDER BY prediction_date;
END;
$$ LANGUAGE plpgsql;

-- Function to get model version history
CREATE OR REPLACE FUNCTION get_model_versions(
    p_user_id BIGINT,
    p_base_name VARCHAR
)
RETURNS TABLE(
    model_id BIGINT,
    version INTEGER,
    accuracy DOUBLE PRECISION,
    status VARCHAR,
    trained_at TIMESTAMP,
    feature_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.id AS model_id,
        m.version,
        m.accuracy,
        m.status::VARCHAR,
        m.trained_at,
        array_length(m.feature_columns::text[]::text[], 1) AS feature_count
    FROM ml_models m
    WHERE m.user_id = p_user_id
    AND m.base_name = p_base_name
    ORDER BY m.version DESC;
END;
$$ LANGUAGE plpgsql;

-- ===========================================
-- TRIGGER FUNCTIONS
-- ===========================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to relevant tables
DROP TRIGGER IF EXISTS update_datasets_updated_at ON datasets;
CREATE TRIGGER update_datasets_updated_at
    BEFORE UPDATE ON datasets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_ml_models_updated_at ON ml_models;
CREATE TRIGGER update_ml_models_updated_at
    BEFORE UPDATE ON ml_models
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_preferences_updated_at ON user_preferences;
CREATE TRIGGER update_user_preferences_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_webhooks_updated_at ON webhooks;
CREATE TRIGGER update_webhooks_updated_at
    BEFORE UPDATE ON webhooks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to increment model prediction count
CREATE OR REPLACE FUNCTION increment_model_prediction_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE ml_models 
    SET prediction_count = prediction_count + 1,
        last_used_at = NOW()
    WHERE id = NEW.model_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply prediction count trigger
DROP TRIGGER IF EXISTS increment_prediction_count ON predictions;
CREATE TRIGGER increment_prediction_count
    AFTER INSERT ON predictions
    FOR EACH ROW
    EXECUTE FUNCTION increment_model_prediction_count();

File: backend/src/main/resources/db/migration/V10__seed_sample_data.sql (Optional - for development)
sql-- ============================================================
-- V10: Seed sample data for development/testing
-- This migration is optional and should be skipped in production
-- ============================================================

-- Only run in development environment
DO $$
BEGIN
    -- Check if we should seed data (e.g., based on environment variable or existing data)
    IF EXISTS (SELECT 1 FROM users LIMIT 1) THEN
        RAISE NOTICE 'Users already exist, skipping seed data';
        RETURN;
    END IF;

    -- Create a sample user (password: "password123")
    INSERT INTO users (
        email, 
        password, 
        first_name, 
        last_name, 
        organization, 
        role, 
        email_verified, 
        created_at
    ) VALUES (
        'demo@xai-forge.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMye.sHQ9hLj/UhJd.Sh.7YZEGdGFqJ5vC.',  -- bcrypt hash of 'password123'
        'Demo',
        'User',
        'XAI-Forge Demo',
        'Data Scientist',
        TRUE,
        NOW()
    );

    RAISE NOTICE 'Sample data seeded successfully';
END $$;


Database Migration Summary
Migration FilePurposeV2__add_user_profile_fields.sqlAdd profile fields to existing users tableV3__create_new_tables.sqlCreate all 8 new tables (predictions, api_keys, etc.)V4__update_datasets_models.sqlAdd new columns to datasets and ml_modelsV5__create_user_preferences_defaults.sqlDefault preferences + auto-create triggerV6__add_foreign_key_constraints.sqlAll foreign key relationshipsV7__add_check_constraints.sqlData validation constraintsV8__create_indexes_for_performance.sqlPerformance indexes including GIN for JSONBV9__create_views_and_functions.sqlViews, utility functions, triggersV10__seed_sample_data.sqlOptional development seed data

PHASE 8: FRONTEND API INTEGRATION
Now integrate the Figma components with the Spring Boot backend. The Figma export provides UI components - we need to add API connectivity, state management, and routing.
8.1 Install Additional Dependencies
Update package.json to add these dependencies:
json{
  "dependencies": {
    "axios": "^1.6.5",
    "react-router-dom": "^6.21.3",
    "zustand": "^4.5.0",
    "@tanstack/react-query": "^5.17.0",
    "zod": "^3.22.4",
    "@hookform/resolvers": "^3.3.4",
    "date-fns": "^3.3.0",
    "react-dropzone": "^14.2.3"
  },
  "devDependencies": {
    "@types/react": "^18.2.48",
    "@types/react-dom": "^18.2.18",
    "typescript": "^5.3.3",
    "vitest": "^1.2.2",
    "@testing-library/react": "^14.2.0",
    "msw": "^2.1.5"
  }
}
8.2 Create TypeScript Types
Create src/types/index.ts matching backend DTOs:
typescript// ============================================
// XAI-Forge TypeScript Types
// ============================================

// User & Authentication
export interface User {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  organization: string | null;
  role: string | null;
  location: string | null;
  bio: string | null;
  profileImageUrl: string | null;
  emailVerified: boolean;
  twoFactorEnabled: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
  twoFactorCode?: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

// Dataset Types
export type DatasetStatus = 'UPLOADING' | 'PROCESSING' | 'READY' | 'ERROR' | 'DELETED';

export interface ColumnMetadata {
  name: string;
  type: 'NUMERIC' | 'CATEGORICAL' | 'TEXT' | 'DATETIME' | 'BOOLEAN' | 'ID';
  nullCount: number;
  uniqueCount: number;
  sampleValues?: string[];
}

export interface Dataset {
  id: number;
  name: string;
  description: string | null;
  fileName: string;
  filePath: string;
  fileSizeBytes: number;
  mimeType: string;
  rowCount: number;
  columnCount: number;
  status: DatasetStatus;
  processingError: string | null;
  columnMetadata: ColumnMetadata[] | null;
  columnNames: string[] | null;
  targetColumn: string | null;
  qualityScore: number | null;
  createdAt: string;
}

// Model Types
export type ModelStatus = 'PENDING' | 'TRAINING' | 'READY' | 'FAILED' | 'ARCHIVED';
export type ModelType = 'CLASSIFICATION' | 'REGRESSION';
export type Algorithm = 'LOGISTIC_REGRESSION' | 'RANDOM_FOREST' | 'GRADIENT_BOOSTING' | 'LINEAR_REGRESSION' | 'DECISION_TREE' | 'SVM';

export interface FeatureImportance {
  feature: string;
  importance: number;
}

export interface Model {
  id: number;
  name: string;
  description: string | null;
  version: number;
  baseName: string;
  modelType: ModelType;
  algorithm: Algorithm;
  status: ModelStatus;
  accuracy: number | null;
  precisionScore: number | null;
  recallScore: number | null;
  f1Score: number | null;
  mse: number | null;
  rmse: number | null;
  mae: number | null;
  r2Score: number | null;
  featureImportance: FeatureImportance[] | null;
  trainingProgress: number;
  trainingStep: string | null;
  trainingError: string | null;
  datasetId: number;
  datasetName: string;
  predictionCount: number;
  createdAt: string;
  trainedAt: string | null;
}

export interface TrainModelRequest {
  name: string;
  description?: string;
  datasetId: number;
  targetColumn: string;
  featureColumns: string[];
  modelType: ModelType;
  algorithm: Algorithm;
  trainTestSplit?: number;
  hyperparameters?: Record<string, unknown>;
}

// Prediction Types
export interface Prediction {
  id: number;
  modelId: number;
  modelName: string;
  predictionResult: string;
  confidence: number;
  inputData: Record<string, unknown>;
  explanation: Record<string, number> | null;
  explanationSummary: string | null;
  predictionTimeMs: number;
  explanationTimeMs: number | null;
  createdAt: string;
}

export interface MakePredictionRequest {
  modelId: number;
  inputData: Record<string, unknown>;
  generateExplanation?: boolean;
}

// Dashboard Types
export interface DashboardSummary {
  totalDatasets: number;
  totalModels: number;
  totalPredictions: number;
  averageModelAccuracy: number;
  datasetsThisWeek: number;
  modelsThisWeek: number;
  predictionsLast30Days: number;
  activeModels: number;
}

export interface ActivityFeedItem {
  id: number;
  type: 'model' | 'dataset' | 'prediction' | 'security';
  icon: string;
  title: string;
  subtitle: string;
  timestamp: string;
  actionUrl: string;
}

export interface UsageTrend {
  date: string;
  predictions: number;
}

// Notification Types
export type NotificationType = 'MODEL_TRAINED' | 'MODEL_FAILED' | 'DATASET_UPLOADED' | 'SECURITY_ALERT' | 'EXPORT_READY' | 'SYSTEM';
export type NotificationPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';

export interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  metadata: Record<string, unknown> | null;
  isRead: boolean;
  readAt: string | null;
  priority: NotificationPriority;
  actionUrl: string | null;
  actionLabel: string | null;
  createdAt: string;
}

// API Key Types
export type ApiKeyEnvironment = 'PRODUCTION' | 'DEVELOPMENT' | 'STAGING';

export interface ApiKey {
  id: number;
  name: string;
  keyPreview: string;
  environment: ApiKeyEnvironment;
  permissions: string[];
  active: boolean;
  lastUsedAt: string | null;
  usageCount: number;
  expiresAt: string | null;
  createdAt: string;
}

export interface CreateApiKeyRequest {
  name: string;
  environment: ApiKeyEnvironment;
  permissions: string[];
  description?: string;
  expiresAt?: string;
}

export interface CreateApiKeyResponse extends ApiKey {
  key: string; // Full key shown only once
}

// Session Types
export interface Session {
  id: number;
  deviceInfo: string;
  ipAddress: string;
  location: string | null;
  lastActiveAt: string;
  createdAt: string;
  isCurrentSession: boolean;
}

export interface LoginHistory {
  success: boolean;
  deviceInfo: string;
  ipAddress: string;
  location: string | null;
  timestamp: string;
}

// Activity Log Types
export interface ActivityLog {
  id: number;
  action: string;
  actionDisplayName: string;
  resourceType: string | null;
  resourceId: number | null;
  resourceName: string | null;
  description: string | null;
  metadata: Record<string, unknown> | null;
  success: boolean;
  errorMessage: string | null;
  ipAddress: string | null;
  deviceInfo: string | null;
  location: string | null;
  durationMs: number | null;
  createdAt: string;
}

// Webhook Types
export interface Webhook {
  id: number;
  name: string;
  url: string;
  events: string[];
  description: string | null;
  active: boolean;
  autoDisabled: boolean;
  lastTriggeredAt: string | null;
  lastResponseCode: number | null;
  failureCount: number;
  successCount: number;
  createdAt: string;
}

export interface CreateWebhookRequest {
  name: string;
  url: string;
  events: string[];
  description?: string;
}

// Export Job Types
export type ExportStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'EXPIRED';
export type ExportType = 'FULL' | 'DATASETS' | 'MODELS' | 'PREDICTIONS' | 'ACTIVITY';

export interface ExportJob {
  id: number;
  status: ExportStatus;
  exportType: ExportType;
  includeItems: string[];
  format: string;
  progress: number;
  currentStep: string | null;
  fileSizeBytes: number | null;
  errorMessage: string | null;
  createdAt: string;
  completedAt: string | null;
  expiresAt: string | null;
  downloadCount: number;
}

// Pagination
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

// Common Response Types
export interface MessageResponse {
  message: string;
}

export interface BulkDeleteResponse {
  deletedCount: number;
}

// Model Comparison Types
export interface ModelComparison {
  models: Model[];
  metricsComparison: Record<string, Record<number, number>>;
  featureImportanceComparison: Record<string, Record<number, number>>;
  bestModelId: number;
  recommendations: string[];
  modelType: ModelType;
}

// User Preferences
export interface UserPreferences {
  theme: 'DARK' | 'LIGHT' | 'SYSTEM';
  accentColor: string;
  displayDensity: 'COMFORTABLE' | 'DEFAULT' | 'COMPACT';
  emailNotifications: Record<string, boolean>;
  inAppNotifications: Record<string, boolean>;
  timezone: string;
  defaultClassificationAlgorithm: Algorithm;
  defaultRegressionAlgorithm: Algorithm;
  predictionRetentionDays: number;
  itemsPerPage: number;
}

// Two-Factor Auth
export interface TwoFactorSetup {
  secret: string;
  qrCodeDataUri: string;
  backupCodes: string[];
}
8.3 Create API Client
Create src/api/client.ts:
typescriptimport axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// Token storage keys
const ACCESS_TOKEN_KEY = 'xai_access_token';
const REFRESH_TOKEN_KEY = 'xai_refresh_token';

// Token helpers
export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);
export const setTokens = (access: string, refresh: string) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, access);
  localStorage.setItem(REFRESH_TOKEN_KEY, refresh);
};
export const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // If 401 and not already retrying, try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const refreshToken = getRefreshToken();
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });
          const { token, refreshToken: newRefreshToken } = response.data;
          setTokens(token, newRefreshToken);

          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        } catch (refreshError) {
          clearTokens();
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      }
    }

    return Promise.reject(error);
  }
);

// File upload helper
export const uploadFile = async (
  url: string,
  file: File,
  additionalData?: Record<string, string>,
  onProgress?: (progress: number) => void
) => {
  const formData = new FormData();
  formData.append('file', file);
  
  if (additionalData) {
    Object.entries(additionalData).forEach(([key, value]) => {
      formData.append(key, value);
    });
  }

  return apiClient.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(progress);
      }
    },
  });
};

// File download helper
export const downloadFile = async (url: string, filename: string) => {
  const response = await apiClient.get(url, { responseType: 'blob' });
  const blob = new Blob([response.data]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
};

export default apiClient;
8.4 Create API Endpoint Modules
Create src/api/auth.ts:
typescriptimport apiClient, { setTokens, clearTokens } from './client';
import { AuthResponse, LoginRequest, RegisterRequest, User, MessageResponse } from '../types';

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', data);
    setTokens(response.data.token, response.data.refreshToken);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/register', data);
    setTokens(response.data.token, response.data.refreshToken);
    return response.data;
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient.post('/auth/logout');
    } finally {
      clearTokens();
    }
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },

  forgotPassword: async (email: string): Promise<MessageResponse> => {
    const response = await apiClient.post<MessageResponse>('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token: string, newPassword: string): Promise<MessageResponse> => {
    const response = await apiClient.post<MessageResponse>('/auth/reset-password', { token, newPassword });
    return response.data;
  },
};
Create src/api/datasets.ts:
typescriptimport apiClient, { uploadFile } from './client';
import { Dataset, PaginatedResponse, PaginationParams, MessageResponse } from '../types';

export const datasetsApi = {
  getAll: async (params?: PaginationParams): Promise<PaginatedResponse<Dataset>> => {
    const response = await apiClient.get<PaginatedResponse<Dataset>>('/datasets', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Dataset> => {
    const response = await apiClient.get<Dataset>(`/datasets/${id}`);
    return response.data;
  },

  upload: async (
    file: File,
    name: string,
    description?: string,
    onProgress?: (progress: number) => void
  ): Promise<Dataset> => {
    const response = await uploadFile(
      '/datasets/upload',
      file,
      { name, description: description || '' },
      onProgress
    );
    return response.data;
  },

  update: async (id: number, data: { name?: string; description?: string }): Promise<Dataset> => {
    const response = await apiClient.put<Dataset>(`/datasets/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/datasets/${id}`);
    return response.data;
  },

  getPreview: async (id: number, rows?: number): Promise<Record<string, unknown>[]> => {
    const response = await apiClient.get(`/datasets/${id}/preview`, { params: { rows } });
    return response.data;
  },

  getColumns: async (id: number): Promise<string[]> => {
    const response = await apiClient.get(`/datasets/${id}/columns`);
    return response.data;
  },

  analyzeColumn: async (id: number, column: string): Promise<Record<string, unknown>> => {
    const response = await apiClient.get(`/datasets/${id}/columns/${column}/analyze`);
    return response.data;
  },
};
Create src/api/models.ts:
typescriptimport apiClient from './client';
import { Model, TrainModelRequest, PaginatedResponse, PaginationParams, MessageResponse, ModelComparison } from '../types';

export const modelsApi = {
  getAll: async (params?: PaginationParams & { status?: string; type?: string }): Promise<PaginatedResponse<Model>> => {
    const response = await apiClient.get<PaginatedResponse<Model>>('/models', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Model> => {
    const response = await apiClient.get<Model>(`/models/${id}`);
    return response.data;
  },

  train: async (data: TrainModelRequest): Promise<Model> => {
    const response = await apiClient.post<Model>('/models/train', data);
    return response.data;
  },

  getTrainingStatus: async (id: number): Promise<{ status: string; progress: number; step: string | null; error: string | null }> => {
    const response = await apiClient.get(`/models/${id}/status`);
    return response.data;
  },

  delete: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/models/${id}`);
    return response.data;
  },

  archive: async (id: number): Promise<Model> => {
    const response = await apiClient.post<Model>(`/models/${id}/archive`);
    return response.data;
  },

  getReadyModels: async (): Promise<Model[]> => {
    const response = await apiClient.get<Model[]>('/models/ready');
    return response.data;
  },

  compare: async (modelIds: number[]): Promise<ModelComparison> => {
    const response = await apiClient.post<ModelComparison>('/models/compare', { modelIds });
    return response.data;
  },

  getVersions: async (baseName: string): Promise<Model[]> => {
    const response = await apiClient.get<Model[]>(`/models/versions/${baseName}`);
    return response.data;
  },
};
Create src/api/predictions.ts:
typescriptimport apiClient, { downloadFile } from './client';
import { Prediction, MakePredictionRequest, PaginatedResponse, MessageResponse, BulkDeleteResponse } from '../types';

export const predictionsApi = {
  getAll: async (params?: { modelId?: number; page?: number; size?: number; startDate?: string; endDate?: string }): Promise<PaginatedResponse<Prediction>> => {
    const response = await apiClient.get<PaginatedResponse<Prediction>>('/predictions', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Prediction> => {
    const response = await apiClient.get<Prediction>(`/predictions/${id}`);
    return response.data;
  },

  predict: async (data: MakePredictionRequest): Promise<Prediction> => {
    const response = await apiClient.post<Prediction>('/predictions', data);
    return response.data;
  },

  delete: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/predictions/${id}`);
    return response.data;
  },

  bulkDelete: async (ids: number[]): Promise<BulkDeleteResponse> => {
    const response = await apiClient.post<BulkDeleteResponse>('/predictions/bulk-delete', { ids });
    return response.data;
  },

  reExplain: async (id: number): Promise<Prediction> => {
    const response = await apiClient.post<Prediction>(`/predictions/${id}/re-explain`);
    return response.data;
  },

  exportCsv: async (params?: { modelId?: number; startDate?: string; endDate?: string }): Promise<void> => {
    await downloadFile('/predictions/export?format=csv', 'predictions.csv');
  },

  exportJson: async (params?: { modelId?: number; startDate?: string; endDate?: string }): Promise<void> => {
    await downloadFile('/predictions/export?format=json', 'predictions.json');
  },
};
Create src/api/dashboard.ts:
typescriptimport apiClient from './client';
import { DashboardSummary, ActivityFeedItem, UsageTrend, Model } from '../types';

export const dashboardApi = {
  getSummary: async (): Promise<DashboardSummary> => {
    const response = await apiClient.get<DashboardSummary>('/dashboard/summary');
    return response.data;
  },

  getRecentActivity: async (limit?: number): Promise<ActivityFeedItem[]> => {
    const response = await apiClient.get<ActivityFeedItem[]>('/dashboard/recent-activity', { params: { limit } });
    return response.data;
  },

  getModelsByType: async (): Promise<{ name: string; value: number }[]> => {
    const response = await apiClient.get('/dashboard/models-by-type');
    return response.data;
  },

  getUsageTrend: async (days?: number): Promise<UsageTrend[]> => {
    const response = await apiClient.get<UsageTrend[]>('/dashboard/usage-trend', { params: { days } });
    return response.data;
  },

  getRecentModels: async (limit?: number): Promise<Model[]> => {
    const response = await apiClient.get<Model[]>('/dashboard/recent-models', { params: { limit } });
    return response.data;
  },

  getQuickStats: async (): Promise<{ predictionsToday: number; modelsInTraining: number; storageUsedBytes: number }> => {
    const response = await apiClient.get('/dashboard/quick-stats');
    return response.data;
  },
};
Create src/api/user.ts:
typescriptimport apiClient from './client';
import { User, UserPreferences, TwoFactorSetup, MessageResponse } from '../types';

export const userApi = {
  getProfile: async (): Promise<User> => {
    const response = await apiClient.get<User>('/users/me');
    return response.data;
  },

  updateProfile: async (data: Partial<User>): Promise<User> => {
    const response = await apiClient.put<User>('/users/me', data);
    return response.data;
  },

  uploadAvatar: async (file: File): Promise<{ avatarUrl: string }> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  deleteAvatar: async (): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>('/users/me/avatar');
    return response.data;
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<MessageResponse> => {
    const response = await apiClient.put<MessageResponse>('/users/me/password', { currentPassword, newPassword, confirmPassword: newPassword });
    return response.data;
  },

  enable2FA: async (): Promise<TwoFactorSetup> => {
    const response = await apiClient.post<TwoFactorSetup>('/users/me/2fa/enable');
    return response.data;
  },

  verify2FA: async (code: string): Promise<{ valid: boolean; backupCodes?: string[] }> => {
    const response = await apiClient.post('/users/me/2fa/verify', { code });
    return response.data;
  },

  disable2FA: async (code: string): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>('/users/me/2fa', { data: { code } });
    return response.data;
  },

  getPreferences: async (): Promise<UserPreferences> => {
    const response = await apiClient.get<UserPreferences>('/settings/preferences');
    return response.data;
  },

  updatePreferences: async (data: Partial<UserPreferences>): Promise<UserPreferences> => {
    const response = await apiClient.put<UserPreferences>('/settings/preferences', data);
    return response.data;
  },

  deleteAccount: async (password: string): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>('/users/me', { data: { password } });
    return response.data;
  },
};
Create src/api/notifications.ts:
typescriptimport apiClient from './client';
import { Notification, PaginatedResponse, MessageResponse } from '../types';

export const notificationsApi = {
  getAll: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<Notification>> => {
    const response = await apiClient.get<PaginatedResponse<Notification>>('/notifications', { params });
    return response.data;
  },

  getUnreadCount: async (): Promise<{ count: number }> => {
    const response = await apiClient.get('/notifications/unread-count');
    return response.data;
  },

  markAsRead: async (id: number): Promise<Notification> => {
    const response = await apiClient.put<Notification>(`/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<MessageResponse> => {
    const response = await apiClient.put<MessageResponse>('/notifications/read-all');
    return response.data;
  },

  delete: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/notifications/${id}`);
    return response.data;
  },
};
Create src/api/sessions.ts:
typescriptimport apiClient from './client';
import { Session, LoginHistory, MessageResponse } from '../types';

export const sessionsApi = {
  getActiveSessions: async (): Promise<Session[]> => {
    const response = await apiClient.get<Session[]>('/sessions');
    return response.data;
  },

  revokeSession: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/sessions/${id}`);
    return response.data;
  },

  revokeAllOtherSessions: async (): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>('/sessions/others');
    return response.data;
  },

  getLoginHistory: async (limit?: number): Promise<LoginHistory[]> => {
    const response = await apiClient.get<LoginHistory[]>('/sessions/history', { params: { limit } });
    return response.data;
  },
};
Create src/api/apiKeys.ts:
typescriptimport apiClient from './client';
import { ApiKey, CreateApiKeyRequest, CreateApiKeyResponse, MessageResponse } from '../types';

export const apiKeysApi = {
  getAll: async (): Promise<ApiKey[]> => {
    const response = await apiClient.get<ApiKey[]>('/keys');
    return response.data;
  },

  create: async (data: CreateApiKeyRequest): Promise<CreateApiKeyResponse> => {
    const response = await apiClient.post<CreateApiKeyResponse>('/keys', data);
    return response.data;
  },

  revoke: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/keys/${id}`);
    return response.data;
  },
};
Create src/api/webhooks.ts:
typescriptimport apiClient from './client';
import { Webhook, CreateWebhookRequest, MessageResponse } from '../types';

export const webhooksApi = {
  getAll: async (): Promise<Webhook[]> => {
    const response = await apiClient.get<Webhook[]>('/webhooks');
    return response.data;
  },

  getById: async (id: number): Promise<Webhook> => {
    const response = await apiClient.get<Webhook>(`/webhooks/${id}`);
    return response.data;
  },

  create: async (data: CreateWebhookRequest): Promise<Webhook> => {
    const response = await apiClient.post<Webhook>('/webhooks', data);
    return response.data;
  },

  update: async (id: number, data: Partial<CreateWebhookRequest & { active: boolean }>): Promise<Webhook> => {
    const response = await apiClient.put<Webhook>(`/webhooks/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(`/webhooks/${id}`);
    return response.data;
  },

  test: async (id: number): Promise<{ success: boolean; statusCode: number; responseBody: string; responseTimeMs: number }> => {
    const response = await apiClient.post(`/webhooks/${id}/test`);
    return response.data;
  },
};
Create src/api/activity.ts:
typescriptimport apiClient, { downloadFile } from './client';
import { ActivityLog, PaginatedResponse } from '../types';

export const activityApi = {
  getAll: async (params?: { page?: number; size?: number; action?: string; startDate?: string; endDate?: string }): Promise<PaginatedResponse<ActivityLog>> => {
    const response = await apiClient.get<PaginatedResponse<ActivityLog>>('/activity', { params });
    return response.data;
  },

  getById: async (id: number): Promise<ActivityLog> => {
    const response = await apiClient.get<ActivityLog>(`/activity/${id}`);
    return response.data;
  },

  exportCsv: async (): Promise<void> => {
    await downloadFile('/activity/export', 'activity_log.csv');
  },
};
Create src/api/exports.ts:
typescriptimport apiClient from './client';
import { ExportJob, MessageResponse } from '../types';

export const exportsApi = {
  getAll: async (): Promise<ExportJob[]> => {
    const response = await apiClient.get<ExportJob[]>('/export');
    return response.data;
  },

  requestExport: async (data: { exportType: string; includeItems: string[]; format: string }): Promise<ExportJob> => {
    const response = await apiClient.post<ExportJob>('/export/full', data);
    return response.data;
  },

  getStatus: async (jobId: number): Promise<ExportJob> => {
    const response = await apiClient.get<ExportJob>(`/export/${jobId}/status`);
    return response.data;
  },

  download: async (jobId: number): Promise<Blob> => {
    const response = await apiClient.get(`/export/${jobId}/download`, { responseType: 'blob' });
    return response.data;
  },
};
Create src/api/index.ts to export all APIs:
typescriptexport * from './client';
export { authApi } from './auth';
export { datasetsApi } from './datasets';
export { modelsApi } from './models';
export { predictionsApi } from './predictions';
export { dashboardApi } from './dashboard';
export { userApi } from './user';
export { notificationsApi } from './notifications';
export { sessionsApi } from './sessions';
export { apiKeysApi } from './apiKeys';
export { webhooksApi } from './webhooks';
export { activityApi } from './activity';
export { exportsApi } from './exports';
8.5 Create Zustand Stores
Create src/stores/authStore.ts:
typescriptimport { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, LoginRequest, RegisterRequest } from '../types';
import { authApi, getAccessToken } from '../api';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  fetchUser: () => Promise<void>;
  setUser: (user: User | null) => void;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: !!getAccessToken(),
      isLoading: false,
      error: null,

      login: async (credentials) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.login(credentials);
          set({ user: response.user, isAuthenticated: true, isLoading: false });
        } catch (error: any) {
          set({ isLoading: false, error: error.response?.data?.message || 'Login failed' });
          throw error;
        }
      },

      register: async (data) => {
        set({ isLoading: true, error: null });
        try {
          const response = await authApi.register(data);
          set({ user: response.user, isAuthenticated: true, isLoading: false });
        } catch (error: any) {
          set({ isLoading: false, error: error.response?.data?.message || 'Registration failed' });
          throw error;
        }
      },

      logout: async () => {
        try {
          await authApi.logout();
        } finally {
          set({ user: null, isAuthenticated: false });
        }
      },

      fetchUser: async () => {
        if (!getAccessToken()) {
          set({ user: null, isAuthenticated: false });
          return;
        }
        try {
          const user = await authApi.getCurrentUser();
          set({ user, isAuthenticated: true });
        } catch {
          set({ user: null, isAuthenticated: false });
        }
      },

      setUser: (user) => set({ user, isAuthenticated: !!user }),
      clearError: () => set({ error: null }),
    }),
    { name: 'xai-auth', partialize: (state) => ({ isAuthenticated: state.isAuthenticated }) }
  )
);
Create src/stores/uiStore.ts:
typescriptimport { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UIState {
  sidebarCollapsed: boolean;
  notificationsPanelOpen: boolean;
  theme: 'dark' | 'light' | 'system';
  
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  toggleNotificationsPanel: () => void;
  setNotificationsPanelOpen: (open: boolean) => void;
  setTheme: (theme: 'dark' | 'light' | 'system') => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarCollapsed: false,
      notificationsPanelOpen: false,
      theme: 'dark',

      toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
      setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),
      toggleNotificationsPanel: () => set((state) => ({ notificationsPanelOpen: !state.notificationsPanelOpen })),
      setNotificationsPanelOpen: (open) => set({ notificationsPanelOpen: open }),
      setTheme: (theme) => set({ theme }),
    }),
    { name: 'xai-ui' }
  )
);
Create src/stores/notificationsStore.ts:
typescriptimport { create } from 'zustand';
import { Notification } from '../types';
import { notificationsApi } from '../api';

interface NotificationsState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  
  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  deleteNotification: (id: number) => Promise<void>;
}

export const useNotificationsStore = create<NotificationsState>((set, get) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  fetchNotifications: async () => {
    set({ isLoading: true });
    try {
      const response = await notificationsApi.getAll({ size: 50 });
      set({ notifications: response.content, isLoading: false });
    } catch {
      set({ isLoading: false });
    }
  },

  fetchUnreadCount: async () => {
    try {
      const { count } = await notificationsApi.getUnreadCount();
      set({ unreadCount: count });
    } catch {}
  },

  markAsRead: async (id) => {
    await notificationsApi.markAsRead(id);
    set((state) => ({
      notifications: state.notifications.map((n) => (n.id === id ? { ...n, isRead: true } : n)),
      unreadCount: Math.max(0, state.unreadCount - 1),
    }));
  },

  markAllAsRead: async () => {
    await notificationsApi.markAllAsRead();
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, isRead: true })),
      unreadCount: 0,
    }));
  },

  deleteNotification: async (id) => {
    await notificationsApi.delete(id);
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },
}));
Create src/stores/index.ts:
typescriptexport { useAuthStore } from './authStore';
export { useUIStore } from './uiStore';
export { useNotificationsStore } from './notificationsStore';
8.6 Create React Router Setup
Update src/main.tsx:
typescriptimport { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'sonner';
import App from './App';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
        <Toaster position="top-right" richColors closeButton theme="dark" />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>
);
Update src/App.tsx to use React Router with the Figma components:
typescriptimport { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores';

// Import existing Figma components
import { Navigation } from './components/Navigation';
import { Dashboard } from './components/Dashboard';
import { Datasets } from './components/Datasets';
import { ModelTraining } from './components/ModelTraining';
import { ModelDetails } from './components/ModelDetails';
import { Predictions } from './components/Predictions';
import { UserProfile } from './components/UserProfile';
import { Settings } from './components/Settings';
import { NotificationCenter } from './components/NotificationCenter';
import { ModelComparison } from './components/ModelComparison';
import { PredictionHistory } from './components/PredictionHistory';
import { ActivityLog } from './components/ActivityLog';
import { ErrorPages } from './components/ErrorPages';

// Auth pages (you'll need to create these)
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';

// Protected Route wrapper
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

// Main Layout with Navigation
function MainLayout({ children }: { children: React.ReactNode }) {
  const { sidebarCollapsed, notificationsPanelOpen, setNotificationsPanelOpen } = useUIStore();

  return (
    <div className="min-h-screen bg-background">
      <Navigation onNotificationsClick={() => setNotificationsPanelOpen(!notificationsPanelOpen)} />
      <div className={`pt-16 transition-all duration-300 ${sidebarCollapsed ? 'pl-16' : 'pl-60'}`}>
        {children}
      </div>
      {notificationsPanelOpen && (
        <>
          <div className="fixed inset-0 bg-black/50 z-40" onClick={() => setNotificationsPanelOpen(false)} />
          <NotificationCenter onClose={() => setNotificationsPanelOpen(false)} />
        </>
      )}
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Auth Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected Routes */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <MainLayout><Dashboard /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/datasets" element={
        <ProtectedRoute>
          <MainLayout><Datasets /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/models" element={
        <ProtectedRoute>
          <MainLayout><ModelDetails /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/models/train" element={
        <ProtectedRoute>
          <MainLayout><ModelTraining /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/models/compare" element={
        <ProtectedRoute>
          <MainLayout><ModelComparison /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/predictions" element={
        <ProtectedRoute>
          <MainLayout><Predictions /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/predictions/history" element={
        <ProtectedRoute>
          <MainLayout><PredictionHistory /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/profile" element={
        <ProtectedRoute>
          <MainLayout><UserProfile /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/settings" element={
        <ProtectedRoute>
          <MainLayout><Settings /></MainLayout>
        </ProtectedRoute>
      } />

      <Route path="/activity" element={
        <ProtectedRoute>
          <MainLayout><ActivityLog /></MainLayout>
        </ProtectedRoute>
      } />

      {/* 404 */}
      <Route path="*" element={<ErrorPages type="404" />} />
    </Routes>
  );
}
8.7 Update Navigation Component for React Router
Modify src/components/Navigation.tsx to use React Router's Link and useNavigate:
typescript// At the top, add imports:
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore, useUIStore, useNotificationsStore } from '../stores';

// Replace the Navigation component interface and implementation:
interface NavigationProps {
  onNotificationsClick?: () => void;
}

export function Navigation({ onNotificationsClick }: NavigationProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { sidebarCollapsed, setSidebarCollapsed } = useUIStore();
  const { unreadCount } = useNotificationsStore();
  const { user, logout } = useAuthStore();

  const navItems = [
    { id: 'dashboard', path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { 
      id: 'datasets', 
      path: '/datasets',
      label: 'Datasets', 
      icon: Database,
      children: [
        { id: 'datasets-all', path: '/datasets', label: 'All Datasets' },
        { id: 'datasets-upload', path: '/datasets?upload=true', label: 'Upload New' },
      ]
    },
    { 
      id: 'models', 
      path: '/models',
      label: 'Models', 
      icon: BrainCircuit,
      children: [
        { id: 'models-all', path: '/models', label: 'All Models' },
        { id: 'models-train', path: '/models/train', label: 'Train New' },
        { id: 'models-compare', path: '/models/compare', label: 'Compare Models' },
      ]
    },
    { 
      id: 'predictions', 
      path: '/predictions',
      label: 'Predictions', 
      icon: Sparkles,
      children: [
        { id: 'predictions-new', path: '/predictions', label: 'New Prediction' },
        { id: 'predictions-history', path: '/predictions/history', label: 'History' },
      ]
    },
    { id: 'activity', path: '/activity', label: 'Activity', icon: Activity },
    { id: 'settings', path: '/settings', label: 'Settings', icon: Settings },
  ];

  const isActive = (path: string) => {
    if (path === '/dashboard') return location.pathname === '/dashboard';
    return location.pathname.startsWith(path);
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  // Rest of the component, replacing:
  // - onClick={() => onNavigate(item.id)} with Link to={item.path}
  // - currentPage === item.id with isActive(item.path)
  // - Badge count with unreadCount
  // - User menu logout with handleLogout
}
8.8 Create Login and Register Pages
Create src/pages/LoginPage.tsx:
typescriptimport { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { BrainCircuit, Mail, Lock, Eye, EyeOff } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { useAuthStore } from '../stores';
import { toast } from 'sonner';

export function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoading, error, clearError } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [twoFactorCode, setTwoFactorCode] = useState('');
  const [needs2FA, setNeeds2FA] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    try {
      await login({ email, password, twoFactorCode: needs2FA ? twoFactorCode : undefined });
      toast.success('Welcome back!');
      navigate('/dashboard');
    } catch (err: any) {
      if (err.response?.data?.requires2FA) {
        setNeeds2FA(true);
        toast.info('Please enter your 2FA code');
      } else {
        toast.error(err.response?.data?.message || 'Login failed');
      }
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center mb-4">
            <BrainCircuit className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-2xl font-semibold">Welcome back</h1>
          <p className="text-muted-foreground mt-1">Sign in to XAI-Forge</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="email">Email</Label>
            <div className="relative mt-2">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10"
                required
              />
            </div>
          </div>

          <div>
            <Label htmlFor="password">Password</Label>
            <div className="relative mt-2">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 pr-10"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {needs2FA && (
            <div>
              <Label htmlFor="twoFactorCode">Two-Factor Code</Label>
              <Input
                id="twoFactorCode"
                type="text"
                placeholder="000000"
                value={twoFactorCode}
                onChange={(e) => setTwoFactorCode(e.target.value)}
                className="mt-2 text-center tracking-widest"
                maxLength={6}
                required
              />
            </div>
          )}

          {error && <p className="text-error text-sm">{error}</p>}

          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign In'}
          </Button>
        </form>

        <div className="mt-6 text-center text-sm">
          <Link to="/forgot-password" className="text-primary hover:underline">
            Forgot password?
          </Link>
        </div>

        <div className="mt-4 text-center text-sm text-muted-foreground">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary hover:underline">
            Create account
          </Link>
        </div>
      </Card>
    </div>
  );
}
Create src/pages/RegisterPage.tsx:
typescriptimport { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { BrainCircuit, Mail, Lock, User, Eye, EyeOff } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card } from '../components/ui/card';
import { useAuthStore } from '../stores';
import { toast } from 'sonner';

export function RegisterPage() {
  const navigate = useNavigate();
  const { register, isLoading, error, clearError } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    if (password.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }

    try {
      await register({ email, password, firstName, lastName });
      toast.success('Account created successfully!');
      navigate('/dashboard');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center mb-4">
            <BrainCircuit className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-2xl font-semibold">Create Account</h1>
          <p className="text-muted-foreground mt-1">Get started with XAI-Forge</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="firstName">First Name</Label>
              <Input
                id="firstName"
                type="text"
                placeholder="John"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className="mt-2"
              />
            </div>
            <div>
              <Label htmlFor="lastName">Last Name</Label>
              <Input
                id="lastName"
                type="text"
                placeholder="Doe"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                className="mt-2"
              />
            </div>
          </div>

          <div>
            <Label htmlFor="email">Email</Label>
            <div className="relative mt-2">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10"
                required
              />
            </div>
          </div>

          <div>
            <Label htmlFor="password">Password</Label>
            <div className="relative mt-2">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="pl-10 pr-10"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            <p className="text-xs text-muted-foreground mt-1">Min 8 characters, 1 uppercase, 1 number</p>
          </div>

          <div>
            <Label htmlFor="confirmPassword">Confirm Password</Label>
            <Input
              id="confirmPassword"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="mt-2"
              required
            />
          </div>

          {error && <p className="text-error text-sm">{error}</p>}

          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? 'Creating account...' : 'Create Account'}
          </Button>
        </form>

        <div className="mt-6 text-center text-sm text-muted-foreground">
          Already have an account?{' '}
          <Link to="/login" className="text-primary hover:underline">
            Sign in
          </Link>
        </div>
      </Card>
    </div>
  );
}

PHASE 9: CONNECT FIGMA COMPONENTS TO API
Now update the existing Figma components to fetch real data from the API instead of using mock data.
9.1 Update Dashboard Component
In src/components/Dashboard.tsx, replace mock data with API calls:
typescript// Add at the top:
import { useQuery } from '@tanstack/react-query';
import { dashboardApi, modelsApi } from '../api';
import { Skeleton } from './ui/skeleton';

// Inside the component, replace mock data with:
export function Dashboard() {
  const navigate = useNavigate();

  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['dashboard', 'summary'],
    queryFn: dashboardApi.getSummary,
  });

  const { data: recentActivity, isLoading: activityLoading } = useQuery({
    queryKey: ['dashboard', 'activity'],
    queryFn: () => dashboardApi.getRecentActivity(5),
  });

  const { data: recentModels, isLoading: modelsLoading } = useQuery({
    queryKey: ['dashboard', 'models'],
    queryFn: () => dashboardApi.getRecentModels(5),
  });

  const { data: modelsByType } = useQuery({
    queryKey: ['dashboard', 'modelsByType'],
    queryFn: dashboardApi.getModelsByType,
  });

  const { data: usageTrend } = useQuery({
    queryKey: ['dashboard', 'usageTrend'],
    queryFn: () => dashboardApi.getUsageTrend(7),
  });

  const kpiData = summary ? [
    { label: 'Total Datasets', value: summary.totalDatasets.toString(), icon: Database, subtext: `+${summary.datasetsThisWeek} this week`, color: 'text-primary' },
    { label: 'Trained Models', value: summary.totalModels.toString(), icon: BrainCircuit, subtext: `${summary.activeModels} active`, color: 'text-secondary' },
    { label: 'Predictions Made', value: summary.predictionsLast30Days.toLocaleString(), icon: Target, subtext: 'Last 30 days', color: 'text-success' },
    { label: 'Avg. Model Accuracy', value: `${(summary.averageModelAccuracy * 100).toFixed(1)}%`, icon: TrendingUp, subtext: 'Across all models', color: 'text-warning' },
  ] : [];

  // Show loading skeleton while data loads
  if (summaryLoading) {
    return (
      <div className="p-8 space-y-8">
        <Skeleton className="h-24 w-full" />
        <div className="grid grid-cols-4 gap-6">
          {[1,2,3,4].map(i => <Skeleton key={i} className="h-32" />)}
        </div>
      </div>
    );
  }

  // Rest of the component using real data...
}
9.2 Update Datasets Component
In src/components/Datasets.tsx:
typescriptimport { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { datasetsApi } from '../api';
import { toast } from 'sonner';

export function Datasets() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ['datasets', page],
    queryFn: () => datasetsApi.getAll({ page, size: 20 }),
  });

  const deleteMutation = useMutation({
    mutationFn: datasetsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['datasets'] });
      toast.success('Dataset deleted');
    },
    onError: () => toast.error('Failed to delete dataset'),
  });

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this dataset?')) {
      deleteMutation.mutate(id);
    }
  };

  // Use data?.content for the datasets list
  // Use data?.totalPages for pagination
}
9.3 Update Model Training Component
In src/components/ModelTraining.tsx:
typescriptimport { useQuery, useMutation } from '@tanstack/react-query';
import { datasetsApi, modelsApi } from '../api';
import { toast } from 'sonner';

export function ModelTraining() {
  const navigate = useNavigate();
  const [selectedDataset, setSelectedDataset] = useState<number | null>(null);
  const [config, setConfig] = useState({
    name: '',
    targetColumn: '',
    featureColumns: [] as string[],
    modelType: 'CLASSIFICATION' as const,
    algorithm: 'RANDOM_FOREST' as const,
    trainTestSplit: 0.8,
  });

  // Fetch datasets for dropdown
  const { data: datasets } = useQuery({
    queryKey: ['datasets', 'ready'],
    queryFn: () => datasetsApi.getAll({ size: 100 }),
  });

  // Fetch columns when dataset is selected
  const { data: columns } = useQuery({
    queryKey: ['dataset', selectedDataset, 'columns'],
    queryFn: () => datasetsApi.getColumns(selectedDataset!),
    enabled: !!selectedDataset,
  });

  const trainMutation = useMutation({
    mutationFn: modelsApi.train,
    onSuccess: (model) => {
      toast.success('Model training started!');
      navigate(`/models/${model.id}`);
    },
    onError: (err: any) => {
      toast.error(err.response?.data?.message || 'Failed to start training');
    },
  });

  const handleSubmit = () => {
    if (!selectedDataset || !config.name || !config.targetColumn) {
      toast.error('Please fill in all required fields');
      return;
    }

    trainMutation.mutate({
      name: config.name,
      datasetId: selectedDataset,
      targetColumn: config.targetColumn,
      featureColumns: config.featureColumns,
      modelType: config.modelType,
      algorithm: config.algorithm,
      trainTestSplit: config.trainTestSplit,
    });
  };
}
9.4 Update Predictions Component
In src/components/Predictions.tsx:
typescriptimport { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { modelsApi, predictionsApi } from '../api';
import { toast } from 'sonner';

export function Predictions() {
  const queryClient = useQueryClient();
  const [selectedModel, setSelectedModel] = useState<number | null>(null);
  const [inputData, setInputData] = useState<Record<string, string>>({});

  // Fetch ready models for dropdown
  const { data: models } = useQuery({
    queryKey: ['models', 'ready'],
    queryFn: modelsApi.getReadyModels,
  });

  // Fetch selected model details to get feature columns
  const { data: modelDetails } = useQuery({
    queryKey: ['model', selectedModel],
    queryFn: () => modelsApi.getById(selectedModel!),
    enabled: !!selectedModel,
  });

  const predictMutation = useMutation({
    mutationFn: predictionsApi.predict,
    onSuccess: (prediction) => {
      toast.success(`Prediction: ${prediction.predictionResult} (${(prediction.confidence * 100).toFixed(1)}% confidence)`);
      queryClient.invalidateQueries({ queryKey: ['predictions'] });
    },
    onError: () => toast.error('Prediction failed'),
  });

  const handlePredict = () => {
    if (!selectedModel) {
      toast.error('Please select a model');
      return;
    }

    predictMutation.mutate({
      modelId: selectedModel,
      inputData: Object.fromEntries(
        Object.entries(inputData).map(([k, v]) => [k, isNaN(Number(v)) ? v : Number(v)])
      ),
      generateExplanation: true,
    });
  };
}
9.5 Update Settings Component
In src/components/Settings.tsx, connect each section to the API:
typescript// Security tab - sessions
const { data: sessions } = useQuery({
  queryKey: ['sessions'],
  queryFn: sessionsApi.getActiveSessions,
});

const { data: loginHistory } = useQuery({
  queryKey: ['loginHistory'],
  queryFn: () => sessionsApi.getLoginHistory(10),
});

const revokeSessionMutation = useMutation({
  mutationFn: sessionsApi.revokeSession,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['sessions'] });
    toast.success('Session revoked');
  },
});

// API Keys tab
const { data: apiKeys } = useQuery({
  queryKey: ['apiKeys'],
  queryFn: apiKeysApi.getAll,
});

const createApiKeyMutation = useMutation({
  mutationFn: apiKeysApi.create,
  onSuccess: (data) => {
    queryClient.invalidateQueries({ queryKey: ['apiKeys'] });
    // Show the full key to the user (only time it's visible)
    setNewApiKey(data.key);
    setApiKeyModalOpen(true);
  },
});

// 2FA
const enable2FAMutation = useMutation({
  mutationFn: userApi.enable2FA,
  onSuccess: (data) => {
    setTwoFactorSetup(data);
    setTwoFactorModalOpen(true);
  },
});

PHASE 10: ENVIRONMENT CONFIGURATION
10.1 Create Environment Files
Create .env.example:
env# API Configuration
VITE_API_BASE_URL=/api
VITE_API_TIMEOUT=30000

# Application
VITE_APP_NAME=XAI-Forge
VITE_APP_VERSION=1.0.0
VITE_APP_ENV=development

# Features
VITE_ENABLE_2FA=true
VITE_ENABLE_WEBHOOKS=false
VITE_ENABLE_ANALYTICS=false

# File Upload
VITE_MAX_UPLOAD_SIZE=52428800
VITE_ALLOWED_FILE_TYPES=.csv,.json,.xlsx

# Training
VITE_TRAINING_POLL_INTERVAL=2000
Create .env.development:
envVITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_ENV=development
VITE_ENABLE_DEBUG=true
Create .env.production:
envVITE_API_BASE_URL=/api
VITE_APP_ENV=production
VITE_ENABLE_DEBUG=false
VITE_ENABLE_ANALYTICS=true
10.2 Update Vite Config
Update vite.config.ts:
typescriptimport { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});

PHASE 11: UTILITY FUNCTIONS
Create src/utils/index.ts:
typescriptimport { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Tailwind class merge utility
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Date formatting
export function formatDate(date: string | Date, format: 'short' | 'long' | 'relative' = 'short'): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  
  if (format === 'relative') {
    const now = new Date();
    const diff = now.getTime() - d.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
  }
  
  return d.toLocaleDateString('en-US', {
    month: format === 'long' ? 'long' : 'short',
    day: 'numeric',
    year: 'numeric',
    ...(format === 'long' && { hour: '2-digit', minute: '2-digit' }),
  });
}

// Number formatting
export function formatNumber(num: number): string {
  return new Intl.NumberFormat('en-US').format(num);
}

export function formatPercentage(value: number, decimals = 1): string {
  return `${(value * 100).toFixed(decimals)}%`;
}

// File size formatting
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

// Status formatting
export function formatStatus(status: string): string {
  return status.charAt(0) + status.slice(1).toLowerCase().replace(/_/g, ' ');
}

export function getStatusColor(status: string): string {
  const colors: Record<string, string> = {
    READY: 'text-success',
    TRAINING: 'text-warning',
    PENDING: 'text-warning',
    FAILED: 'text-error',
    ERROR: 'text-error',
    PROCESSING: 'text-warning',
  };
  return colors[status] || 'text-muted-foreground';
}

export function getStatusBadgeClass(status: string): string {
  const classes: Record<string, string> = {
    READY: 'bg-success/10 text-success border-success/20',
    TRAINING: 'bg-warning/10 text-warning border-warning/20 animate-pulse',
    PENDING: 'bg-warning/10 text-warning border-warning/20',
    FAILED: 'bg-error/10 text-error border-error/20',
    ERROR: 'bg-error/10 text-error border-error/20',
  };
  return classes[status] || 'bg-muted text-muted-foreground';
}

// Algorithm formatting
export function formatAlgorithm(algorithm: string): string {
  const names: Record<string, string> = {
    LOGISTIC_REGRESSION: 'Logistic Regression',
    RANDOM_FOREST: 'Random Forest',
    GRADIENT_BOOSTING: 'Gradient Boosting',
    LINEAR_REGRESSION: 'Linear Regression',
    DECISION_TREE: 'Decision Tree',
    SVM: 'Support Vector Machine',
  };
  return names[algorithm] || algorithm;
}

// Validation
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function isValidPassword(password: string): { valid: boolean; errors: string[] } {
  const errors: string[] = [];
  if (password.length < 8) errors.push('At least 8 characters');
  if (!/[A-Z]/.test(password)) errors.push('One uppercase letter');
  if (!/[a-z]/.test(password)) errors.push('One lowercase letter');
  if (!/[0-9]/.test(password)) errors.push('One number');
  return { valid: errors.length === 0, errors };
}