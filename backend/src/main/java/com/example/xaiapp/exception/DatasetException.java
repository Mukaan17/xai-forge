/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:14
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:39
 */
package com.example.xaiapp.exception;

/**
 * Base exception for dataset-related errors
 * 
 * This exception is thrown when there are issues with dataset operations
 * such as upload, parsing, or validation.
 * 
 * @author Mukhil Sundararaj
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
