package com.example.xaiapp.exception;

/**
 * Exception thrown when model training fails
 * 
 * This exception is thrown when there are issues during the ML model
 * training process, such as insufficient data, invalid parameters,
 * or training timeouts.
 * 
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
