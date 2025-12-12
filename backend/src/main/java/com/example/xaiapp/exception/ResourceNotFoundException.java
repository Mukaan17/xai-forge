package com.example.xaiapp.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends XaiException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, "The requested resource was not found");
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super("RESOURCE_NOT_FOUND", message, "The requested resource was not found", cause);
    }
}
