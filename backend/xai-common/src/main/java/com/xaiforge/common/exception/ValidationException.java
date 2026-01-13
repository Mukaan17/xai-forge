package com.xaiforge.common.exception;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends XaiForgeException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(List<String> validationErrors) {
        super(ErrorCode.VALIDATION_FAILED, "Validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String field, String error) {
        super(ErrorCode.VALIDATION_FAILED, String.format("Validation failed for field '%s': %s", field, error));
        this.validationErrors = List.of(String.format("%s: %s", field, error));
        withMetadata("field", field);
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}

