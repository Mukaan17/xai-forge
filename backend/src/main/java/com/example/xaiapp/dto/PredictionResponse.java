package com.example.xaiapp.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    
    private String prediction;
    private Double confidence;
    private Map<String, Object> probabilities;
    private Map<String, String> inputData;
    
    // Manual setters (Lombok not generating them)
    public void setPrediction(String prediction) { this.prediction = prediction; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public void setProbabilities(Map<String, Object> probabilities) { this.probabilities = probabilities; }
    public void setInputData(Map<String, String> inputData) { this.inputData = inputData; }
}
