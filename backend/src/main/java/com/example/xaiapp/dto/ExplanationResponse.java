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
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureContribution {
        private String featureName;
        private Double contribution;
        private String direction; // "positive" or "negative"
    }
}
