/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:14:04
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:03
 */
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
 * @author Mukhil Sundararaj
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
