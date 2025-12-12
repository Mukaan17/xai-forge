package com.example.xaiapp.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends XaiException {
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, message, cause);
    }
}
