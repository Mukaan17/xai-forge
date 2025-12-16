package com.example.xaiapp.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationResponse {
    
    private String prediction;
    private List<FeatureContribution> featureContributions;
    private Map<String, String> inputData;
    private String explanationText;
    private String warning; // Warning message if model is degenerate or explanations are approximate
    
    // Manual setters (Lombok not generating them)
    public void setPrediction(String prediction) { this.prediction = prediction; }
    public void setFeatureContributions(List<FeatureContribution> featureContributions) { this.featureContributions = featureContributions; }
    public void setInputData(Map<String, String> inputData) { this.inputData = inputData; }
    public void setExplanationText(String explanationText) { this.explanationText = explanationText; }
    public void setWarning(String warning) { this.warning = warning; }
    public String getWarning() { return warning; }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureContribution {
        private String featureName;
        private Double contribution;
        private String direction; // "positive" or "negative"
        
        // Manual getters and setters (Lombok not generating them)
        public String getFeatureName() { return featureName; }
        public void setFeatureName(String featureName) { this.featureName = featureName; }
        public Double getContribution() { return contribution; }
        public void setContribution(Double contribution) { this.contribution = contribution; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }
}
