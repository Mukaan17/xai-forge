package com.xaiforge.common.dto;

import java.util.List;
import java.util.Map;

public record ExtendedMetricsDto(
    // Basic metrics
    Double accuracy,
    Double precision,
    Double recall,
    Double f1Score,
    
    // Regression metrics
    Double mse,
    Double rmse,
    Double mae,
    Double r2Score,
    
    // Confusion matrix (for classification)
    List<List<Integer>> confusionMatrix,
    List<String> classLabels,
    
    // ROC curve data (for classification)
    List<RocPoint> rocCurve,
    
    // Feature importance
    Map<String, Double> featureImportance,
    
    // Training history
    List<Map<String, Object>> trainingHistory
) {
    public record RocPoint(
        double falsePositiveRate,
        double truePositiveRate,
        double threshold
    ) {}
}
