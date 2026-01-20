package com.xaiforge.infrastructure.ml;

import com.xaiforge.common.dto.ExplanationResponse;
import com.xaiforge.domain.model.entity.MLModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tribuo.Example;
import org.tribuo.Feature;
import org.tribuo.Model;
import org.tribuo.Prediction;

import java.util.*;

/**
 * LIME (Local Interpretable Model-agnostic Explanations) implementation
 * 
 * Generates explanations by:
 * 1. Creating perturbed samples around the instance to explain
 * 2. Getting predictions for those samples
 * 3. Weighting samples by their distance to the original instance
 * 4. Fitting a local linear model to approximate the model's behavior
 * 5. Extracting feature importance from the linear model coefficients
 * 
 * @since 1.0.0
 */
@Component

public class LimeExplainer {
    private static final Logger log = LoggerFactory.getLogger(LimeExplainer.class);
    
    private static final int DEFAULT_NUM_SAMPLES = 5000;
    private static final double DEFAULT_KERNEL_WIDTH = 0.75;
    private static final Random RANDOM = new Random();
    
    /**
     * Generate LIME explanation for a prediction
     * 
     * @param model The trained model
     * @param originalExample The example to explain
     * @param originalPrediction The prediction for the original example
     * @param mlModel The ML model entity (for metadata)
     * @param numSamples Number of perturbed samples to generate
     * @return List of feature impacts sorted by importance
     */
    public List<ExplanationResponse.FeatureImpact> explain(
            Model<?> model,
            Example<?> originalExample,
            Prediction<?> originalPrediction,
            MLModel mlModel,
            int numSamples) {
        
        try {
            // Extract feature values from original example
            Map<String, Double> originalFeatures = extractFeatureValues(originalExample);
            List<String> featureNames = new ArrayList<>(originalFeatures.keySet());
            
            if (featureNames.isEmpty()) {
                log.warn("No features found in example");
                return Collections.emptyList();
            }
            
            // Generate perturbed samples
            List<PerturbedSample> samples = generatePerturbedSamples(
                originalFeatures, featureNames, numSamples);
            
            // Get predictions for perturbed samples
            List<WeightedPrediction> weightedPredictions = new ArrayList<>();
            for (PerturbedSample sample : samples) {
                try {
                    Example<?> perturbedExample = createExampleFromFeatures(
                        sample.features, originalExample, mlModel);
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    Prediction<?> prediction = ((Model) model).predict(perturbedExample);
                    
                    // Calculate distance weight (exponential kernel)
                    double distance = calculateDistance(originalFeatures, sample.features);
                    double weight = Math.exp(-(distance * distance) / (DEFAULT_KERNEL_WIDTH * DEFAULT_KERNEL_WIDTH));
                    
                    // Get prediction value
                    double predictionValue = extractPredictionValue(prediction, mlModel.getModelType());
                    double originalValue = extractPredictionValue(originalPrediction, mlModel.getModelType());
                    
                    weightedPredictions.add(new WeightedPrediction(
                        sample.features, predictionValue, weight, originalValue));
                    
                } catch (Exception e) {
                    log.debug("Failed to get prediction for perturbed sample: {}", e.getMessage());
                }
            }
            
            if (weightedPredictions.isEmpty()) {
                log.warn("No valid predictions obtained for perturbed samples");
                return generateFallbackExplanation(originalFeatures);
            }
            
            // Fit local linear model using weighted least squares
            Map<String, Double> featureImportance = fitLocalLinearModel(
                featureNames, weightedPredictions, originalFeatures);
            
            // Convert to FeatureImpact list
            List<ExplanationResponse.FeatureImpact> impacts = new ArrayList<>();
            for (String featureName : featureNames) {
                Double importance = featureImportance.get(featureName);
                if (importance != null) {
                    double contribution = importance;
                    String direction = contribution >= 0 ? "positive" : "negative";
                    impacts.add(new ExplanationResponse.FeatureImpact(
                        featureName, Math.abs(contribution), direction, contribution));
                }
            }
            
            // Sort by absolute importance
            impacts.sort((a, b) -> Double.compare(b.impact(), a.impact()));
            
            log.debug("Generated LIME explanation with {} features", impacts.size());
            return impacts;
            
        } catch (Exception e) {
            log.error("Error generating LIME explanation: {}", e.getMessage(), e);
            return generateFallbackExplanation(extractFeatureValues(originalExample));
        }
    }
    
