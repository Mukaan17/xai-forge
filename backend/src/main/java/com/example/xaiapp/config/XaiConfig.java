package com.example.xaiapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for XAI (Explainable AI) explanation generation
 * 
 * This configuration class provides configurable parameters for generating
 * model explanations, replacing hardcoded magic numbers with environment-based
 * configuration.
 * 
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.xai")
@Data
public class XaiConfig {
    
    /**
     * Base contribution factor for regression models
     */
    private double regressionBaseFactor = 0.2;
    
    /**
     * Base contribution factor for classification models
     */
    private double classificationBaseFactor = 0.25;
    
    /**
     * Feature importance multipliers for domain-specific scaling
     */
    private Map<String, Double> featureMultipliers = new HashMap<>();
    
    /**
     * Enable fallback explanation when model weight extraction fails
     */
    private boolean enableFallbackExplanation = true;
    
    /**
     * Maximum number of features to show in explanations
     */
    private int maxFeaturesInExplanation = 10;
    
    /**
     * Minimum contribution threshold for features to be included
     */
    private double minContributionThreshold = 0.01;
    
    /**
     * Initialize default feature multipliers
     */
    public XaiConfig() {
        // Initialize default feature multipliers
        featureMultipliers.put("score", 1.5);
        featureMultipliers.put("grade", 1.5);
        featureMultipliers.put("age", 1.2);
        featureMultipliers.put("year", 1.2);
        featureMultipliers.put("length", 2.0);
        featureMultipliers.put("width", 2.0);
        featureMultipliers.put("color", 1.8);
        featureMultipliers.put("type", 1.8);
        featureMultipliers.put("size", 1.5);
        featureMultipliers.put("area", 1.5);
        featureMultipliers.put("count", 1.3);
        featureMultipliers.put("number", 1.3);
    }
    
    /**
     * Get feature multiplier for a given feature name
     * 
     * @param featureName The feature name to look up
     * @return The multiplier value, or 1.0 if not found
     */
    public double getFeatureMultiplier(String featureName) {
        if (featureName == null) {
            return 1.0;
        }
        
        // Check for exact match first
        if (featureMultipliers.containsKey(featureName.toLowerCase())) {
            return featureMultipliers.get(featureName.toLowerCase());
        }
        
        // Check for partial matches (case-insensitive)
        for (Map.Entry<String, Double> entry : featureMultipliers.entrySet()) {
            if (featureName.toLowerCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return 1.0; // Default multiplier
    }
    
    // Manual getters (Lombok not generating them)
    public double getRegressionBaseFactor() { return regressionBaseFactor; }
    public double getClassificationBaseFactor() { return classificationBaseFactor; }
    public Map<String, Double> getFeatureMultipliers() { return featureMultipliers; }
    public boolean isEnableFallbackExplanation() { return enableFallbackExplanation; }
    public int getMaxFeaturesInExplanation() { return maxFeaturesInExplanation; }
    public double getMinContributionThreshold() { return minContributionThreshold; }
}
