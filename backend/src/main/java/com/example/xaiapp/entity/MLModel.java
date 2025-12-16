package com.example.xaiapp.entity;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ml_models")
@Data
@Getter
@Setter
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
    @JsonIgnore
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
    
    // Manual getters and setters (Lombok not generating them)
    public Long getId() { return id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public ModelType getModelType() { return modelType; }
    public void setModelType(ModelType modelType) { this.modelType = modelType; }
    public String getSerializedModelPath() { return serializedModelPath; }
    public void setSerializedModelPath(String serializedModelPath) { this.serializedModelPath = serializedModelPath; }
    public String getTargetVariable() { return targetVariable; }
    public void setTargetVariable(String targetVariable) { this.targetVariable = targetVariable; }
    public List<String> getFeatureNames() { return featureNames; }
    public void setFeatureNames(List<String> featureNames) { this.featureNames = featureNames; }
    public Dataset getDataset() { return dataset; }
    public void setDataset(Dataset dataset) { this.dataset = dataset; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}
