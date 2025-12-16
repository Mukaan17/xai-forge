package com.example.xaiapp.builder;

import com.example.xaiapp.dto.PredictionResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for PredictionResponse objects
 * 
 * This builder provides a fluent interface for creating PredictionResponse
 * objects with proper validation and default values.
 * 
 * @since 1.0.0
 */
public class PredictionResponseBuilder {
    
    private String prediction;
    private Double confidence;
    private Map<String, Object> probabilities;
    private Map<String, String> inputData;
    
    /**
     * Create a new PredictionResponseBuilder instance
     * 
     * @return A new PredictionResponseBuilder
     */
    public static PredictionResponseBuilder builder() {
        return new PredictionResponseBuilder();
    }
    
    /**
     * Set the prediction value
     * 
     * @param prediction The prediction value
     * @return This builder instance
     */
    public PredictionResponseBuilder setPrediction(String prediction) {
        this.prediction = prediction;
        return this;
    }
    
    /**
     * Set the confidence score
     * 
     * @param confidence The confidence score (0.0 to 1.0)
     * @return This builder instance
     */
    public PredictionResponseBuilder setConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }
    
    /**
     * Set the probabilities map
     * 
     * @param probabilities The probabilities for each class
     * @return This builder instance
     */
    public PredictionResponseBuilder setProbabilities(Map<String, Object> probabilities) {
        this.probabilities = new HashMap<>(probabilities);
        return this;
    }
    
    /**
     * Add a probability entry
     * 
     * @param className The class name
     * @param probability The probability value
     * @return This builder instance
     */
    public PredictionResponseBuilder addProbability(String className, Object probability) {
        if (this.probabilities == null) {
            this.probabilities = new HashMap<>();
        }
        this.probabilities.put(className, probability);
        return this;
    }
    
    /**
     * Set the input data
     * 
     * @param inputData The input data used for prediction
     * @return This builder instance
     */
    public PredictionResponseBuilder setInputData(Map<String, String> inputData) {
        this.inputData = new HashMap<>(inputData);
        return this;
    }
    
    /**
     * Add input data entry
     * 
     * @param key The input key
     * @param value The input value
     * @return This builder instance
     */
    public PredictionResponseBuilder addInputData(String key, String value) {
        if (this.inputData == null) {
            this.inputData = new HashMap<>();
        }
        this.inputData.put(key, value);
        return this;
    }
    
    /**
     * Build the PredictionResponse object
     * 
     * @return The built PredictionResponse
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public PredictionResponse build() {
        validate();
        PredictionResponse response = new PredictionResponse();
        response.setPrediction(prediction);
        response.setConfidence(confidence);
        response.setProbabilities(probabilities);
        response.setInputData(inputData);
        return response;
    }
    
    /**
     * Validate the builder state
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private void validate() {
        if (prediction == null || prediction.trim().isEmpty()) {
            throw new IllegalArgumentException("Prediction value is required");
        }
        
        if (confidence == null) {
            throw new IllegalArgumentException("Confidence score is required");
        }
        
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence score must be between 0.0 and 1.0");
        }
        
        if (inputData == null || inputData.isEmpty()) {
            throw new IllegalArgumentException("Input data is required");
        }
    }
}
