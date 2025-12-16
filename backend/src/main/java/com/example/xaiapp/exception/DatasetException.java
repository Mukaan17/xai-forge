package com.example.xaiapp.exception;

/**
 * Base exception for dataset-related errors
 * 
 * This exception is thrown when there are issues with dataset operations
 * such as upload, parsing, or validation.
 * 
 * @since 1.0.0
 */
public class DatasetException extends XaiException {
    
    public DatasetException(String message) {
        super("DATASET_ERROR", message, "There was an error processing your dataset. Please check the file format and try again.");
    }
    
    public DatasetException(String message, Throwable cause) {
        super("DATASET_ERROR", message, "There was an error processing your dataset. Please check the file format and try again.", cause);
    }
    
    public DatasetException(String message, String userMessage) {
        super("DATASET_ERROR", message, userMessage);
    }
}
