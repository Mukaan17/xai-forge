package com.xaiforge.common.exception;

/**
 * Error codes for categorizing exceptions in the XAI-Forge application.
 * Each error code follows the pattern: [CATEGORY][NUMBER]
 */
public enum ErrorCode {
    // Validation errors (400)
    VALIDATION_FAILED("VAL001", "Validation failed"),
    INVALID_INPUT("VAL002", "Invalid input provided"),
    FILE_TOO_LARGE("VAL003", "File exceeds size limit"),
    UNSUPPORTED_FORMAT("VAL004", "Unsupported file format"),
    
    // Authentication errors (401)
    INVALID_CREDENTIALS("AUTH001", "Invalid credentials"),
    TOKEN_EXPIRED("AUTH002", "Token has expired"),
    TOKEN_INVALID("AUTH003", "Invalid token"),
    
    // Authorization errors (403)
    ACCESS_DENIED("AUTHZ001", "Access denied"),
    RESOURCE_FORBIDDEN("AUTHZ002", "Resource access forbidden"),
    
    // Not found errors (404)
    DATASET_NOT_FOUND("RES001", "Dataset not found"),
    MODEL_NOT_FOUND("RES002", "Model not found"),
    USER_NOT_FOUND("RES003", "User not found"),
    PREDICTION_NOT_FOUND("RES004", "Prediction not found"),
    
    // Conflict errors (409)
    DUPLICATE_RESOURCE("CONF001", "Resource already exists"),
    CONCURRENT_MODIFICATION("CONF002", "Resource was modified by another request"),
    
    // Business logic errors (422)
    TRAINING_FAILED("BUS001", "Model training failed"),
    PREDICTION_FAILED("BUS002", "Prediction failed"),
    INSUFFICIENT_DATA("BUS003", "Insufficient data for operation"),
    INVALID_MODEL_STATE("BUS004", "Model is not in valid state for this operation"),
    
    // Rate limiting (429)
    RATE_LIMIT_EXCEEDED("RATE001", "Rate limit exceeded"),
    
    // Server errors (500)
    INTERNAL_ERROR("SRV001", "Internal server error"),
    DATABASE_ERROR("SRV002", "Database operation failed"),
    CACHE_ERROR("SRV003", "Cache operation failed"),
    ML_ENGINE_ERROR("SRV004", "ML engine error"),
    
    // Service unavailable (503)
    SERVICE_UNAVAILABLE("SRV005", "Service temporarily unavailable"),
    DEPENDENCY_FAILED("SRV006", "Dependent service failed");
    
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
}

