package com.xaiforge.common.exception;

/**
 * Exception thrown when an internal server error occurs.
 */
public class InternalServerException extends XaiForgeException {
    
    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }
    
    public InternalServerException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_ERROR, message, cause);
    }
}
