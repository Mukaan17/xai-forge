package com.xaiforge.common.exception;

/**
 * Exception thrown when authentication credentials are invalid.
 */
public class InvalidCredentialsException extends XaiForgeException {
    
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
}

