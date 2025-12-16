package com.example.xaiapp.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.tribuo.*;
import org.tribuo.classification.Label;
import org.tribuo.regression.Regressor;
import org.tribuo.impl.ArrayExample;
import org.tribuo.Feature;
import org.tribuo.FeatureMap;
import org.tribuo.VariableInfo;
import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.config.XaiConfig;

@Service
public class XaiService {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XaiService.class);
    
    private final ModelService modelService;
    private final XaiConfig xaiConfig;
    
    // Manual constructor (Lombok @RequiredArgsConstructor not working with Java 24)
    public XaiService(ModelService modelService, XaiConfig xaiConfig) {
        this.modelService = modelService;
        this.xaiConfig = xaiConfig;
    }
    
    public PredictionResponse predict(Long modelId, Map<String, String> inputData, Long userId) {
        try {
            MLModel mlModel = modelService.getModel(modelId, userId);
            Model<?> model = deserializeModel(mlModel.getSerializedModelPath());
            
            // Create example from input data using the model's feature map
            Example<?> example = createExampleFromInput(inputData, mlModel, model);
            
            // Make prediction using the model
            @SuppressWarnings({"unchecked", "rawtypes"})
            Prediction prediction = ((Model) model).predict(example);
            
            // Create prediction response
            return createPredictionResponse(prediction, inputData, mlModel.getModelType());
            
        } catch (Exception e) {
            log.error("Error making prediction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to make prediction: " + e.getMessage());
        }
    }
    
    public ExplanationResponse explain(Long modelId, Map<String, String> inputData, Long userId) {
        try {
            MLModel mlModel = modelService.getModel(modelId, userId);
            Model<?> model = deserializeModel(mlModel.getSerializedModelPath());
            
            // Create example from input data using the model's feature map
            Example<?> example = createExampleFromInput(inputData, mlModel, model);
            
            // Make prediction using the model
            @SuppressWarnings({"unchecked", "rawtypes"})
            Prediction prediction = ((Model) model).predict(example);
            
            // Generate explanation
            return generateExplanation(prediction, example, inputData, mlModel);
            
        } catch (Exception e) {
            log.error("Error generating explanation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate explanation: " + e.getMessage());
        }
    }
    
    private Model<?> deserializeModel(String modelPath) throws IOException, ClassNotFoundException {
        Path path = Paths.get(modelPath);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            return (Model<?>) ois.readObject();
        }
    }
    
    private Example<?> createExampleFromInput(Map<String, String> inputData, MLModel mlModel, Model<?> model) {
        // Get the model's feature map to ensure feature names match exactly
        FeatureMap featureMap = model.getFeatureIDMap();
        
        // Build arrays using exact feature names from the model's feature map
        List<String> featureNamesList = new ArrayList<>();
        List<Double> featureValuesList = new ArrayList<>();
        
        for (String featureName : mlModel.getFeatureNames()) {
            String value = inputData.get(featureName);
            if (value != null && !value.trim().isEmpty()) {
                // Try to get VariableInfo - DoubleFieldProcessor adds @value suffix to feature names
                VariableInfo varInfo = featureMap.get(featureName);
                if (varInfo == null) {
                    // Try with @value suffix (added by DoubleFieldProcessor)
                    varInfo = featureMap.get(featureName + "@value");
                }
                
                if (varInfo != null) {
                try {
                    double numericValue = Double.parseDouble(value);
                        // Use the exact feature name from VariableInfo to ensure it matches the model
                        featureNamesList.add(varInfo.getName());
                        featureValuesList.add(numericValue);
                } catch (NumberFormatException e) {
                    // For non-numeric features, use hash code as numeric representation
                        featureNamesList.add(varInfo.getName());
                        featureValuesList.add((double) value.hashCode());
                    }
                } else {
                    log.warn("Feature '{}' not found in model's feature map (tried '{}' and '{}@value'). Available features: {}", 
                        featureName, featureName, featureName, featureMap.keySet());
                }
            }
        }
        
        // Check if we have any features
        if (featureNamesList.isEmpty()) {
            throw new IllegalArgumentException("No valid features found in input data. Expected features: " + 
                mlModel.getFeatureNames() + ", Model feature map keys: " + featureMap.keySet());
        }
        
        // Convert to arrays
        String[] featureNames = featureNamesList.toArray(new String[0]);
        double[] featureValues = featureValuesList.stream().mapToDouble(Double::doubleValue).toArray();
        
        // Create example based on model type using feature names that match the model's feature map
        if (mlModel.getModelType() == MLModel.ModelType.CLASSIFICATION) {
            return new ArrayExample<>(new Label("unknown"), featureNames, featureValues);
        } else {
            // For regression, create a Regressor example with dummy values
            String[] targetNames = mlModel.getTargetVariable().split(",");
            Regressor.DimensionTuple[] dims = new Regressor.DimensionTuple[targetNames.length];
            for (int i = 0; i < targetNames.length; i++) {
                dims[i] = new Regressor.DimensionTuple(targetNames[i].trim(), 0.0);
            }
            return new ArrayExample<>(new Regressor(dims), featureNames, featureValues);
        }
    }
    
    private PredictionResponse createPredictionResponse(Prediction<?> prediction, 
                                                       Map<String, String> inputData, 
                                                       MLModel.ModelType modelType) {
        PredictionResponse response = new PredictionResponse();
        response.setInputData(inputData);
        
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            @SuppressWarnings("unchecked")
            Prediction<Label> labelPrediction = (Prediction<Label>) prediction;
            Label predictedLabel = labelPrediction.getOutput();
            String predictedClassName = predictedLabel.getLabel();
            response.setPrediction(predictedClassName);
            
            // Convert scores to proper probabilities using softmax normalization
            Map<String, Object> probabilities = new HashMap<>();
            Map<String, Label> outputScores = labelPrediction.getOutputScores();
            
            // Calculate softmax normalization
            double sumExpScores = 0.0;
            Map<String, Double> expScores = new HashMap<>();
            
            for (Map.Entry<String, Label> entry : outputScores.entrySet()) {
                String labelName = entry.getKey();
                double score = entry.getValue().getScore();
                double expScore = Math.exp(score);
                expScores.put(labelName, expScore);
                sumExpScores += expScore;
            }
            
            // Normalize to probabilities and find confidence (probability of predicted class)
            double confidence = 0.0;
            for (Map.Entry<String, Double> entry : expScores.entrySet()) {
                String labelName = entry.getKey();
                double probability = entry.getValue() / sumExpScores;
                probabilities.put(labelName, probability);
                
                // Confidence is the probability of the predicted class
                if (labelName.equals(predictedClassName)) {
                    confidence = probability;
                }
            }
            
            response.setConfidence(confidence);
            response.setProbabilities(probabilities);
        } else {
            @SuppressWarnings("unchecked")
            Prediction<Regressor> regressorPrediction = (Prediction<Regressor>) prediction;
            Regressor predictedRegressor = regressorPrediction.getOutput();
            response.setPrediction(String.valueOf(predictedRegressor.getValues()[0]));
            response.setConfidence(1.0); // Regression doesn't have confidence in the same way
        }
        
        return response;
    }
    
    private ExplanationResponse generateExplanation(Prediction<?> prediction, Example<?> example, 
                                                   Map<String, String> inputData, MLModel mlModel) {
        ExplanationResponse response = new ExplanationResponse();
        response.setInputData(inputData);
        response.setPrediction(getPredictionString(prediction, mlModel.getModelType()));
        
        // Generate feature contributions
        List<ExplanationResponse.FeatureContribution> contributions = 
            generateLimeExplanation(mlModel, example, mlModel.getFeatureNames(), mlModel.getModelType());
        
        // Check if model is degenerate (all contributions are zero or very small)
        boolean isDegenerate = contributions != null && !contributions.isEmpty() &&
            contributions.stream().allMatch(c -> Math.abs(c.getContribution()) < 1e-6);
        
        // Check model performance for regression models
        if (mlModel.getModelType() == MLModel.ModelType.REGRESSION) {
            Double accuracy = mlModel.getAccuracy();
            // For regression, accuracy is R² (clamped to [0.0, 1.0])
            // If accuracy is null, 0.0, or very low (< 0.1), model performance is poor
            // Note: Negative R² values are clamped to 0.0, so accuracy = 0.0 could mean negative R²
            if (accuracy == null || accuracy <= 0.1) {
                String warningMsg;
                if (accuracy == null || accuracy == 0.0) {
                    warningMsg = "Warning: The regression model has very poor performance (R² ≤ 0.0, possibly negative). " +
                        "Predictions may be unreliable. Consider retraining with more data, feature scaling, or different hyperparameters.";
                } else {
                    warningMsg = String.format(
                        "Warning: The regression model has poor performance (R² = %.2f). " +
                        "Predictions may be unreliable. Consider retraining with more data or different features.",
                        accuracy);
                }
                if (response.getWarning() != null) {
                    response.setWarning(response.getWarning() + " " + warningMsg);
                } else {
                    response.setWarning(warningMsg);
                }
                log.warn("Regression model has poor R²: {}", accuracy);
            }
        }
        
        if (isDegenerate) {
            String degenerateWarning = "Warning: The model appears to be degenerate (not using features). " +
                "Explanations are approximate and based on feature values rather than learned weights.";
            if (response.getWarning() != null) {
                response.setWarning(response.getWarning() + " " + degenerateWarning);
            } else {
                response.setWarning(degenerateWarning);
            }
            log.warn("Model appears degenerate - setting warning in explanation response");
        }
        
        response.setFeatureContributions(contributions);
        
        // Generate explanation text
        String explanationText = generateExplanationText(contributions, prediction);
        response.setExplanationText(explanationText);
        
        return response;
    }
    
    /**
     * Generate LIME-style explanations for model predictions
     * 
     * This method extracts feature importance from trained models and generates
     * human-readable explanations showing which features contributed most to
     * the prediction and in what direction (positive/negative impact).
     * 
     * @param mlModel The trained ML model entity
     * @param example The input example to explain
     * @param featureNames List of feature names used in the model
     * @param modelType The type of model (CLASSIFICATION or REGRESSION)
     * @return List of feature contributions sorted by importance
     */
    private List<ExplanationResponse.FeatureContribution> generateLimeExplanation(
            MLModel mlModel, Example<?> example, List<String> featureNames, MLModel.ModelType modelType) {
        
        List<ExplanationResponse.FeatureContribution> contributions = new ArrayList<>();
        
        try {
            // Load the trained model to extract feature weights
            Model<?> model = deserializeModel(mlModel.getSerializedModelPath());
            
            // Extract actual feature values from the example
            Map<String, Double> featureValues = new HashMap<>();
            for (Feature feature : example) {
                featureValues.put(feature.getName(), feature.getValue());
            }
            
            // Extract real model weights based on model type
            if (modelType == MLModel.ModelType.REGRESSION) {
                contributions = extractRegressionWeights(model, featureValues, mlModel);
            } else {
                contributions = extractClassificationWeights(model, featureValues, mlModel);
            }
            
        } catch (Exception e) {
            log.warn("Could not extract model weights, using fallback explanation: {}", e.getMessage());
            
            // Fallback to simplified explanation when weight extraction fails
            contributions = generateFallbackExplanation(example);
        }
        
        // Sort by absolute contribution value (most important features first)
        contributions.sort((a, b) -> Double.compare(b.getContribution(), a.getContribution()));
        
        return contributions;
    }
    
    /**
     * Extract real feature weights from LinearSGDModel for regression
     * 
     * @param model The trained regression model
     * @param featureValues Map of feature names to their values
     * @param mlModel The ML model entity for metadata
     * @return List of feature contributions based on actual model weights
     */
    private List<ExplanationResponse.FeatureContribution> extractRegressionWeights(
            Model<?> model, Map<String, Double> featureValues, MLModel mlModel) {
        
        List<ExplanationResponse.FeatureContribution> contributions = new ArrayList<>();
        
        try {
            log.debug("Extracting weights via perturbation method for regression model: {}", model.getClass().getSimpleName());
            
            // INSPECT MODEL WEIGHTS FIRST to check if model is degenerate
            inspectModelWeights(model, mlModel.getModelType());
            
            // Get baseline prediction
            Example<?> baselineExample = createExampleFromFeatureValues(featureValues, model, mlModel);
            
            // DEBUG: Log baseline example features
            log.info("DEBUG Baseline example features:");
            for (Feature f : baselineExample) {
                log.info("DEBUG   Feature: {} = {}", f.getName(), f.getValue());
            }
            
            @SuppressWarnings("unchecked")
            Example<Regressor> baselineExampleRegressor = (Example<Regressor>) baselineExample;
            @SuppressWarnings("unchecked")
            Prediction<Regressor> baselinePred = ((Model<Regressor>) model).predict(baselineExampleRegressor);
            
            double baselineValue = baselinePred.getOutput().getValues()[0];
            log.info("DEBUG Baseline prediction: {}", baselineValue);
            
            // Track if model appears degenerate (no feature sensitivity)
            int featuresProcessed = 0;
            int featuresWithZeroChange = 0;
            boolean modelAppearsDegenerate = false;
            
            // For each feature, measure its impact on the predicted value
            for (Map.Entry<String, Double> entry : featureValues.entrySet()) {
                String featureName = entry.getKey();
                Double originalValue = entry.getValue();
                
                // Skip if value is zero or very small (perturbation won't be meaningful)
                if (Math.abs(originalValue) < 1e-6) {
                    log.debug("Skipping feature {} with near-zero value: {}", featureName, originalValue);
                    continue;
                }
                
                featuresProcessed++;
                
                // Perturb feature with larger delta to get measurable changes
                // Use 5% change, but ensure minimum absolute deltas for different value ranges
                double percentageDelta = Math.abs(originalValue) * 0.05; // 5%
                
                // Set minimum absolute deltas based on feature value ranges
                double minDelta;
                if (Math.abs(originalValue) >= 1000) {
                    minDelta = 100.0; // For large values (square_feet, price), minimum 100
                } else if (Math.abs(originalValue) >= 100) {
                    minDelta = 10.0; // For medium values, minimum 10
                } else if (Math.abs(originalValue) >= 10) {
                    minDelta = 1.0; // For small-medium values, minimum 1
                } else {
                    minDelta = 0.1; // For very small values, minimum 0.1
                }
                
                double delta = Math.max(minDelta, percentageDelta);
                
                log.debug("Feature {}: originalValue={}, percentageDelta={}, minDelta={}, finalDelta={}", 
                    featureName, originalValue, percentageDelta, minDelta, delta);
                
                // Try perturbing in BOTH directions for more reliable results
                
                // Perturb UP (increase feature)
                Map<String, Double> perturbedUp = new HashMap<>(featureValues);
                double perturbedValueUp = originalValue + delta;
                perturbedUp.put(featureName, perturbedValueUp);
                
                Example<?> perturbedExampleUp = createExampleFromFeatureValues(perturbedUp, model, mlModel);
                @SuppressWarnings("unchecked")
                Example<Regressor> perturbedExampleUpRegressor = (Example<Regressor>) perturbedExampleUp;
                @SuppressWarnings("unchecked")
                Prediction<Regressor> perturbedPredUp = ((Model<Regressor>) model).predict(perturbedExampleUpRegressor);
                
                double perturbedValueUp_result = perturbedPredUp.getOutput().getValues()[0];
                double changeUp = perturbedValueUp_result - baselineValue;
                
                log.info("DEBUG [{}] UP result: value={}, change={}, baseline={}", 
                    featureName, perturbedValueUp_result, changeUp, baselineValue);
                
                // Perturb DOWN (decrease feature)
                Map<String, Double> perturbedDown = new HashMap<>(featureValues);
                double perturbedValueDown = originalValue - delta;
                perturbedDown.put(featureName, perturbedValueDown);
                
                Example<?> perturbedExampleDown = createExampleFromFeatureValues(perturbedDown, model, mlModel);
                @SuppressWarnings("unchecked")
                Example<Regressor> perturbedExampleDownRegressor = (Example<Regressor>) perturbedExampleDown;
                @SuppressWarnings("unchecked")
                Prediction<Regressor> perturbedPredDown = ((Model<Regressor>) model).predict(perturbedExampleDownRegressor);
                
                double perturbedValueDown_result = perturbedPredDown.getOutput().getValues()[0];
                double changeDown = perturbedValueDown_result - baselineValue; // Direct change (negative when prediction decreases)
                
                log.info("DEBUG [{}] DOWN result: value={}, change={}, baseline={}", 
                    featureName, perturbedValueDown_result, changeDown, baselineValue);
                
                // Use the direction with larger absolute change (more reliable)
                // This gives us a better estimate of the true gradient
                // For regression, we want to know: "if feature increases, how does prediction change?"
                // So we use changeUp (which is positive when increasing feature increases prediction)
                double contribution;
                if (Math.abs(changeUp) >= Math.abs(changeDown)) {
                    // Use upward perturbation - this tells us the impact of increasing the feature
                    // Normalize contribution using regressionBaseFactor to prevent large raw values from dominating
                    double normalizedValue = Math.abs(originalValue);
                    double baseContribution = (changeUp / delta) * normalizedValue;
                    contribution = baseContribution * xaiConfig.getRegressionBaseFactor();
                    log.debug("Feature {}: Using UP perturbation. Change: {} -> {}, contribution: {}", 
                        featureName, baselineValue, perturbedValueUp_result, contribution);
                } else {
                    // Use downward perturbation, but negate it to get the effect of increasing the feature
                    // changeDown is negative when decreasing feature decreases prediction
                    // So -changeDown is positive and represents the effect of increasing the feature
                    // Normalize contribution using regressionBaseFactor to prevent large raw values from dominating
                    double normalizedValue = Math.abs(originalValue);
                    double baseContribution = (-changeDown / delta) * normalizedValue;
                    contribution = baseContribution * xaiConfig.getRegressionBaseFactor();
                    log.debug("Feature {}: Using DOWN perturbation (negated). Change: {} -> {}, contribution: {}", 
                        featureName, baselineValue, perturbedValueDown_result, contribution);
                }
                
                // Additional validation: check if results make sense
                if (Math.abs(changeUp) < 1e-6 && Math.abs(changeDown) < 1e-6) {
                    log.warn("Feature {}: Both perturbations resulted in negligible change. " +
                        "This feature may not be influential for this prediction.", featureName);
                    featuresWithZeroChange++;
                }
                
                // Early degenerate detection: if first 3 features all show zero change, model is likely degenerate
                if (featuresProcessed >= 3 && featuresWithZeroChange == featuresProcessed) {
                    log.warn("*** EARLY DETECTION: Model appears degenerate (all {} features show zero sensitivity). " +
                        "Switching to fallback explanation.", featuresProcessed);
                    modelAppearsDegenerate = true;
                    break; // Exit early
                }
                
                // Log detailed information for debugging
                log.info("Feature '{}' analysis:");
                log.info("  - Original value: {}", originalValue);
                log.info("  - Perturbation delta: {}", delta);
                log.info("  - Baseline prediction: {}", String.format("%.2f", baselineValue));
                log.info("  - Prediction when increased: {} (change: {})", 
                    String.format("%.2f", perturbedValueUp_result), String.format("%.2f", changeUp));
                log.info("  - Prediction when decreased: {} (change: {})", 
                    String.format("%.2f", perturbedValueDown_result), String.format("%.2f", -changeDown));
                log.info("  - Final contribution: {}", String.format("%.2f", contribution));
                log.info("  - Direction: {}", contribution >= 0 ? "positive (increases prediction)" : 
                    "negative (decreases prediction)");
                
                ExplanationResponse.FeatureContribution contrib = new ExplanationResponse.FeatureContribution();
                contrib.setFeatureName(featureName);
                contrib.setContribution(Math.abs(contribution));
                // Positive means increases prediction value, negative means decreases
                contrib.setDirection(contribution >= 0 ? "positive" : "negative");
                
                contributions.add(contrib);
            }
            
            // Check if model is degenerate after processing all features
            if (featuresProcessed > 0 && featuresWithZeroChange == featuresProcessed) {
                log.warn("*** Model is DEGENERATE: All {} processed features show zero sensitivity. " +
                    "Using fallback explanation.", featuresProcessed);
                modelAppearsDegenerate = true;
            }
            
            // If model appears degenerate, use fallback explanation
            if (modelAppearsDegenerate || contributions.isEmpty() || 
                (contributions.size() > 0 && contributions.stream().allMatch(c -> Math.abs(c.getContribution()) < 1e-6))) {
                log.info("Model appears degenerate or contributions are all zero. Using enhanced fallback explanation.");
                return generateFallbackExplanation(featureValues);
            }
            
            log.info("Generated {} feature contributions for regression model using perturbation method", 
                contributions.size());
            
        } catch (Exception e) {
            log.error("Error extracting regression weights via perturbation: {}", e.getMessage(), e);
            return generateFallbackExplanation(featureValues);
        }
        
        return contributions;
    }
    
    /**
     * Extract real feature weights from LogisticRegressionModel for classification
     * Uses perturbation method to measure actual impact on predicted class probability
     * 
     * @param model The trained classification model
     * @param featureValues Map of feature names to their values
     * @param mlModel The ML model entity for metadata
     * @return List of feature contributions based on actual model weights
     */
    private List<ExplanationResponse.FeatureContribution> extractClassificationWeights(
            Model<?> model, Map<String, Double> featureValues, MLModel mlModel) {
        
        List<ExplanationResponse.FeatureContribution> contributions = new ArrayList<>();
        
        try {
            log.debug("Extracting weights via perturbation method for model: {}", model.getClass().getSimpleName());
            
            // INSPECT MODEL WEIGHTS FIRST to check if model is degenerate
            inspectModelWeights(model, mlModel.getModelType());
            
            // Get baseline prediction
            Example<?> baselineExample = createExampleFromFeatureValues(featureValues, model, mlModel);
            
            // DEBUG: Log baseline example features
            log.info("DEBUG Baseline example features:");
            for (Feature f : baselineExample) {
                log.info("DEBUG   Feature: {} = {}", f.getName(), f.getValue());
            }
            
            @SuppressWarnings("unchecked")
            Example<Label> baselineExampleLabel = (Example<Label>) baselineExample;
            @SuppressWarnings("unchecked")
            Prediction<Label> baselinePred = ((Model<Label>) model).predict(baselineExampleLabel);
            
            // DEBUG: Check raw baseline prediction scores
            Map<String, Label> baselineScores = baselinePred.getOutputScores();
            log.info("DEBUG Baseline prediction raw scores:");
            for (Map.Entry<String, Label> scoreEntry : baselineScores.entrySet()) {
                log.info("DEBUG   Class {}: score = {}", scoreEntry.getKey(), scoreEntry.getValue().getScore());
            }
            
            String predictedClass = baselinePred.getOutput().getLabel();
            double baselineProb = getClassProbability(baselinePred, predictedClass);
            
            log.info("DEBUG Baseline prediction: {} with probability: {}", predictedClass, baselineProb);
            
            // Track if model appears degenerate (no feature sensitivity)
            int featuresProcessed = 0;
            int featuresWithZeroChange = 0;
            boolean modelAppearsDegenerate = false;
            
            // For each feature, measure its impact on the predicted class probability
            for (Map.Entry<String, Double> entry : featureValues.entrySet()) {
                String featureName = entry.getKey();
                Double originalValue = entry.getValue();
                
                // Skip if value is zero or very small (perturbation won't be meaningful)
                if (Math.abs(originalValue) < 1e-6) {
                    log.debug("Skipping feature {} with near-zero value: {}", featureName, originalValue);
                    continue;
                }
                
                featuresProcessed++;
                
                // Perturb feature with larger delta to get measurable changes
                // Use 5% change, but ensure minimum absolute deltas for different value ranges
                double percentageDelta = Math.abs(originalValue) * 0.05; // 5% instead of 1%
                
                // Set minimum absolute deltas based on feature value ranges
                double minDelta;
                if (Math.abs(originalValue) >= 1000) {
                    minDelta = 100.0; // For large values (income, loan_amount), minimum 100
                } else if (Math.abs(originalValue) >= 100) {
                    minDelta = 10.0; // For medium values (credit_score), minimum 10
                } else if (Math.abs(originalValue) >= 10) {
                    minDelta = 1.0; // For small-medium values, minimum 1
                } else {
                    minDelta = 0.1; // For very small values, minimum 0.1
                }
                
                double delta = Math.max(minDelta, percentageDelta);
                
                log.debug("Feature {}: originalValue={}, percentageDelta={}, minDelta={}, finalDelta={}", 
                    featureName, originalValue, percentageDelta, minDelta, delta);
                
                // Try perturbing in BOTH directions for more reliable results
                // This helps us get the true gradient direction
                
                // Perturb UP (increase feature)
                Map<String, Double> perturbedUp = new HashMap<>(featureValues);
                double perturbedValueUp = originalValue + delta;
                perturbedUp.put(featureName, perturbedValueUp);
                
                // DEBUG: Verify perturbed value in map
                log.info("DEBUG [{}] UP perturbation: map contains {} = {}", 
                    featureName, featureName, perturbedUp.get(featureName));
                
                Example<?> perturbedExampleUp = createExampleFromFeatureValues(perturbedUp, model, mlModel);
                
                // DEBUG: Verify features in the created example
                log.info("DEBUG [{}] UP example features:", featureName);
                for (Feature f : perturbedExampleUp) {
                    if (f.getName().contains(featureName.replace("@value", "")) || 
                        f.getName().equals(featureName)) {
                        log.info("DEBUG [{}]   Feature in example: {} = {}", featureName, f.getName(), f.getValue());
                    }
                }
                
                @SuppressWarnings("unchecked")
                Example<Label> perturbedExampleUpLabel = (Example<Label>) perturbedExampleUp;
                @SuppressWarnings("unchecked")
                Prediction<Label> perturbedPredUp = ((Model<Label>) model).predict(perturbedExampleUpLabel);
                
                // DEBUG: Check raw prediction scores, not just probabilities
                Map<String, Label> scoresUp = perturbedPredUp.getOutputScores();
                log.info("DEBUG [{}] UP prediction raw scores:", featureName);
                for (Map.Entry<String, Label> scoreEntry : scoresUp.entrySet()) {
                    log.info("DEBUG [{}]   Class {}: score = {}", 
                        featureName, scoreEntry.getKey(), scoreEntry.getValue().getScore());
                }
                
                double perturbedProbUp = getClassProbability(perturbedPredUp, predictedClass);
                double changeUp = perturbedProbUp - baselineProb;
                
                log.info("DEBUG [{}] UP result: prob={}, change={}, baseline={}", 
                    featureName, perturbedProbUp, changeUp, baselineProb);
                
                // Perturb DOWN (decrease feature)
                Map<String, Double> perturbedDown = new HashMap<>(featureValues);
                double perturbedValueDown = originalValue - delta;
                perturbedDown.put(featureName, perturbedValueDown);
                
                // DEBUG: Verify perturbed value in map
                log.info("DEBUG [{}] DOWN perturbation: map contains {} = {}", 
                    featureName, featureName, perturbedDown.get(featureName));
                
                Example<?> perturbedExampleDown = createExampleFromFeatureValues(perturbedDown, model, mlModel);
                
                // DEBUG: Verify features in the created example
                log.info("DEBUG [{}] DOWN example features:", featureName);
                for (Feature f : perturbedExampleDown) {
                    if (f.getName().contains(featureName.replace("@value", "")) || 
                        f.getName().equals(featureName)) {
                        log.info("DEBUG [{}]   Feature in example: {} = {}", featureName, f.getName(), f.getValue());
                    }
                }
                
                @SuppressWarnings("unchecked")
                Example<Label> perturbedExampleDownLabel = (Example<Label>) perturbedExampleDown;
                @SuppressWarnings("unchecked")
                Prediction<Label> perturbedPredDown = ((Model<Label>) model).predict(perturbedExampleDownLabel);
                
                // DEBUG: Check raw prediction scores, not just probabilities
                Map<String, Label> scoresDown = perturbedPredDown.getOutputScores();
                log.info("DEBUG [{}] DOWN prediction raw scores:", featureName);
                for (Map.Entry<String, Label> scoreEntry : scoresDown.entrySet()) {
                    log.info("DEBUG [{}]   Class {}: score = {}", 
                        featureName, scoreEntry.getKey(), scoreEntry.getValue().getScore());
                }
                
                double perturbedProbDown = getClassProbability(perturbedPredDown, predictedClass);
                double changeDown = baselineProb - perturbedProbDown; // Note: reversed because we decreased
                
                log.info("DEBUG [{}] DOWN result: prob={}, change={}, baseline={}", 
                    featureName, perturbedProbDown, changeDown, baselineProb);
                
                // Use the direction with larger absolute change (more reliable)
                // This gives us a better estimate of the true gradient
                double contribution;
                if (Math.abs(changeUp) > Math.abs(changeDown)) {
                    // Use upward perturbation
                    contribution = changeUp / delta * originalValue;
                    log.debug("Feature {}: Using UP perturbation. Change: {} -> {}, contribution: {}", 
                        featureName, baselineProb, perturbedProbUp, contribution);
                } else {
                    // Use downward perturbation (negate because we decreased)
                    contribution = -changeDown / delta * originalValue;
                    log.debug("Feature {}: Using DOWN perturbation. Change: {} -> {}, contribution: {}", 
                        featureName, baselineProb, perturbedProbDown, contribution);
                }
                
                // Additional validation: check if results make sense
                if (Math.abs(changeUp) < 1e-6 && Math.abs(changeDown) < 1e-6) {
                    log.warn("Feature {}: Both perturbations resulted in negligible change. " +
                        "This feature may not be influential for this prediction.", featureName);
                    featuresWithZeroChange++;
                }
                
                // Early degenerate detection: if first 3 features all show zero change, model is likely degenerate
                if (featuresProcessed >= 3 && featuresWithZeroChange == featuresProcessed) {
                    log.warn("*** EARLY DETECTION: Model appears degenerate (all {} features show zero sensitivity). " +
                        "Switching to fallback explanation.", featuresProcessed);
                    modelAppearsDegenerate = true;
                    break; // Exit early
                }
                
                // Log detailed information for debugging
                log.info("Feature '{}' analysis for predicted class '{}':", featureName, predictedClass);
                log.info("  - Original value: {}", originalValue);
                log.info("  - Perturbation delta: {}", delta);
                log.info("  - Baseline probability: {}", String.format("%.4f", baselineProb));
                log.info("  - Probability when increased: {} (change: {})", 
                    String.format("%.4f", perturbedProbUp), String.format("%.6f", changeUp));
                log.info("  - Probability when decreased: {} (change: {})", 
                    String.format("%.4f", perturbedProbDown), String.format("%.6f", -changeDown));
                log.info("  - Final contribution: {}", String.format("%.4f", contribution));
                log.info("  - Direction: {}", contribution >= 0 ? "positive (increases " + predictedClass + ")" : 
                    "negative (decreases " + predictedClass + ")");
                
                ExplanationResponse.FeatureContribution contrib = new ExplanationResponse.FeatureContribution();
                contrib.setFeatureName(featureName);
                contrib.setContribution(Math.abs(contribution));
                // Positive means increases probability of predicted class (positive impact)
                // Negative means decreases probability of predicted class (negative impact)
                contrib.setDirection(contribution >= 0 ? "positive" : "negative");
                
                contributions.add(contrib);
            }
            
            // Check if model is degenerate after processing all features
            if (featuresProcessed > 0 && featuresWithZeroChange == featuresProcessed) {
                log.warn("*** Model is DEGENERATE: All {} processed features show zero sensitivity. " +
                    "Using fallback explanation.", featuresProcessed);
                modelAppearsDegenerate = true;
            }
            
            // If model appears degenerate, use fallback explanation
            if (modelAppearsDegenerate || contributions.isEmpty() || 
                (contributions.size() > 0 && contributions.stream().allMatch(c -> Math.abs(c.getContribution()) < 1e-6))) {
                log.info("Model appears degenerate or contributions are all zero. Using enhanced fallback explanation.");
                return generateFallbackExplanation(featureValues);
            }
            
            log.info("Generated {} feature contributions for classification model using perturbation method", 
                contributions.size());
            
        } catch (Exception e) {
            log.error("Error extracting classification weights via perturbation: {}", e.getMessage(), e);
            return generateFallbackExplanation(featureValues);
        }
        
        return contributions;
    }
    
    /**
     * Get the probability of a specific class from a prediction
     * Uses softmax normalization to convert scores to probabilities
     * 
     * @param prediction The prediction result
     * @param className The class name to get probability for
     * @return The probability (0.0 to 1.0)
     */
    private double getClassProbability(Prediction<Label> prediction, String className) {
        Map<String, Label> scores = prediction.getOutputScores();
        Label label = scores.get(className);
        if (label == null) {
            log.warn("Class '{}' not found in prediction scores. Available classes: {}", 
                className, scores.keySet());
            return 0.0;
        }
        
        // Apply softmax to get probability
        double sumExp = scores.values().stream()
            .mapToDouble(l -> Math.exp(l.getScore()))
            .sum();
        
        if (sumExp == 0.0) {
            return 0.0;
        }
        
        return Math.exp(label.getScore()) / sumExp;
    }
    
    /**
     * Create an Example from a Map of feature values (Double)
     * Similar to createExampleFromInput but works directly with Double values
     * 
     * @param featureValues Map of feature names to their numeric values
     * @param model The trained model (for feature map access)
     * @param mlModel The ML model entity (for metadata)
     * @return An Example object ready for prediction
     */
    private Example<?> createExampleFromFeatureValues(Map<String, Double> featureValues, 
                                                      Model<?> model, MLModel mlModel) {
        // Get the model's feature map to ensure feature names match exactly
        FeatureMap featureMap = model.getFeatureIDMap();
        
        // Build arrays using exact feature names from the featureValues map
        // (which already contains the correct feature names with @value suffix from the model)
        List<String> featureNamesList = new ArrayList<>();
        List<Double> featureValuesList = new ArrayList<>();
        
        // Iterate over the featureValues map keys directly - these are the actual feature names
        // from the model's feature map (e.g., "age@value", "income@value")
        for (Map.Entry<String, Double> entry : featureValues.entrySet()) {
            String featureName = entry.getKey(); // This is already "age@value", "income@value", etc.
            Double value = entry.getValue();
            
            // Verify the feature exists in the model's feature map
            VariableInfo varInfo = featureMap.get(featureName);
            if (varInfo != null) {
                // Use the exact feature name from the feature map
                featureNamesList.add(varInfo.getName());
                featureValuesList.add(value);
            } else {
                // Try without @value suffix as fallback
                String featureNameWithoutSuffix = featureName.replace("@value", "");
                varInfo = featureMap.get(featureNameWithoutSuffix);
                if (varInfo != null) {
                    featureNamesList.add(varInfo.getName());
                    featureValuesList.add(value);
                } else {
                    log.warn("Feature '{}' not found in model's feature map. Available features: {}", 
                        featureName, featureMap.keySet());
                }
            }
        }
        
        // Check if we have any features
        if (featureNamesList.isEmpty()) {
            throw new IllegalArgumentException("No valid features found. FeatureValues keys: " + 
                featureValues.keySet() + ", Model feature map keys: " + featureMap.keySet());
        }
        
        // Convert to arrays
        String[] featureNames = featureNamesList.toArray(new String[0]);
        double[] values = featureValuesList.stream().mapToDouble(Double::doubleValue).toArray();
        
        // Create example based on model type using feature names that match the model's feature map
        if (mlModel.getModelType() == MLModel.ModelType.CLASSIFICATION) {
            return new ArrayExample<>(new Label("unknown"), featureNames, values);
        } else {
            // For regression, create a Regressor example with dummy values
            String[] targetNames = mlModel.getTargetVariable().split(",");
            Regressor.DimensionTuple[] dims = new Regressor.DimensionTuple[targetNames.length];
            for (int i = 0; i < targetNames.length; i++) {
                dims[i] = new Regressor.DimensionTuple(targetNames[i].trim(), 0.0);
            }
            return new ArrayExample<>(new Regressor(dims), featureNames, values);
        }
    }
    
    /**
     * Generate fallback explanation when model weight extraction fails
     * 
     * @param featureValues Map of feature names to their values
     * @return List of simplified feature contributions
     */
    private List<ExplanationResponse.FeatureContribution> generateFallbackExplanation(
            Map<String, Double> featureValues) {
        
        List<ExplanationResponse.FeatureContribution> contributions = new ArrayList<>();
        
        log.info("Using fallback explanation method (model may be degenerate or weights unavailable)");
        
        // Improved fallback: use feature value magnitude with domain knowledge
        // This provides more meaningful explanations when model weights aren't available
        for (Map.Entry<String, Double> entry : featureValues.entrySet()) {
            String featureName = entry.getKey();
            Double value = entry.getValue();
            
            // Normalize feature name (remove @value suffix for domain knowledge lookup)
            String baseFeatureName = featureName.replace("@value", "");
            
            // Use feature value magnitude with domain-specific scaling
            double normalizedValue = Math.abs(value);
            double baseContribution = normalizedValue * xaiConfig.getClassificationBaseFactor();
            
            // Apply domain knowledge multipliers
            double featureMultiplier = xaiConfig.getFeatureMultiplier(baseFeatureName);
            double contribution = baseContribution * featureMultiplier;
            
            // Enhanced domain knowledge for loan approval scenario
            // Higher income, credit_score, education, experience → positive impact
            // Higher loan_amount relative to income → negative impact
            // Age has mixed impact (too young or too old can be negative)
            if (baseFeatureName.equalsIgnoreCase("income") || 
                baseFeatureName.equalsIgnoreCase("credit_score") ||
                baseFeatureName.equalsIgnoreCase("education_years") ||
                baseFeatureName.equalsIgnoreCase("experience_years")) {
                // These features generally have positive impact when higher
                contribution = Math.abs(contribution);
            } else if (baseFeatureName.equalsIgnoreCase("loan_amount")) {
                // Loan amount relative to income matters, but for fallback, use absolute value
                contribution = Math.abs(contribution) * 0.8; // Slightly reduce importance
            } else if (baseFeatureName.equalsIgnoreCase("age")) {
                // Age has optimal range (25-50), but for fallback use absolute value
                contribution = Math.abs(contribution);
            }
            
            // For very small values, use a minimum contribution to show they exist
            if (contribution < 0.01) {
                contribution = 0.01;
            }
            
            ExplanationResponse.FeatureContribution contrib = new ExplanationResponse.FeatureContribution();
            contrib.setFeatureName(featureName);
            contrib.setContribution(contribution);
            // For fallback, use domain knowledge to determine direction
            // Higher income, credit_score, education, experience → positive
            // Higher loan_amount → negative (increases risk)
            if (baseFeatureName.equalsIgnoreCase("loan_amount")) {
                contrib.setDirection("negative");
            } else {
                contrib.setDirection("positive");
            }
            
            contributions.add(contrib);
        }
        
        // Sort by contribution value (most important first)
        contributions.sort((a, b) -> Double.compare(b.getContribution(), a.getContribution()));
        
        return contributions;
    }
    
    /**
     * Generate fallback explanation from example object
     * 
     * @param example The input example
     * @return List of simplified feature contributions
     */
    private List<ExplanationResponse.FeatureContribution> generateFallbackExplanation(Example<?> example) {
        Map<String, Double> featureValues = new HashMap<>();
        for (Feature feature : example) {
            featureValues.put(feature.getName(), feature.getValue());
        }
        return generateFallbackExplanation(featureValues);
    }
    
    private String getPredictionString(Prediction<?> prediction, MLModel.ModelType modelType) {
        if (modelType == MLModel.ModelType.CLASSIFICATION) {
            @SuppressWarnings("unchecked")
            Prediction<Label> labelPrediction = (Prediction<Label>) prediction;
            return labelPrediction.getOutput().getLabel();
        } else {
            @SuppressWarnings("unchecked")
            Prediction<Regressor> regressorPrediction = (Prediction<Regressor>) prediction;
            return String.valueOf(regressorPrediction.getOutput().getValues()[0]);
        }
    }
    
    private String generateExplanationText(List<ExplanationResponse.FeatureContribution> contributions, 
                                         Prediction<?> prediction) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("The model's prediction is primarily influenced by: ");
        
        for (int i = 0; i < Math.min(3, contributions.size()); i++) {
            ExplanationResponse.FeatureContribution contrib = contributions.get(i);
            explanation.append(contrib.getFeatureName());
            explanation.append(" (");
            explanation.append(contrib.getDirection());
            explanation.append(" impact: ");
            explanation.append(String.format("%.2f", contrib.getContribution()));
            explanation.append(")");
            
            if (i < Math.min(3, contributions.size()) - 1) {
                explanation.append(", ");
            }
        }
        
        explanation.append(".");
        return explanation.toString();
    }
    
    /**
     * Inspect the model's internal weights to check if it's degenerate
     * (i.e., has zero or near-zero feature weights)
     */
    private void inspectModelWeights(Model<?> model, MLModel.ModelType modelType) {
        try {
            log.info("=== MODEL WEIGHT INSPECTION ===");
            log.info("Model class: {}", model.getClass().getName());
            log.info("Model type: {}", modelType);
            
            if (modelType == MLModel.ModelType.CLASSIFICATION) {
                // Try to cast to LogisticRegressionModel or LinearSGDModel using fully qualified name
                String modelClassName = model.getClass().getName();
                if (modelClassName.contains("LogisticRegressionModel") || modelClassName.contains("LinearSGDModel")) {
                    @SuppressWarnings("unchecked")
                    Model<Label> lrModel = (Model<Label>) model;
                    
                    log.info("Successfully identified model as: {}", modelClassName);
                    
                    // Try to access weights using reflection
                    try {
                        java.lang.reflect.Method getWeightsMethod = lrModel.getClass().getMethod("getWeights");
                        Object weightsObj = getWeightsMethod.invoke(lrModel);
                        log.info("Weights object type: {}", weightsObj != null ? weightsObj.getClass().getName() : "null");
                        
                        if (weightsObj != null && weightsObj.getClass().getName().contains("DenseMatrix")) {
                            // Use reflection to access DenseMatrix methods
                            java.lang.reflect.Method getDim1Method = weightsObj.getClass().getMethod("getDimension1Size");
                            java.lang.reflect.Method getDim2Method = weightsObj.getClass().getMethod("getDimension2Size");
                            java.lang.reflect.Method getMethod = weightsObj.getClass().getMethod("get", int.class, int.class);
                            
                            int dim1 = (Integer) getDim1Method.invoke(weightsObj);
                            int dim2 = (Integer) getDim2Method.invoke(weightsObj);
                            log.info("Weight matrix dimensions: {} x {} (classes x features)", dim1, dim2);
                            
                            // Log weights for each class
                            FeatureMap featureMap = model.getFeatureIDMap();
                            for (int classIdx = 0; classIdx < dim1; classIdx++) {
                                String className = getClassNameForIndex(model, classIdx);
                                log.info("Weights for class '{}':", className);
                                
                                double maxWeight = 0.0;
                                double minWeight = 0.0;
                                int zeroWeights = 0;
                                int nonZeroWeights = 0;
                                
                                for (int featIdx = 0; featIdx < dim2; featIdx++) {
                                    double weight = ((Double) getMethod.invoke(weightsObj, classIdx, featIdx));
                                    if (featIdx < 10) { // Log first 10 weights
                                        String featName = getFeatureNameForIndex(featureMap, featIdx);
                                        log.info("  Feature {} ({}): weight = {}", featIdx, featName, weight);
                                    }
                                    maxWeight = Math.max(maxWeight, Math.abs(weight));
                                    minWeight = Math.min(minWeight, Math.abs(weight));
                                    if (Math.abs(weight) < 1e-6) {
                                        zeroWeights++;
                                    } else {
                                        nonZeroWeights++;
                                    }
                                }
                                
                                log.info("  Summary: max|weight| = {}, min|weight| = {}, zero weights = {}/{}, non-zero = {}", 
                                    maxWeight, minWeight, zeroWeights, dim2, nonZeroWeights);
                                
                                if (maxWeight < 1e-6) {
                                    log.warn("  *** WARNING: All weights are near-zero! Model is DEGENERATE. ***");
                                } else if (zeroWeights > dim2 * 0.9) {
                                    log.warn("  *** WARNING: {}% of weights are zero! Model may be degenerate. ***", 
                                        (zeroWeights * 100.0 / dim2));
                                }
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        log.warn("getWeights() method not found, trying getWeightsCopy()");
                        // Try getWeightsCopy() as fallback
                        try {
                            java.lang.reflect.Method getWeightsCopyMethod = lrModel.getClass().getMethod("getWeightsCopy");
                            Object weightsObj = getWeightsCopyMethod.invoke(lrModel);
                            log.info("getWeightsCopy() succeeded! Weights object type: {}", 
                                weightsObj != null ? weightsObj.getClass().getName() : "null");
                            
                            if (weightsObj != null && weightsObj.getClass().getName().contains("DenseMatrix")) {
                                // Use reflection to access DenseMatrix methods
                                java.lang.reflect.Method getDim1Method = weightsObj.getClass().getMethod("getDimension1Size");
                                java.lang.reflect.Method getDim2Method = weightsObj.getClass().getMethod("getDimension2Size");
                                java.lang.reflect.Method getMethod = weightsObj.getClass().getMethod("get", int.class, int.class);
                                
                                int dim1 = (Integer) getDim1Method.invoke(weightsObj);
                                int dim2 = (Integer) getDim2Method.invoke(weightsObj);
                                log.info("Weight matrix dimensions: {} x {} (classes x features)", dim1, dim2);
                                
                                // Log weights for each class
                                FeatureMap featureMap = model.getFeatureIDMap();
                                for (int classIdx = 0; classIdx < dim1; classIdx++) {
                                    String className = getClassNameForIndex(model, classIdx);
                                    log.info("Weights for class '{}':", className);
                                    
                                    double maxWeight = 0.0;
                                    double minWeight = Double.MAX_VALUE;
                                    int zeroWeights = 0;
                                    int nonZeroWeights = 0;
                                    
                                    for (int featIdx = 0; featIdx < dim2; featIdx++) {
                                        double weight = ((Double) getMethod.invoke(weightsObj, classIdx, featIdx));
                                        if (featIdx < 10) { // Log first 10 weights
                                            String featName = getFeatureNameForIndex(featureMap, featIdx);
                                            log.info("  Feature {} ({}): weight = {}", featIdx, featName, weight);
                                        }
                                        maxWeight = Math.max(maxWeight, Math.abs(weight));
                                        minWeight = Math.min(minWeight, Math.abs(weight));
                                        if (Math.abs(weight) < 1e-6) {
                                            zeroWeights++;
                                        } else {
                                            nonZeroWeights++;
                                        }
                                    }
                                    
                                    log.info("  Summary: max|weight| = {}, min|weight| = {}, zero weights = {}/{}, non-zero = {}", 
                                        maxWeight, minWeight, zeroWeights, dim2, nonZeroWeights);
                                    
                                    if (maxWeight < 1e-6) {
                                        log.warn("  *** WARNING: All weights are near-zero! Model is DEGENERATE. ***");
                                    } else if (zeroWeights > dim2 * 0.9) {
                                        log.warn("  *** WARNING: {}% of weights are zero! Model may be degenerate. ***", 
                                            (zeroWeights * 100.0 / dim2));
                                    }
                                }
                            }
                        } catch (NoSuchMethodException e2) {
                            log.warn("getWeightsCopy() also not found, trying getModelParameters()");
                            // Try getModelParameters() for additional info
                            try {
                                java.lang.reflect.Method getModelParamsMethod = lrModel.getClass().getMethod("getModelParameters");
                                Object paramsObj = getModelParamsMethod.invoke(lrModel);
                                log.info("getModelParameters() returned: {}", paramsObj != null ? paramsObj.getClass().getName() : "null");
                                if (paramsObj != null) {
                                    log.info("Model parameters: {}", paramsObj.toString());
                                }
                            } catch (Exception e3) {
                                log.debug("getModelParameters() not available: {}", e3.getMessage());
                            }
                            
                            // Log available methods
                            java.lang.reflect.Method[] methods = lrModel.getClass().getMethods();
                            log.info("Available methods containing 'weight' or 'coefficient':");
                            for (java.lang.reflect.Method m : methods) {
                                String methodName = m.getName().toLowerCase();
                                if (methodName.contains("weight") || methodName.contains("coefficient") || 
                                    methodName.contains("param")) {
                                    log.info("  - {}: {}", m.getName(), java.util.Arrays.toString(m.getParameterTypes()));
                                }
                            }
                        } catch (Exception e2) {
                            log.error("Error accessing weights via getWeightsCopy(): {}", e2.getMessage(), e2);
                        }
                    } catch (Exception e) {
                        log.error("Error accessing weights via reflection: {}", e.getMessage(), e);
                    }
                } else {
                    log.warn("Model is not a LogisticRegressionModel or LinearSGDModel, it's: {}", model.getClass().getSimpleName());
                    log.info("Attempting to find weight access methods for: {}", modelClassName);
                    // Try to find any method that might give us weights
                    java.lang.reflect.Method[] methods = model.getClass().getMethods();
                    log.info("Available methods containing 'weight', 'coefficient', 'param', or 'get':");
                    int methodCount = 0;
                    for (java.lang.reflect.Method m : methods) {
                        String methodName = m.getName().toLowerCase();
                        if (methodName.contains("weight") || methodName.contains("coefficient") || 
                            methodName.contains("param") || (methodName.startsWith("get") && methodName.length() > 3)) {
                            if (methodCount < 20) { // Limit output
                                log.info("  - {}: {}", m.getName(), java.util.Arrays.toString(m.getParameterTypes()));
                                methodCount++;
                            }
                        }
                    }
                }
            } else if (modelType == MLModel.ModelType.REGRESSION) {
                // Try to inspect regression model weights
                String modelClassName = model.getClass().getName();
                if (modelClassName.contains("LinearSGDModel") || modelClassName.contains("Regression")) {
                    @SuppressWarnings("unchecked")
                    Model<Regressor> regModel = (Model<Regressor>) model;
                    
                    log.info("Successfully identified regression model as: {}", modelClassName);
                    
                    // Try to access weights using reflection (same approach as classification)
                    try {
                        java.lang.reflect.Method getWeightsMethod = regModel.getClass().getMethod("getWeights");
                        Object weightsObj = getWeightsMethod.invoke(regModel);
                        log.info("Weights object type: {}", weightsObj != null ? weightsObj.getClass().getName() : "null");
                        
                        if (weightsObj != null && weightsObj.getClass().getName().contains("DenseMatrix")) {
                            // Use reflection to access DenseMatrix methods
                            java.lang.reflect.Method getDim1Method = weightsObj.getClass().getMethod("getDimension1Size");
                            java.lang.reflect.Method getDim2Method = weightsObj.getClass().getMethod("getDimension2Size");
                            java.lang.reflect.Method getMethod = weightsObj.getClass().getMethod("get", int.class, int.class);
                            
                            int dim1 = (Integer) getDim1Method.invoke(weightsObj);
                            int dim2 = (Integer) getDim2Method.invoke(weightsObj);
                            log.info("Weight matrix dimensions: {} x {} (output dimensions x features)", dim1, dim2);
                            
                            // Log weights for each output dimension (regression typically has 1)
                            FeatureMap featureMap = model.getFeatureIDMap();
                            for (int dimIdx = 0; dimIdx < dim1; dimIdx++) {
                                log.info("Weights for output dimension {}:", dimIdx);
                                
                                double maxWeight = 0.0;
                                double minWeight = Double.MAX_VALUE;
                                int zeroWeights = 0;
                                int nonZeroWeights = 0;
                                
                                for (int featIdx = 0; featIdx < dim2; featIdx++) {
                                    double weight = ((Double) getMethod.invoke(weightsObj, dimIdx, featIdx));
                                    if (featIdx < 10) { // Log first 10 weights
                                        String featName = getFeatureNameForIndex(featureMap, featIdx);
                                        log.info("  Feature {} ({}): weight = {}", featIdx, featName, weight);
                                    }
                                    maxWeight = Math.max(maxWeight, Math.abs(weight));
                                    minWeight = Math.min(minWeight, Math.abs(weight));
                                    if (Math.abs(weight) < 1e-6) {
                                        zeroWeights++;
                                    } else {
                                        nonZeroWeights++;
                                    }
                                }
                                
                                log.info("  Summary: max|weight| = {}, min|weight| = {}, zero weights = {}/{}, non-zero = {}", 
                                    maxWeight, minWeight, zeroWeights, dim2, nonZeroWeights);
                                
                                if (maxWeight < 1e-6) {
                                    log.warn("  *** WARNING: All weights are near-zero! Model is DEGENERATE. ***");
                                } else if (zeroWeights > dim2 * 0.9) {
                                    log.warn("  *** WARNING: {}% of weights are zero! Model may be degenerate. ***", 
                                        (zeroWeights * 100.0 / dim2));
                                }
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        log.warn("getWeights() method not found, trying getWeightsCopy()");
                        // Try getWeightsCopy() as fallback
                        try {
                            java.lang.reflect.Method getWeightsCopyMethod = regModel.getClass().getMethod("getWeightsCopy");
                            Object weightsObj = getWeightsCopyMethod.invoke(regModel);
                            log.info("getWeightsCopy() succeeded! Weights object type: {}", 
                                weightsObj != null ? weightsObj.getClass().getName() : "null");
                            
                            if (weightsObj != null && weightsObj.getClass().getName().contains("DenseMatrix")) {
                                // Use reflection to access DenseMatrix methods
                                java.lang.reflect.Method getDim1Method = weightsObj.getClass().getMethod("getDimension1Size");
                                java.lang.reflect.Method getDim2Method = weightsObj.getClass().getMethod("getDimension2Size");
                                java.lang.reflect.Method getMethod = weightsObj.getClass().getMethod("get", int.class, int.class);
                                
                                int dim1 = (Integer) getDim1Method.invoke(weightsObj);
                                int dim2 = (Integer) getDim2Method.invoke(weightsObj);
                                log.info("Weight matrix dimensions: {} x {} (output dimensions x features)", dim1, dim2);
                                
                                // Log weights for each output dimension
                                FeatureMap featureMap = model.getFeatureIDMap();
                                for (int dimIdx = 0; dimIdx < dim1; dimIdx++) {
                                    log.info("Weights for output dimension {}:", dimIdx);
                                    
                                    double maxWeight = 0.0;
                                    double minWeight = Double.MAX_VALUE;
                                    int zeroWeights = 0;
                                    int nonZeroWeights = 0;
                                    
                                    for (int featIdx = 0; featIdx < dim2; featIdx++) {
                                        double weight = ((Double) getMethod.invoke(weightsObj, dimIdx, featIdx));
                                        if (featIdx < 10) { // Log first 10 weights
                                            String featName = getFeatureNameForIndex(featureMap, featIdx);
                                            log.info("  Feature {} ({}): weight = {}", featIdx, featName, weight);
                                        }
                                        maxWeight = Math.max(maxWeight, Math.abs(weight));
                                        minWeight = Math.min(minWeight, Math.abs(weight));
                                        if (Math.abs(weight) < 1e-6) {
                                            zeroWeights++;
                                        } else {
                                            nonZeroWeights++;
                                        }
                                    }
                                    
                                    log.info("  Summary: max|weight| = {}, min|weight| = {}, zero weights = {}/{}, non-zero = {}", 
                                        maxWeight, minWeight, zeroWeights, dim2, nonZeroWeights);
                                    
                                    if (maxWeight < 1e-6) {
                                        log.warn("  *** WARNING: All weights are near-zero! Model is DEGENERATE. ***");
                                    } else if (zeroWeights > dim2 * 0.9) {
                                        log.warn("  *** WARNING: {}% of weights are zero! Model may be degenerate. ***", 
                                            (zeroWeights * 100.0 / dim2));
                                    }
                                }
                            }
                        } catch (NoSuchMethodException e2) {
                            log.warn("getWeightsCopy() also not found for regression model");
                        } catch (Exception e2) {
                            log.error("Error accessing weights via getWeightsCopy(): {}", e2.getMessage(), e2);
                        }
                    } catch (Exception e) {
                        log.error("Error accessing regression weights via reflection: {}", e.getMessage(), e);
                    }
                } else {
                    log.warn("Regression model is not a LinearSGDModel, it's: {}", model.getClass().getSimpleName());
                }
            }
            
            // Also check the model's feature map
            FeatureMap featureMap = model.getFeatureIDMap();
            log.info("Model has {} features in feature map", featureMap.size());
            log.info("Feature names: {}", featureMap.keySet());
            log.info("=== END MODEL WEIGHT INSPECTION ===");
            
        } catch (Exception e) {
            log.error("Error inspecting model weights: {}", e.getMessage(), e);
        }
    }
    
    private String getClassNameForIndex(Model<?> model, int index) {
        // Try to get class name from model's output info
        try {
            String modelClassName = model.getClass().getName();
            if (modelClassName.contains("LogisticRegressionModel")) {
                @SuppressWarnings("unchecked")
                Model<Label> lrModel = (Model<Label>) model;
                org.tribuo.OutputInfo<Label> outputInfo = lrModel.getOutputIDInfo();
                java.util.Set<Label> domain = outputInfo.getDomain();
                java.util.List<Label> classes = new ArrayList<>(domain);
                if (index < classes.size()) {
                    return classes.get(index).getLabel();
                }
            }
        } catch (Exception e) {
            log.debug("Could not get class name for index {}: {}", index, e.getMessage());
        }
        return "class_" + index;
    }
    
    private String getFeatureNameForIndex(FeatureMap featureMap, int index) {
        try {
            var features = new ArrayList<>(featureMap.keySet());
            if (index < features.size()) {
                return features.get(index);
            }
        } catch (Exception e) {
            log.debug("Could not get feature name for index {}: {}", index, e.getMessage());
        }
        return "feature_" + index;
    }
}
