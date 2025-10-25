/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:30
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:40
 */
package com.example.xaiapp.exception;

/**
 * Exception thrown when concurrent modification is detected
 * 
 * This exception is thrown when multiple users attempt to modify
 * the same resource simultaneously, causing data inconsistency.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
public class ConcurrentModificationException extends XaiException {
    
    public ConcurrentModificationException(String message) {
        super("CONCURRENT_MODIFICATION", message, "The resource was modified by another user. Please refresh and try again.");
    }
    
    public ConcurrentModificationException(String resource, String operation) {
        super("CONCURRENT_MODIFICATION", 
              "Concurrent modification detected for " + resource + " during " + operation, 
              "The " + resource + " was modified by another user. Please refresh and try again.");
    }
}
