package com.xaiforge.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Schema(description = "Model training request")
public record TrainRequest(
    @Schema(description = "ID of the dataset to use for training", example = "1", required = true)
    @NotNull(message = "Dataset ID is required")
    Long datasetId,
    
    @Schema(description = "Name for the trained model", example = "Customer Churn Predictor", required = true)
    @NotBlank(message = "Model name is required")
    String modelName,
    
    @Schema(
        description = "Algorithm to use for training",
        example = "LOGISTIC_REGRESSION",
        allowableValues = {
            "LOGISTIC_REGRESSION", "RANDOM_FOREST_CLASSIFICATION", "NEURAL_NETWORK", "SVM",
            "LINEAR_REGRESSION", "RANDOM_FOREST_REGRESSION"
        },
        required = true
    )
    @NotBlank(message = "Algorithm is required")
    String algorithm,
    
    @Schema(description = "Name of the target column to predict", example = "churn", required = true)
    @NotBlank(message = "Target column is required")
    String targetColumn,
    
    @Schema(description = "List of feature column names to use for training", example = "[\"age\", \"tenure\", \"monthly_charges\"]")
    java.util.List<String> featureNames,
    
    @Schema(description = "Percentage of data to use for training (50-90)", example = "80", defaultValue = "80")
    @Min(value = 50, message = "Train/test split must be at least 50%")
    @Max(value = 90, message = "Train/test split must be at most 90%")
    int trainTestSplit,
    
    @Schema(description = "Whether to perform cross-validation", example = "true", defaultValue = "false")
    boolean crossValidation,
    
    @Schema(
        description = "Algorithm-specific hyperparameters",
        example = "{\"numTrees\": 100, \"maxDepth\": 10, \"learningRate\": 0.01}",
        defaultValue = "{}"
    )
    java.util.Map<String, Object> hyperparameters
) {
    public TrainRequest {
        if (hyperparameters == null) {
            hyperparameters = new java.util.HashMap<>();
        }
    }
}

