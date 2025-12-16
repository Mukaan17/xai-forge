package com.example.xaiapp.exception;

/**
 * Exception thrown when authentication fails
 * 
 * This exception is thrown when there are issues with user authentication,
 * such as invalid credentials, expired tokens, or authentication service errors.
 * 
 * @since 1.0.0
 */
public class AuthenticationException extends XaiException {
    
    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message, "Authentication failed. Please check your credentials and try again.");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_ERROR", message, "Authentication failed. Please check your credentials and try again.", cause);
    }
}
