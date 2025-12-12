package com.example.xaiapp.exception;

/**
 * Exception thrown when file storage operations fail.
 */
public class FileStorageException extends XaiException {
    
    public FileStorageException(String message) {
        super("FILE_STORAGE_ERROR", message, "File operation failed");
    }
    
    public FileStorageException(String message, Throwable cause) {
        super("FILE_STORAGE_ERROR", message, "File operation failed", cause);
    }
}
