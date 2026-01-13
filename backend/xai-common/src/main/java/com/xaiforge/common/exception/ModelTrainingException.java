package com.xaiforge.common.exception;

/**
 * Exception thrown when model training fails.
 */
public class ModelTrainingException extends XaiForgeException {
    
    public ModelTrainingException(String reason) {
        super(ErrorCode.TRAINING_FAILED, reason);
    }
    
    public ModelTrainingException(String reason, Throwable cause) {
        super(ErrorCode.TRAINING_FAILED, reason, cause);
    }
    
    public ModelTrainingException(String reason, Long datasetId, Throwable cause) {
        super(ErrorCode.TRAINING_FAILED, reason, cause);
        withMetadata("datasetId", datasetId);
    }
    
    public ModelTrainingException(String reason, Long datasetId) {
        super(ErrorCode.TRAINING_FAILED, reason);
        withMetadata("datasetId", datasetId);
    }
}

