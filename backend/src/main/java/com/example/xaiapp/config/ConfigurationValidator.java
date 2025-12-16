package com.example.xaiapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration validator for startup validation
 * 
 * This component validates critical configuration on application startup
 * to ensure the application is properly configured before accepting requests.
 * 
 * @since 1.0.0
 */
@Component
@Slf4j
public class ConfigurationValidator {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationValidator.class);
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    @Value("${app.ml.max-training-time}")
    private long maxTrainingTime;
    
    @Value("${app.ml.regression.learning-rate}")
    private double regressionLearningRate;
    
    @Value("${app.ml.regression.epochs}")
    private int regressionEpochs;
    
    @PostConstruct
    public void validateConfiguration() {
        log.info("Starting configuration validation...");
        
        validateUploadDirectory();
        validateMLParameters();
        
        log.info("Configuration validation completed successfully");
    }
    
    /**
     * Validate upload directory configuration
     */
    private void validateUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                log.info("Creating upload directory: {}", uploadDir);
                Files.createDirectories(uploadPath);
            }
            
            if (!Files.isWritable(uploadPath)) {
                throw new IllegalStateException("Upload directory is not writable: " + uploadDir);
            }
            
            log.info("Upload directory validation passed: {}", uploadDir);
            
        } catch (Exception e) {
            throw new IllegalStateException("Failed to validate upload directory: " + uploadDir, e);
        }
    }
    
    /**
     * Validate ML training parameters
     */
    private void validateMLParameters() {
        // Validate training timeout
        if (maxTrainingTime <= 0) {
            throw new IllegalStateException("ML training timeout must be positive, got: " + maxTrainingTime);
        }
        
        if (maxTrainingTime > 3600000) { // 1 hour
            log.warn("ML training timeout is very high: {}ms ({} minutes)", 
                maxTrainingTime, maxTrainingTime / 60000);
        }
        
        // Validate regression parameters
        if (regressionLearningRate <= 0 || regressionLearningRate > 1.0) {
            throw new IllegalStateException("Regression learning rate must be between 0 and 1, got: " + regressionLearningRate);
        }
        
        if (regressionEpochs <= 0 || regressionEpochs > 1000) {
            throw new IllegalStateException("Regression epochs must be between 1 and 1000, got: " + regressionEpochs);
        }
        
        log.info("ML parameters validation passed - Learning rate: {}, Epochs: {}, Max time: {}ms", 
            regressionLearningRate, regressionEpochs, maxTrainingTime);
    }
}
