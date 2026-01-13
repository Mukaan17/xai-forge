package com.xaiforge.common.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all XAI-Forge exceptions.
 * Provides error code categorization and metadata support.
 */
public abstract class XaiForgeException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Map<String, Object> metadata;
    
    protected XaiForgeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.metadata = new HashMap<>();
    }
    
    protected XaiForgeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.metadata = new HashMap<>();
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public XaiForgeException withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}

