package com.example.xaiapp.exception;

/**
 * Exception thrown when a requested dataset is not found
 * 
 * This exception is thrown when attempting to access a dataset that
 * doesn't exist or the user doesn't have permission to access.
 * 
 * @since 1.0.0
 */
public class DatasetNotFoundException extends DatasetException {
    
    public DatasetNotFoundException(Long datasetId) {
        super("Dataset with ID " + datasetId + " not found", 
              "The requested dataset could not be found. It may have been deleted or you may not have permission to access it.");
    }
    
    public DatasetNotFoundException(String fileName) {
        super("Dataset with filename " + fileName + " not found", 
              "The requested dataset could not be found. It may have been deleted or you may not have permission to access it.");
    }
}
