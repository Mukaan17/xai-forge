package com.example.xaiapp.builder;

import com.example.xaiapp.dto.TrainRequestDto;
import com.example.xaiapp.entity.MLModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for TrainRequestDto objects
 * 
 * This builder provides a fluent interface for creating TrainRequestDto
 * objects with proper validation and default values.
 * 
 * @since 1.0.0
 */
public class TrainRequestBuilder {
    
    private String modelName;
    private String modelType;
    private Long datasetId;
    private String targetVariable;
    private List<String> featureNames;
    
    /**
     * Create a new TrainRequestBuilder instance
     * 
     * @return A new TrainRequestBuilder
     */
    public static TrainRequestBuilder builder() {
        return new TrainRequestBuilder();
    }
    
    /**
     * Set the model name
     * 
     * @param modelName The model name
     * @return This builder instance
     */
    public TrainRequestBuilder setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }
    
    /**
     * Set the model type
     * 
     * @param modelType The model type (CLASSIFICATION or REGRESSION)
     * @return This builder instance
     */
    public TrainRequestBuilder setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }
    
    /**
     * Set the model type using enum
     * 
     * @param modelType The model type enum
     * @return This builder instance
     */
    public TrainRequestBuilder setModelType(MLModel.ModelType modelType) {
        this.modelType = modelType.name();
        return this;
    }
    
    /**
     * Set the dataset ID
     * 
     * @param datasetId The dataset ID
     * @return This builder instance
     */
    public TrainRequestBuilder setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
        return this;
    }
    
    /**
     * Set the target variable
     * 
     * @param targetVariable The target variable name
     * @return This builder instance
     */
    public TrainRequestBuilder setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
        return this;
    }
    
    /**
     * Set the feature names
     * 
     * @param featureNames The list of feature names
     * @return This builder instance
     */
    public TrainRequestBuilder setFeatureNames(List<String> featureNames) {
        this.featureNames = featureNames == null ? null : new ArrayList<>(featureNames);
        return this;
    }
    
    /**
     * Add a feature name
     * 
     * @param featureName The feature name to add
     * @return This builder instance
     */
    public TrainRequestBuilder addFeatureName(String featureName) {
        if (this.featureNames == null) {
            this.featureNames = new ArrayList<>();
        }
        this.featureNames.add(featureName);
        return this;
    }
    
    /**
     * Add multiple feature names
     * 
     * @param featureNames The feature names to add
     * @return This builder instance
     */
    public TrainRequestBuilder addFeatureNames(List<String> featureNames) {
        if (this.featureNames == null) {
            this.featureNames = new ArrayList<>();
        }
        this.featureNames.addAll(featureNames);
        return this;
    }
    
    /**
     * Build the TrainRequestDto object
     * 
     * @return The built TrainRequestDto
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public TrainRequestDto build() {
        validate();
        TrainRequestDto dto = new TrainRequestDto();
        dto.setDatasetId(datasetId);
        dto.setModelName(modelName);
        dto.setTargetVariable(targetVariable);
        dto.setFeatureNames(featureNames);
        dto.setModelType(modelType);
        return dto;
    }
    
    /**
     * Validate the builder state
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private void validate() {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name is required");
        }
        
        if (modelType == null || modelType.trim().isEmpty()) {
            throw new IllegalArgumentException("Model type is required");
        }
        
        if (!modelType.equals("CLASSIFICATION") && !modelType.equals("REGRESSION")) {
            throw new IllegalArgumentException("Model type must be CLASSIFICATION or REGRESSION");
        }
        
        if (datasetId == null) {
            throw new IllegalArgumentException("Dataset ID is required");
        }
        
        if (targetVariable == null || targetVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Target variable is required");
        }
        
        if (featureNames == null || featureNames.isEmpty()) {
            throw new IllegalArgumentException("At least one feature name is required");
        }
        
        // Validate feature names
        for (String featureName : featureNames) {
            if (featureName == null || featureName.trim().isEmpty()) {
                throw new IllegalArgumentException("Feature names cannot be null or empty");
            }
        }
    }
}
