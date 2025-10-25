/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:11
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:36
 */
package com.example.xaiapp.exception;

/**
 * Exception thrown when a requested model is not found
 * 
 * This exception is thrown when attempting to access a model that
 * doesn't exist or the user doesn't have permission to access.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
public class ModelNotFoundException extends XaiException {
    
    public ModelNotFoundException(Long modelId) {
        super("MODEL_NOT_FOUND", 
              "Model with ID " + modelId + " not found", 
              "The requested model could not be found. It may have been deleted or you may not have permission to access it.");
    }
    
    public ModelNotFoundException(String modelName) {
        super("MODEL_NOT_FOUND", 
              "Model with name " + modelName + " not found", 
              "The requested model could not be found. It may have been deleted or you may not have permission to access it.");
    }
}
