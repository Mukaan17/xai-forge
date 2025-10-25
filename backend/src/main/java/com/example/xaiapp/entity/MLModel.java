/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:36
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:52
 */
package com.example.xaiapp.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "model_metadata", columnDefinition = "TEXT")
    private String modelMetadata;
    
    @PrePersist
    protected void onCreate() {
        trainingDate = LocalDateTime.now();
    }
    
    public enum ModelType {
        CLASSIFICATION,
        REGRESSION
    }
}
