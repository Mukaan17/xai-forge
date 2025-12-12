package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
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
    private Map<String, Boolean> emailNotifications = createDefaultEmailNotifications();

    /**
     * In-app notification preferences per event type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "in_app_notifications", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> inAppNotifications = createDefaultInAppNotifications();

    /**
     * Push notification preferences per event type.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "push_notifications", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> pushNotifications = createDefaultPushNotifications();

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

    private static Map<String, Boolean> createDefaultEmailNotifications() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("MODEL_TRAINED", true);
        map.put("MODEL_FAILED", true);
        map.put("DATASET_UPLOADED", false);
        map.put("SECURITY_ALERT", true);
        map.put("WEEKLY_SUMMARY", true);
        map.put("EXPORT_READY", true);
        return map;
    }

    private static Map<String, Boolean> createDefaultInAppNotifications() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("MODEL_TRAINED", true);
        map.put("MODEL_FAILED", true);
        map.put("DATASET_UPLOADED", true);
        map.put("SECURITY_ALERT", true);
        map.put("WEEKLY_SUMMARY", false);
        map.put("EXPORT_READY", true);
        return map;
    }

    private static Map<String, Boolean> createDefaultPushNotifications() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("MODEL_TRAINED", false);
        map.put("MODEL_FAILED", true);
        map.put("DATASET_UPLOADED", false);
        map.put("SECURITY_ALERT", true);
        map.put("WEEKLY_SUMMARY", false);
        map.put("EXPORT_READY", false);
        return map;
    }

    /**
     * Check if email notifications are enabled for a specific event.
     */
    public boolean isEmailEnabledFor(String eventType) {
        Map<String, Boolean> notifications = getEmailNotifications();
        return notifications.getOrDefault(eventType, false);
    }

    /**
     * Check if in-app notifications are enabled for a specific event.
     */
    public boolean isInAppEnabledFor(String eventType) {
        Map<String, Boolean> notifications = getInAppNotifications();
        return notifications.getOrDefault(eventType, true);
    }
    
    /**
     * Get email notifications map, initializing if null.
     */
    public Map<String, Boolean> getEmailNotifications() {
        if (emailNotifications == null) {
            emailNotifications = createDefaultEmailNotifications();
        }
        return emailNotifications;
    }
    
    /**
     * Get in-app notifications map, initializing if null.
     */
    public Map<String, Boolean> getInAppNotifications() {
        if (inAppNotifications == null) {
            inAppNotifications = createDefaultInAppNotifications();
        }
        return inAppNotifications;
    }
    
    /**
     * Get push notifications map, initializing if null.
     */
    public Map<String, Boolean> getPushNotifications() {
        if (pushNotifications == null) {
            pushNotifications = createDefaultPushNotifications();
        }
        return pushNotifications;
    }
    
    /**
     * Initialize null JSONB fields after loading from database.
     */
    @PostLoad
    private void initializeNullFields() {
        if (emailNotifications == null) {
            emailNotifications = createDefaultEmailNotifications();
        }
        if (inAppNotifications == null) {
            inAppNotifications = createDefaultInAppNotifications();
        }
        if (pushNotifications == null) {
            pushNotifications = createDefaultPushNotifications();
        }
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
