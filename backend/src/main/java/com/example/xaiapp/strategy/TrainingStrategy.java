package com.example.xaiapp.strategy;

import org.tribuo.MutableDataset;
import org.tribuo.Model;

import java.util.Map;

/**
 * Strategy interface for ML model training algorithms
 * 
 * This interface defines the contract for different machine learning
 * training strategies, allowing for easy extension and modification
 * of training algorithms.
 * 
 * @since 1.0.0
 */
public interface TrainingStrategy {
    
    /**
     * Train a model using the provided dataset and parameters
     * 
     * @param dataset The training dataset
     * @param parameters Additional training parameters
     * @return The trained model
     * @throws Exception if training fails
     */
    Model<?> train(MutableDataset<?> dataset, Map<String, Object> parameters) throws Exception;
    
    /**
     * Get the name of the algorithm
     * 
     * @return The algorithm name
     */
    String getAlgorithmName();
    
    /**
     * Get the model type this strategy supports
     * 
     * @return The model type
     */
    String getModelType();
    
    /**
     * Validate the dataset for this strategy
     * 
     * @param dataset The dataset to validate
     * @throws IllegalArgumentException if the dataset is invalid for this strategy
     */
    void validateDataset(MutableDataset<?> dataset) throws IllegalArgumentException;
}
