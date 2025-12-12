package com.example.xaiapp.entity;

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
