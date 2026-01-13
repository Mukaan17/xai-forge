package com.xaiforge.common.exception;

/**
 * Exception thrown when a model is not found.
 */
public class ModelNotFoundException extends XaiForgeException {
    
    public ModelNotFoundException(Long modelId) {
        super(
            ErrorCode.MODEL_NOT_FOUND,
            String.format("Model with ID %d not found", modelId)
        );
        withMetadata("modelId", modelId);
    }
    
    public ModelNotFoundException(String modelName) {
        super(
            ErrorCode.MODEL_NOT_FOUND,
            String.format("Model with name '%s' not found", modelName)
        );
        withMetadata("modelName", modelName);
    }
}