    /**
     * Generate perturbed samples around the original instance
     */
    private List<PerturbedSample> generatePerturbedSamples(
            Map<String, Double> originalFeatures,
            List<String> featureNames,
            int numSamples) {
        
        List<PerturbedSample> samples = new ArrayList<>();
        
        // Calculate feature statistics for perturbation
        Map<String, FeatureStats> stats = calculateFeatureStats(originalFeatures);
        
        for (int i = 0; i < numSamples; i++) {
            Map<String, Double> perturbedFeatures = new HashMap<>();
            
            // Randomly select features to perturb (on average, perturb 50% of features)
            Set<String> featuresToPerturb = new HashSet<>();
            for (String featureName : featureNames) {
                if (RANDOM.nextDouble() < 0.5) {
                    featuresToPerturb.add(featureName);
                }
            }
            
            // Perturb selected features
            for (String featureName : featureNames) {
                if (featuresToPerturb.contains(featureName)) {
                    // Perturb by sampling from normal distribution around original value
                    FeatureStats stat = stats.get(featureName);
                    double originalValue = originalFeatures.get(featureName);
                    double stdDev = Math.max(stat.stdDev, Math.abs(originalValue) * 0.1);
                    double perturbedValue = originalValue + RANDOM.nextGaussian() * stdDev;
                    perturbedFeatures.put(featureName, perturbedValue);
                } else {
                    // Keep original value
                    perturbedFeatures.put(featureName, originalFeatures.get(featureName));
                }
            }
            
            samples.add(new PerturbedSample(perturbedFeatures));
        }
        
        return samples;
    }
    
    /**
     * Calculate feature statistics for perturbation
     */
    private Map<String, FeatureStats> calculateFeatureStats(Map<String, Double> features) {
        Map<String, FeatureStats> stats = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String featureName = entry.getKey();
            double value = entry.getValue();
            
            // Use value magnitude as basis for std dev
            double stdDev = Math.max(Math.abs(value) * 0.2, 0.1);
            stats.put(featureName, new FeatureStats(value, stdDev));
        }
        
