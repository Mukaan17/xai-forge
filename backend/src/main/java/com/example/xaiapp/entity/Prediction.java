package com.example.xaiapp.entity;

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
