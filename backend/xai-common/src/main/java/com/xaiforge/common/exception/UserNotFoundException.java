package com.xaiforge.common.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends XaiForgeException {
    
    public UserNotFoundException(Long userId) {
        super(
            ErrorCode.USER_NOT_FOUND,
            String.format("User with ID %d not found", userId)
        );
        withMetadata("userId", userId);
    }
    
    public UserNotFoundException(String username) {
        super(
            ErrorCode.USER_NOT_FOUND,
            String.format("User with username '%s' not found", username)
        );
        withMetadata("username", username);
    }
}

