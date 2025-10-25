/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:08
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:36
 */
package com.example.xaiapp.exception;

/**
 * Exception thrown when model training fails
 * 
 * This exception is thrown when there are issues during the ML model
 * training process, such as insufficient data, invalid parameters,
 * or training timeouts.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
public class ModelTrainingException extends XaiException {
    
    public ModelTrainingException(String message) {
        super("MODEL_TRAINING_ERROR", message, "Model training failed. Please check your data and try again.");
    }
    
    public ModelTrainingException(String message, Throwable cause) {
        super("MODEL_TRAINING_ERROR", message, "Model training failed. Please check your data and try again.", cause);
    }
    
    public ModelTrainingException(String message, String userMessage) {
        super("MODEL_TRAINING_ERROR", message, userMessage);
    }
}
