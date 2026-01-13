package com.xaiforge.domain.model.entity;

import com.xaiforge.domain.dataset.entity.Dataset;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ml_models")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String modelName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelType modelType;
    
    @Column(nullable = false)
    private String serializedModelPath;
    
    @Column(name = "training_date")
    private LocalDateTime trainingDate;
    
    @Column(nullable = false)
    private String targetVariable;
    
    @ElementCollection
    @CollectionTable(name = "model_features", joinColumns = @JoinColumn(name = "model_id"))
    @Column(name = "feature_name")
    private List<String> featureNames;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "precision")
    private Double precision;
    
    @Column(name = "recall")
    private Double recall;
    
    @Column(name = "f1_score")
    private Double f1Score;
    
    @Column(name = "training_time")
    private Long trainingTime; // in seconds
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ModelStatus status = ModelStatus.TRAINING;
    
    @Column(name = "model_metadata", columnDefinition = "TEXT")
    private String modelMetadata;
    
    @Column(name = "confusion_matrix", columnDefinition = "TEXT")
    private String confusionMatrix; // JSON string
    
    @Column(name = "roc_curve_data", columnDefinition = "TEXT")
    private String rocCurveData; // JSON string
    
    @PrePersist
    protected void onCreate() {
        trainingDate = LocalDateTime.now();
    }
    
    public enum ModelType {
        CLASSIFICATION,
        REGRESSION
    }
    
    public enum ModelStatus {
        TRAINING,
        READY,
        FAILED
    }
}

