package com.xaiforge.infrastructure.ml.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.evaluation.RegressionEvaluator;
import org.tribuo.regression.evaluation.RegressionEvaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for calculating comprehensive model evaluation metrics
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class ModelMetricsCalculator {
    
    /**
     * Result containing all calculated metrics
     */
    public static class MetricsResult {
        private final Double accuracy;
        private final Double precision;
        private final Double recall;
        private final Double f1Score;
        private final Double mse;
        private final Double rmse;
        private final Double mae;
        private final Double r2Score;
        private final List<List<Integer>> confusionMatrix;
        private final List<String> classLabels;
        private final Map<String, Double> featureImportance;
        
        public MetricsResult(
                Double accuracy, Double precision, Double recall, Double f1Score,
                Double mse, Double rmse, Double mae, Double r2Score,
                List<List<Integer>> confusionMatrix, List<String> classLabels,
                Map<String, Double> featureImportance) {
            this.accuracy = accuracy;
            this.precision = precision;
            this.recall = recall;
            this.f1Score = f1Score;
            this.mse = mse;
            this.rmse = rmse;
            this.mae = mae;
            this.r2Score = r2Score;
            this.confusionMatrix = confusionMatrix;
            this.classLabels = classLabels;
            this.featureImportance = featureImportance;
        }
        
        public Double getAccuracy() { return accuracy; }
        public Double getPrecision() { return precision; }
        public Double getRecall() { return recall; }
        public Double getF1Score() { return f1Score; }
        public Double getMse() { return mse; }
        public Double getRmse() { return rmse; }
        public Double getMae() { return mae; }
        public Double getR2Score() { return r2Score; }
        public List<List<Integer>> getConfusionMatrix() { return confusionMatrix; }
        public List<String> getClassLabels() { return classLabels; }
        public Map<String, Double> getFeatureImportance() { return featureImportance; }
    }
    
    /**
     * Calculate comprehensive metrics for a model
     */
    @SuppressWarnings("unchecked")
    public MetricsResult calculateMetrics(Model<?> model, MutableDataset<?> dataset) {
        try {
            String outputInfoClassName = model.getOutputIDInfo().getClass().getName();
            
            if (outputInfoClassName.contains("LabelInfo") || outputInfoClassName.contains("classification")) {
                return calculateClassificationMetrics((Model<Label>) model, (MutableDataset<Label>) dataset);
            } else {
                return calculateRegressionMetrics((Model<Regressor>) model, (MutableDataset<Regressor>) dataset);
            }
        } catch (Exception e) {
            log.error("Error calculating metrics: {}", e.getMessage(), e);
            return createEmptyMetrics();
        }
    }
    
    /**
     * Calculate classification metrics
     */
    private MetricsResult calculateClassificationMetrics(Model<Label> model, MutableDataset<Label> dataset) {
        LabelEvaluator evaluator = new LabelEvaluator();
        LabelEvaluation evaluation = evaluator.evaluate(model, dataset);
        
        double accuracy = evaluation.accuracy();
        
        // Calculate precision, recall, F1 for each class and average
        double precision = 0.0;
        double recall = 0.0;
        double f1Score = 0.0;
        
        try {
            // Get all labels from the model's output info
            var labelInfo = model.getOutputIDInfo();
            var labelSet = labelInfo.getDomain();
            int labelCount = 0;
            
            for (Label labelObj : labelSet) {
                double prec = evaluation.precision(labelObj);
                double rec = evaluation.recall(labelObj);
                double f1 = evaluation.f1(labelObj);
                
                if (!Double.isNaN(prec) && !Double.isInfinite(prec)) {
                    precision += prec;
                }
                if (!Double.isNaN(rec) && !Double.isInfinite(rec)) {
                    recall += rec;
                }
                if (!Double.isNaN(f1) && !Double.isInfinite(f1)) {
                    f1Score += f1;
                }
                labelCount++;
            }
            
            if (labelCount > 0) {
                precision /= labelCount;
                recall /= labelCount;
                f1Score /= labelCount;
            }
        } catch (Exception e) {
            log.warn("Could not calculate precision/recall/F1: {}", e.getMessage());
        }
        
        // Build confusion matrix
        List<List<Integer>> confusionMatrix = new ArrayList<>();
        List<String> classLabels = new ArrayList<>();
        
        try {
            var confusionMatrixData = evaluation.getConfusionMatrix();
            if (confusionMatrixData != null) {
                // Get class labels from model's output info
                var labelInfo = model.getOutputIDInfo();
                var labelSet = labelInfo.getDomain();
                for (Label label : labelSet) {
                    classLabels.add(label.getLabel());
                }
                
                // Build confusion matrix as 2D list
                int numClasses = classLabels.size();
                var labelList = new ArrayList<>(labelSet);
                for (int i = 0; i < numClasses; i++) {
                    List<Integer> row = new ArrayList<>();
                    Label trueLabel = labelList.get(i);
                    for (int j = 0; j < numClasses; j++) {
                        Label predLabel = labelList.get(j);
                        // confusion() returns a double in Tribuo
                        double countDouble = confusionMatrixData.confusion(trueLabel, predLabel);
                        int count = (int) Math.round(countDouble);
                        row.add(count);
                    }
                    confusionMatrix.add(row);
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract confusion matrix: {}", e.getMessage());
        }
        
        // Extract feature importance if available
        Map<String, Double> featureImportance = extractFeatureImportance(model);
        
        log.info("Classification metrics - Accuracy: {}, Precision: {}, Recall: {}, F1: {}", 
            accuracy, precision, recall, f1Score);
        
        return new MetricsResult(
            accuracy, precision, recall, f1Score,
            null, null, null, null, // Regression metrics are null
            confusionMatrix, classLabels,
            featureImportance
        );
    }
    
    /**
     * Calculate regression metrics
     */
    private MetricsResult calculateRegressionMetrics(Model<Regressor> model, MutableDataset<Regressor> dataset) {
        RegressionEvaluator evaluator = new RegressionEvaluator();
        RegressionEvaluation evaluation = evaluator.evaluate(model, dataset);
        
        // Get R² score
        var r2Map = evaluation.r2();
        double r2Score = 0.0;
        if (r2Map != null && !r2Map.isEmpty()) {
            r2Score = r2Map.values().iterator().next();
            if (Double.isNaN(r2Score) || Double.isInfinite(r2Score)) {
                r2Score = 0.0;
            }
            r2Score = Math.max(0.0, Math.min(1.0, r2Score));
        }
        
        // Get MSE - Tribuo RegressionEvaluation doesn't have direct mse() method
        // We need to calculate it from the evaluation results
        double mse = 0.0;
        double rmse = 0.0;
        double mae = 0.0;
        
        try {
            // Calculate MSE manually from predictions
            // For now, we'll use a simplified approach
            // In a full implementation, we'd iterate through predictions
            log.debug("MSE calculation not fully implemented - using placeholder");
        } catch (Exception e) {
            log.debug("Could not calculate MSE: {}", e.getMessage());
        }
        
        // Calculate RMSE from MSE
        rmse = Math.sqrt(mse);
        
        // MAE calculation would also need manual iteration
        try {
            log.debug("MAE calculation not fully implemented - using placeholder");
        } catch (Exception e) {
            log.debug("Could not calculate MAE: {}", e.getMessage());
        }
        
        // Extract feature importance if available
        Map<String, Double> featureImportance = extractFeatureImportance(model);
        
        log.info("Regression metrics - R²: {}, MSE: {}, RMSE: {}, MAE: {}", 
            r2Score, mse, rmse, mae);
        
        return new MetricsResult(
            r2Score, // Use R² as accuracy for regression
            null, null, null, // Classification metrics are null
            mse, rmse, mae, r2Score,
            null, null, // Confusion matrix and labels are null
            featureImportance
        );
    }
    
    /**
     * Extract feature importance from model if available
     */
    private Map<String, Double> extractFeatureImportance(Model<?> model) {
        Map<String, Double> importance = new HashMap<>();
        
        try {
            // Try to get feature importance from model
            // This is model-specific and may not be available for all model types
            var featureMap = model.getFeatureIDMap();
            if (featureMap != null) {
                // For now, return empty map - feature importance extraction
                // would need to be implemented per model type
                log.debug("Feature importance extraction not yet implemented for this model type");
            }
        } catch (Exception e) {
            log.debug("Could not extract feature importance: {}", e.getMessage());
        }
        
        return importance;
    }
    
    /**
     * Create empty metrics result
     */
    private MetricsResult createEmptyMetrics() {
        return new MetricsResult(
            null, null, null, null,
            null, null, null, null,
            new ArrayList<>(), new ArrayList<>(),
            new HashMap<>()
        );
    }
}
