package com.example.xaiapp.exception;

/**
 * Exception thrown when dataset parsing fails
 * 
 * This exception is thrown when there are issues parsing the uploaded
 * CSV file, such as malformed headers, invalid data types, or encoding issues.
 * 
 * @since 1.0.0
 */
public class DatasetParsingException extends DatasetException {
    
    public DatasetParsingException(String message) {
        super("Dataset parsing failed: " + message, 
              "The uploaded file could not be parsed. Please check that it's a valid CSV file with proper headers and data.");
    }
    
    public DatasetParsingException(String message, Throwable cause) {
        super("Dataset parsing failed: " + message, cause);
    }
}
