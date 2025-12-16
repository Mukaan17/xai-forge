package com.example.xaiapp.exception;

/**
 * Exception thrown when authorization fails
 * 
 * This exception is thrown when a user attempts to access resources
 * they don't have permission to access.
 * 
 * @since 1.0.0
 */
public class AuthorizationException extends XaiException {
    
    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message, "You don't have permission to access this resource.");
    }
    
    public AuthorizationException(String resource, String action) {
        super("AUTHORIZATION_ERROR", 
              "User not authorized to " + action + " " + resource, 
              "You don't have permission to " + action + " this " + resource + ".");
    }
}
