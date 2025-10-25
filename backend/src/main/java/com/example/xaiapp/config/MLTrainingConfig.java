/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 18:31:22
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:39:19
 */
package com.example.xaiapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration for ML training parameters
 * 
 * This configuration class provides configurable parameters for machine learning
 * model training, replacing hardcoded values with environment-based configuration.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.ml")
@Data
public class MLTrainingConfig {
    
    /**
     * Maximum training time in milliseconds
     */
    private long maxTrainingTime = 300000; // 5 minutes default
    
    /**
     * Maximum dataset size for training
     */
    private long maxDatasetSize = 100000;
    
    /**
     * Cache configuration
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * Regression-specific training parameters
     */
    private RegressionConfig regression = new RegressionConfig();
    
    /**
     * Classification-specific training parameters
     */
    private ClassificationConfig classification = new ClassificationConfig();
    
    @Data
    public static class CacheConfig {
        private boolean enabled = true;
        private int maxSize = 100;
        private long ttl = 3600000; // 1 hour in milliseconds
    }
    
    @Data
    public static class RegressionConfig {
        private double learningRate = 0.1;
        private double initialLearningRate = 0.1;
        private int epochs = 10;
        private int minibatchSize = 1;
        private long maxTrainingTime = 300000; // 5 minutes
    }
    
    @Data
    public static class ClassificationConfig {
        private long maxTrainingTime = 300000; // 5 minutes
        // Additional classification parameters can be added here
    }
}
