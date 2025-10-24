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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.xaiapp.dto.ExplanationResponse;
import com.example.xaiapp.dto.PredictionResponse;
import com.example.xaiapp.entity.MLModel;

@Service
@RequiredArgsConstructor
@Slf4j
public class XaiService {
    
    private final ModelService modelService;
    
    public PredictionResponse predict(Long modelId, Map<String, String> inputData, Long userId) {
        try {
            MLModel mlModel = modelService.getModel(modelId, userId);
            Model<?> model = deserializeModel(mlModel.getSerializedModelPath());
            
            // Create example from input data
            Example<?> example = createExampleFromInput(inputData, mlModel);
            
            // Make prediction
            Prediction<?> prediction = model.predict(example);
            
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
            
            // Create example from input data
            Example<?> example = createExampleFromInput(inputData, mlModel);
            
            // Make prediction
            Prediction<?> prediction = model.predict(example);
            
            // Generate LIME explanation
            List<ExplanationResponse.FeatureContribution> contributions = generateLimeExplanation(
                model, example, mlModel.getFeatureNames(), mlModel.getModelType());
            
            // Create explanation response
            ExplanationResponse response = new ExplanationResponse();
            response.setPrediction(getPredictionString(prediction, mlModel.getModelType()));
            response.setFeatureContributions(contributions);
            response.setInputData(inputData);
            response.setExplanationText(generateExplanationText(contributions, prediction));
            
            return response;
            
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
    
    private Example<?> createExampleFromInput(Map<String, String> inputData, MLModel mlModel) {
        // Create feature map
        Map<String, Double> features = new HashMap<>();
        for (String featureName : mlModel.getFeatureNames()) {
            String value = inputData.get(featureName);
            if (value != null) {
                try {
                    features.put(featureName, Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    // For non-numeric features, use hash code as numeric representation
                    features.put(featureName, (double) value.hashCode());
                }
            }
        }
        
        // Create example based on model type
        if (mlModel.getModelType() == MLModel.ModelType.CLASSIFICATION) {
            return ExampleFactory.createExample(new Label("unknown"), features);
        } else {
            return ExampleFactory.createExample(new Regressor("target", 0.0), features);
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
            response.setPrediction(predictedLabel.getLabel());
            response.setConfidence(labelPrediction.getOutputScore().get(predictedLabel));
            
            // Convert scores to probabilities
            Map<String, Object> probabilities = new HashMap<>();
            for (Map.Entry<Label, Double> entry : labelPrediction.getOutputScore().entrySet()) {
                probabilities.put(entry.getKey().getLabel(), entry.getValue());
            }
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
    
    private List<ExplanationResponse.FeatureContribution> generateLimeExplanation(
            Model<?> model, Example<?> example, List<String> featureNames, MLModel.ModelType modelType) {
        
        List<ExplanationResponse.FeatureContribution> contributions = new ArrayList<>();
        
        // Simplified LIME implementation
        // In a real implementation, you would use Tribuo's LIME explainer
        // For now, we'll create a mock explanation based on feature values
        
        Map<String, Double> featureValues = new HashMap<>();
        for (String featureName : featureNames) {
            if (example.getFeatureSet().contains(featureName)) {
                featureValues.put(featureName, example.getFeatureSet().get(featureName).getValue());
            }
        }
        
        // Generate mock contributions (in real implementation, use actual LIME)
        for (Map.Entry<String, Double> entry : featureValues.entrySet()) {
            String featureName = entry.getKey();
            Double value = entry.getValue();
            
            // Mock contribution calculation
            double contribution = value * (Math.random() - 0.5) * 2; // Random between -1 and 1
            
            ExplanationResponse.FeatureContribution contrib = new ExplanationResponse.FeatureContribution();
            contrib.setFeatureName(featureName);
            contrib.setContribution(Math.abs(contribution));
            contrib.setDirection(contribution >= 0 ? "positive" : "negative");
            
            contributions.add(contrib);
        }
        
        // Sort by absolute contribution value
        contributions.sort((a, b) -> Double.compare(b.getContribution(), a.getContribution()));
        
        return contributions;
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
}
