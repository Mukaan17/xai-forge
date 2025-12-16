package com.example.xaiapp.exception;

/**
 * Exception thrown when system resources are exhausted
 * 
 * This exception is thrown when the system cannot fulfill a request
 * due to resource limitations such as memory, disk space, or connection limits.
 * 
 * @since 1.0.0
 */
public class ResourceExhaustedException extends XaiException {
    
    public ResourceExhaustedException(String message) {
        super("RESOURCE_EXHAUSTED", message, "System resources are currently unavailable. Please try again later.");
    }
    
    public ResourceExhaustedException(String resource, String limit) {
        super("RESOURCE_EXHAUSTED", 
              resource + " limit exceeded: " + limit, 
              "The system has reached its " + resource + " limit. Please try again later or contact support.");
    }
}
