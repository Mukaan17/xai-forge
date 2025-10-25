/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-10-24 12:13:24
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:38:42
 */
package com.example.xaiapp.exception;

/**
 * Exception thrown when authorization fails
 * 
 * This exception is thrown when a user attempts to access resources
 * they don't have permission to access.
 * 
 * @author Mukhil Sundararaj
 * @since 1.0.0
 */
public class AuthorizationException extends XaiException {
    
    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message, "You don't have permission to access this resource.");
    }
    
    public AuthorizationException(String resource, String action) {
        super("AUTHORIZATION_ERROR", 
              "User not authorized to " + action + " " + resource, 
              "You don't have permission to " + action + " this " + resource + ".");
    }
}
