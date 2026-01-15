package com.xaiforge.common.dto;

import java.util.List;
import java.util.Map;

/**
 * Result DTO for batch prediction
 * 
 * @since 1.0.0
 */
public record BatchPredictionResult(
    Long totalRows,
    Long successfulPredictions,
    Long failedPredictions,
    List<PredictionRow> results,
    List<ErrorRow> errors
) {
    public record PredictionRow(
        Integer rowNumber,
        Map<String, String> inputData,
        String prediction,
        Double confidence,
        Map<String, Double> featureImportance,
        ExplanationResponse explanation
    ) {}
    
    public record ErrorRow(
        Integer rowNumber,
        Map<String, String> inputData,
        String errorMessage
    ) {}
}