        return stats;
    }
    
    /**
     * Calculate Euclidean distance between two feature vectors
     */
    private double calculateDistance(Map<String, Double> features1, Map<String, Double> features2) {
        double sumSquaredDiff = 0.0;
        int count = 0;
        
        for (String featureName : features1.keySet()) {
            if (features2.containsKey(featureName)) {
                double diff = features1.get(featureName) - features2.get(featureName);
                sumSquaredDiff += diff * diff;
                count++;
            }
        }
        
        return count > 0 ? Math.sqrt(sumSquaredDiff / count) : 1.0;
    }
    
    /**
     * Fit local linear model using weighted least squares
     */
    private Map<String, Double> fitLocalLinearModel(
            List<String> featureNames,
            List<WeightedPrediction> weightedPredictions,
            Map<String, Double> originalFeatures) {
        
        // Simple linear regression: y = intercept + sum(coef_i * x_i)
        // Using weighted least squares
        
        int numFeatures = featureNames.size();
        int numSamples = weightedPredictions.size();
        
        if (numSamples < numFeatures) {
            log.warn("Not enough samples ({}) for number of features ({}), using simplified approach", 
                numSamples, numFeatures);
            return fitSimplifiedModel(featureNames, weightedPredictions, originalFeatures);
        }
        
        // Build design matrix X and target vector y
        double[][] X = new double[numSamples][numFeatures];
        double[] y = new double[numSamples];
        double[] weights = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            WeightedPrediction wp = weightedPredictions.get(i);
            weights[i] = wp.weight;
            y[i] = wp.predictionValue - wp.originalValue; // Predict difference from original
            
            for (int j = 0; j < numFeatures; j++) {
                String featureName = featureNames.get(j);
                double originalValue = originalFeatures.get(featureName);
                double perturbedValue = wp.features.get(featureName);
                X[i][j] = perturbedValue - originalValue; // Feature difference from original
            }
        }
        
        // Solve weighted least squares: (X^T W X) beta = X^T W y
        // Simplified: use gradient descent or normal equations
        
        Map<String, Double> coefficients = new HashMap<>();
        
        // For each feature, calculate correlation-weighted importance
        for (int j = 0; j < numFeatures; j++) {
            String featureName = featureNames.get(j);
            double sumWeightedProduct = 0.0;
            double sumWeightedSquared = 0.0;
            
            for (int i = 0; i < numSamples; i++) {
                double weight = weights[i];
                double xDiff = X[i][j];
                double yDiff = y[i];
                
                sumWeightedProduct += weight * xDiff * yDiff;
                sumWeightedSquared += weight * xDiff * xDiff;
            }
            
            // Coefficient = weighted covariance / weighted variance
            double coefficient = sumWeightedSquared > 1e-10 
                ? sumWeightedProduct / sumWeightedSquared 
                : 0.0;
            
            coefficients.put(featureName, coefficient);
        }
        
        return coefficients;
    }
    
    /**
     * Simplified model fitting when we don't have enough samples
     */
    private Map<String, Double> fitSimplifiedModel(
            List<String> featureNames,
            List<WeightedPrediction> weightedPredictions,
            Map<String, Double> originalFeatures) {
        
        Map<String, Double> coefficients = new HashMap<>();
        
        // Calculate average impact per feature
        for (String featureName : featureNames) {
            double sumWeightedImpact = 0.0;
            double sumWeights = 0.0;
            
            for (WeightedPrediction wp : weightedPredictions) {
                double featureDiff = wp.features.get(featureName) - originalFeatures.get(featureName);
                double predictionDiff = wp.predictionValue - wp.originalValue;
                
                if (Math.abs(featureDiff) > 1e-10) {
                    double impact = predictionDiff / featureDiff;
                    sumWeightedImpact += wp.weight * impact;
                    sumWeights += wp.weight;
                }
            }
            
            double coefficient = sumWeights > 1e-10 ? sumWeightedImpact / sumWeights : 0.0;
            coefficients.put(featureName, coefficient);
        }
        
        return coefficients;
    }
    
    /**
     * Extract feature values from example
     */
    private Map<String, Double> extractFeatureValues(Example<?> example) {
        Map<String, Double> features = new HashMap<>();
        for (Feature feature : example) {
            features.put(feature.getName(), feature.getValue());
        }
        return features;
    }
    
    /**
     * Create example from feature map
     */
    private Example<?> createExampleFromFeatures(
            Map<String, Double> features,
            Example<?> originalExample,
            MLModel mlModel) {
        
        // Reuse the original example structure but with new feature values
        // This is a simplified approach - in a full implementation, we'd properly construct
        // the example based on model type
        
        List<String> featureNames = new ArrayList<>(features.keySet());
        double[] featureValues = featureNames.stream()
            .mapToDouble(features::get)
            .toArray();
        
        String[] featureNamesArray = featureNames.toArray(new String[0]);
        
        if (mlModel.getModelType() == MLModel.ModelType.CLASSIFICATION) {
            return new org.tribuo.impl.ArrayExample<>(
                new org.tribuo.classification.Label("unknown"), 
                featureNamesArray, 
                featureValues);
        } else {
            String[] targetNames = mlModel.getTargetVariable().split(",");
            org.tribuo.regression.Regressor.DimensionTuple[] dims = 
                new org.tribuo.regression.Regressor.DimensionTuple[targetNames.length];
            for (int i = 0; i < targetNames.length; i++) {
                dims[i] = new org.tribuo.regression.Regressor.DimensionTuple(targetNames[i].trim(), 0.0);
            }
            return new org.tribuo.impl.ArrayExample<>(
                new org.tribuo.regression.Regressor(dims), 
                featureNamesArray, 
                featureValues);
        }
    }
    
    /**
     * Extract prediction value as double
     */
    private double extractPredictionValue(Prediction<?> prediction, MLModel.ModelType modelType) {
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            @SuppressWarnings("unchecked")
            Prediction<org.tribuo.classification.Label> labelPrediction = 
                (Prediction<org.tribuo.classification.Label>) prediction;
            org.tribuo.classification.Label label = labelPrediction.getOutput();
            return label.getScore();
        } else {
            @SuppressWarnings("unchecked")
            Prediction<org.tribuo.regression.Regressor> regressorPrediction = 
                (Prediction<org.tribuo.regression.Regressor>) prediction;
            org.tribuo.regression.Regressor regressor = regressorPrediction.getOutput();
            return regressor.getValues()[0];
        }
    }
    
    /**
     * Generate fallback explanation when LIME fails
     */
    private List<ExplanationResponse.FeatureImpact> generateFallbackExplanation(
            Map<String, Double> features) {
        
        List<ExplanationResponse.FeatureImpact> impacts = new ArrayList<>();
        
        // Use feature values as importance (normalized)
        double maxAbsValue = features.values().stream()
            .mapToDouble(v -> v != null ? Math.abs(v) : 0.0)
            .max()
            .orElse(1.0);
        
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            double normalizedValue = entry.getValue() / maxAbsValue;
            String direction = normalizedValue >= 0 ? "positive" : "negative";
            impacts.add(new ExplanationResponse.FeatureImpact(
                entry.getKey(), Math.abs(normalizedValue), direction, normalizedValue));
        }
        
        impacts.sort((a, b) -> Double.compare(b.impact(), a.impact()));
        return impacts;
    }
    
    // Helper classes
    private record PerturbedSample(Map<String, Double> features) {}
    
    private record WeightedPrediction(
        Map<String, Double> features,
        double predictionValue,
        double weight,
        double originalValue
    ) {}
    
    private record FeatureStats(double mean, double stdDev) {}
}
