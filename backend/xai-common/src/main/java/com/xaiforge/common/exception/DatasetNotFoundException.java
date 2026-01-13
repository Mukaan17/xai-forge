package com.xaiforge.common.exception;

/**
 * Exception thrown when a dataset is not found.
 */
public class DatasetNotFoundException extends XaiForgeException {
    
    public DatasetNotFoundException(Long datasetId) {
        super(
            ErrorCode.DATASET_NOT_FOUND,
            String.format("Dataset with ID %d not found", datasetId)
        );
        withMetadata("datasetId", datasetId);
    }
    
    public DatasetNotFoundException(String fileName) {
        super(
            ErrorCode.DATASET_NOT_FOUND,
            String.format("Dataset with name '%s' not found", fileName)
        );
        withMetadata("fileName", fileName);
    }
}

