package com.example.xaiapp.exception;

import com.example.xaiapp.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for XAI-Forge Application
 * 
 * This class provides centralized exception handling for the entire application,
 * ensuring consistent error responses and proper logging of exceptions.
 * 
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle custom XAI exceptions
     */
    @ExceptionHandler(XaiException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleXaiException(XaiException ex, WebRequest request) {
        log.error("XAI Exception: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle model training exceptions
     */
    @ExceptionHandler(ModelTrainingException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleModelTrainingException(ModelTrainingException ex, WebRequest request) {
        log.error("Model Training Exception: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("MODEL_TRAINING_ERROR")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle model not found exceptions
     */
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleModelNotFoundException(ModelNotFoundException ex, WebRequest request) {
        log.warn("Model Not Found: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("MODEL_NOT_FOUND")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle dataset exceptions
     */
    @ExceptionHandler(DatasetException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleDatasetException(DatasetException ex, WebRequest request) {
        log.error("Dataset Exception: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("DATASET_ERROR")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle dataset not found exceptions
     */
    @ExceptionHandler(DatasetNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleDatasetNotFoundException(DatasetNotFoundException ex, WebRequest request) {
        log.warn("Dataset Not Found: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("DATASET_NOT_FOUND")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<ErrorDetails>> handleAuthenticationException(Exception ex, WebRequest request) {
        log.warn("Authentication Exception: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("AUTHENTICATION_ERROR")
                .message("Authentication failed")
                .userMessage("Invalid credentials. Please check your username and password.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed", errorDetails));
    }
    
    /**
     * Handle authorization exceptions
     */
    @ExceptionHandler({AuthorizationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<ErrorDetails>> handleAuthorizationException(Exception ex, WebRequest request) {
        log.warn("Authorization Exception: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("AUTHORIZATION_ERROR")
                .message("Access denied")
                .userMessage("You don't have permission to access this resource.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", errorDetails));
    }
    
    /**
     * Handle resource exhausted exceptions
     */
    @ExceptionHandler(ResourceExhaustedException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleResourceExhaustedException(ResourceExhaustedException ex, WebRequest request) {
        log.error("Resource Exhausted: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("RESOURCE_EXHAUSTED")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle concurrent modification exceptions
     */
    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleConcurrentModificationException(ConcurrentModificationException ex, WebRequest request) {
        log.warn("Concurrent Modification: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("CONCURRENT_MODIFICATION")
                .message(ex.getMessage())
                .userMessage(ex.getUserMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getUserMessage(), errorDetails));
    }
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation Exception: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint Violation: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Constraint violation", errors));
    }
    
    /**
     * Handle file upload size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("File Upload Size Exceeded: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("FILE_SIZE_EXCEEDED")
                .message("File size exceeds maximum allowed size")
                .userMessage("The uploaded file is too large. Please reduce the file size and try again.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("File size exceeded", errorDetails));
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal Argument: {}", ex.getMessage());
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .userMessage("Invalid input provided. Please check your request and try again.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid argument", errorDetails));
    }
    
    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime Exception: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("RUNTIME_ERROR")
                .message(ex.getMessage())
                .userMessage("An unexpected error occurred. Please try again later.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", errorDetails));
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected Exception: {}", ex.getMessage(), ex);
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .userMessage("An unexpected error occurred. Please try again later.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", errorDetails));
    }
    
    /**
     * Error details class for structured error responses
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorDetails {
        private LocalDateTime timestamp;
        private String errorCode;
        private String message;
        private String userMessage;
        private String path;
        
        // Manual setters (Lombok not generating them)
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public void setMessage(String message) { this.message = message; }
        public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
        public void setPath(String path) { this.path = path; }
        
        // Manual builder (Lombok @Builder not generating it)
        public static ErrorDetailsBuilder builder() {
            return new ErrorDetailsBuilder();
        }
        
        public static class ErrorDetailsBuilder {
            private LocalDateTime timestamp;
            private String errorCode;
            private String message;
            private String userMessage;
            private String path;
            
            public ErrorDetailsBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public ErrorDetailsBuilder errorCode(String errorCode) {
                this.errorCode = errorCode;
                return this;
            }
            
            public ErrorDetailsBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public ErrorDetailsBuilder userMessage(String userMessage) {
                this.userMessage = userMessage;
                return this;
            }
            
            public ErrorDetailsBuilder path(String path) {
                this.path = path;
                return this;
            }
            
            public ErrorDetails build() {
                ErrorDetails details = new ErrorDetails();
                details.setTimestamp(timestamp);
                details.setErrorCode(errorCode);
                details.setMessage(message);
                details.setUserMessage(userMessage);
                details.setPath(path);
                return details;
            }
        }
    }
}
