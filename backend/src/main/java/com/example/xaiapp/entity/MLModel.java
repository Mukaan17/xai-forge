package com.example.xaiapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @JsonIgnore
    private User user;
    
    /**
     * Dataset used to train this model.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    @JsonIgnore
    private Dataset dataset;
    
    /**
     * Display name for the model.
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * Model name (legacy column for backward compatibility).
     */
    @Column(name = "model_name", nullable = false, length = 255)
    private String modelName;
    
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
     * Target variable (legacy column for backward compatibility).
     */
    @Column(name = "target_variable", nullable = false, length = 255)
    private String targetVariable;
    
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
     * Serialized model path (legacy column for backward compatibility).
     */
    @Column(name = "serialized_model_path", nullable = false, length = 255)
    private String serializedModelPath;
    
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
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Prediction> predictions = new ArrayList<>();
    
    // ==================== BACKWARD COMPATIBILITY ====================
    
    // Backward compatibility: modelName -> name and model_name
    public String getModelName() {
        return modelName != null ? modelName : name;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
        this.name = modelName;
        if (this.baseName == null) {
            this.baseName = modelName;
        }
    }
    
    // Backward compatibility: serializedModelPath -> modelPath and serialized_model_path
    public String getSerializedModelPath() {
        return serializedModelPath != null ? serializedModelPath : modelPath;
    }
    
    public void setSerializedModelPath(String serializedModelPath) {
        this.serializedModelPath = serializedModelPath;
        this.modelPath = serializedModelPath;
    }
    
    // Backward compatibility: targetVariable -> targetColumn and target_variable
    public String getTargetVariable() {
        return targetVariable != null ? targetVariable : targetColumn;
    }
    
    public void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
        this.targetColumn = targetVariable;
    }
    
    // Backward compatibility: featureNames -> featureColumns
    @Transient
    public List<String> getFeatureNames() {
        return featureColumns;
    }
    
    @Transient
    public void setFeatureNames(List<String> featureNames) {
        this.featureColumns = featureNames;
    }
    
    // Backward compatibility: trainingDate -> createdAt
    @Transient
    public LocalDateTime getTrainingDate() {
        return createdAt;
    }
    
    @Transient
    public void setTrainingDate(LocalDateTime trainingDate) {
        this.createdAt = trainingDate;
    }
    
    // Backward compatibility: modelMetadata -> description
    @Transient
    public String getModelMetadata() {
        return description;
    }
    
    @Transient
    public void setModelMetadata(String modelMetadata) {
        this.description = modelMetadata;
    }
    
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
    
    // Getter for dataset ID (for JSON serialization without circular references)
    @com.fasterxml.jackson.annotation.JsonProperty("datasetId")
    public Long getDatasetId() {
        return dataset != null ? dataset.getId() : null;
    }
}
