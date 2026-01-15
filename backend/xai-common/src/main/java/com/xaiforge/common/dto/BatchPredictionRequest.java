package com.xaiforge.common.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for batch prediction
 * 
 * @since 1.0.0
 */
public record BatchPredictionRequest(
    @NotNull(message = "Model ID is required")
    Long modelId,
    
    @NotNull(message = "CSV file is required")
    org.springframework.web.multipart.MultipartFile file,
    
    Boolean includeExplanations,
    
    Boolean returnCsv
) {
    public BatchPredictionRequest {
        if (includeExplanations == null) {
            includeExplanations = false;
        }
        if (returnCsv == null) {
            returnCsv = false;
        }
    }
}
