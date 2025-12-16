package com.example.xaiapp.exception;

/**
 * Base exception class for XAI-Forge application
 * 
 * All custom exceptions in the application should extend this class
 * to provide consistent error handling and logging.
 * 
 * @since 1.0.0
 */
public class XaiException extends RuntimeException {
    
    private final String errorCode;
    private final String userMessage;
    
    public XaiException(String message) {
        super(message);
        this.errorCode = "XAI_ERROR";
        this.userMessage = "An unexpected error occurred";
    }
    
    public XaiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "XAI_ERROR";
        this.userMessage = "An unexpected error occurred";
    }
    
    public XaiException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public XaiException(String errorCode, String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}
