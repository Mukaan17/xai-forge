package com.xaiforge.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record TrainRequest(
    @NotNull(message = "Dataset ID is required")
    Long datasetId,
    
    @NotBlank(message = "Model name is required")
    String modelName,
    
    @NotBlank(message = "Algorithm is required")
    String algorithm,
    
    @NotBlank(message = "Target column is required")
    String targetColumn,
    
    java.util.List<String> featureNames,
    
    @Min(value = 50, message = "Train/test split must be at least 50%")
    @Max(value = 90, message = "Train/test split must be at most 90%")
    int trainTestSplit,
    
    boolean crossValidation
) {}

