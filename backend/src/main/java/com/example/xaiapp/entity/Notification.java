package com.example.xaiapp.entity;

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
