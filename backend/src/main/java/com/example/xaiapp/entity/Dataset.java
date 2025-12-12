package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * Owner ID (legacy column for backward compatibility).
     * This should match user_id.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
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
     * Original filename as uploaded (legacy column name for backward compatibility).
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    /**
     * Original filename as uploaded (new column name).
     */
    @Column(name = "original_filename", length = 255)
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
    @Builder.Default
    private List<MLModel> models = new ArrayList<>();
    
    // Backward compatibility: map owner to user
    public User getOwner() {
        return user;
    }
    
    public void setOwner(User owner) {
        this.user = owner;
        // Also set ownerId to match user_id for database compatibility
        if (owner != null && owner.getId() != null) {
            this.ownerId = owner.getId();
        }
    }
    
    // Backward compatibility: fileName maps to both file_name and original_filename
    public String getFileName() {
        return fileName != null ? fileName : originalFilename;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (this.originalFilename == null) {
            this.originalFilename = fileName;
        }
        if (this.name == null) {
            this.name = fileName;
        }
    }
    
    // Backward compatibility: headers -> columnNames
    @Transient
    public List<String> getHeaders() {
        return columnNames;
    }
    
    @Transient
    public void setHeaders(List<String> headers) {
        this.columnNames = headers;
    }
    
    // Backward compatibility: uploadDate -> createdAt
    @Transient
    public LocalDateTime getUploadDate() {
        return createdAt;
    }
    
    @Transient
    public void setUploadDate(LocalDateTime uploadDate) {
        this.createdAt = uploadDate;
    }
    
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
