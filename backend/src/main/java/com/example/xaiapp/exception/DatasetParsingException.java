/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:19
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 15:18:23
 */
package com.example.xaiapp.exception;

/**
 * Exception thrown when dataset parsing fails
 * 
 * This exception is thrown when there are issues parsing the uploaded
 * CSV file, such as malformed headers, invalid data types, or encoding issues.
 * 
 * @author Mukhil Sundararaj
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
