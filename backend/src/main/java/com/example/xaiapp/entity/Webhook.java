package com.example.xaiapp.entity;

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
