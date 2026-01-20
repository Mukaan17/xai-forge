package com.xaiforge.api.config;

import com.xaiforge.common.exception.ErrorCode;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.common.exception.XaiForgeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler that converts exceptions to RFC 7807 Problem Detail format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(XaiForgeException.class)
    public ResponseEntity<ProblemDetail> handleXaiForgeException(
            XaiForgeException ex, WebRequest request) {
        
        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
        ProblemDetail problem = createProblemDetail(ex, status, request);
        
        logException(ex, request, status);
        
        return ResponseEntity.status(status).body(problem);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("/errors/validation-failed"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getDescription(false).replace("uri=", ""));
        
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ValidationError(
                fe.getField(),
                fe.getDefaultMessage(),
                fe.getRejectedValue()))
            .collect(Collectors.toList());
        
        problem.setProperty("errors", errors);
        
        log.warn("Validation failed: {}", errors);
        
        return ResponseEntity.badRequest().body(problem);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("/errors/validation-failed"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        
        List<ValidationError> errors = ex.getConstraintViolations().stream()
            .map(cv -> new ValidationError(
                getPropertyPath(cv),
                cv.getMessage(),
                cv.getInvalidValue()))
            .collect(Collectors.toList());
        
        problem.setProperty("errors", errors);
        
        return ResponseEntity.badRequest().body(problem);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("/errors/validation-failed"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", ex.getValidationErrors());
        
        if (!ex.getMetadata().isEmpty()) {
            problem.setProperty("details", ex.getMetadata());
        }
        
        return ResponseEntity.badRequest().body(problem);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Invalid credentials");
        problem.setType(URI.create("/errors/invalid-credentials"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        
        log.warn("Authentication failed: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDatabaseException(
            DataAccessException ex, WebRequest request) {
        
        log.error("Database error: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Database operation failed. Please try again.");
        problem.setType(URI.create("/errors/database-error"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", ErrorCode.DATABASE_ERROR.getCode());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ProblemDetail> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        
        // Check if this is the Spring Security authentication null pointer
        String message = ex.getMessage();
        if (message != null && message.contains("Authentication.getPrincipal()")) {
            log.warn("Spring Security authentication access error on public endpoint: {}", request.getDescription(false));
            // Return a more user-friendly error
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred during registration. Please try again.");
            problem.setType(URI.create("/errors/internal-error"));
            problem.setProperty("correlationId", MDC.get("correlationId"));
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("errorCode", ErrorCode.INTERNAL_ERROR.getCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
        }
        
        // For other null pointer exceptions, log and handle normally
        log.error("NullPointerException: {}", ex.getMessage(), ex);
        return handleUnexpectedException(ex, request);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Our team has been notified.");
        problem.setType(URI.create("/errors/internal-error"));
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", ErrorCode.INTERNAL_ERROR.getCode());
        
        // Only expose internal details in development
        if (isDevEnvironment()) {
            problem.setProperty("exception", ex.getClass().getSimpleName());
            problem.setProperty("message", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
    
    private HttpStatus mapErrorCodeToStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_FAILED, INVALID_INPUT, FILE_TOO_LARGE, UNSUPPORTED_FORMAT -> 
                HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID -> 
                HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED, RESOURCE_FORBIDDEN -> 
                HttpStatus.FORBIDDEN;
            case DATASET_NOT_FOUND, MODEL_NOT_FOUND, USER_NOT_FOUND, PREDICTION_NOT_FOUND -> 
                HttpStatus.NOT_FOUND;
            case DUPLICATE_RESOURCE, CONCURRENT_MODIFICATION -> 
                HttpStatus.CONFLICT;
            case TRAINING_FAILED, PREDICTION_FAILED, INSUFFICIENT_DATA, INVALID_MODEL_STATE -> 
                HttpStatus.UNPROCESSABLE_ENTITY;
            case RATE_LIMIT_EXCEEDED -> 
                HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL_ERROR, DATABASE_ERROR, CACHE_ERROR, ML_ENGINE_ERROR -> 
                HttpStatus.INTERNAL_SERVER_ERROR;
            case SERVICE_UNAVAILABLE, DEPENDENCY_FAILED -> 
                HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
    
    private ProblemDetail createProblemDetail(XaiForgeException ex, 
                                               HttpStatus status,
                                               WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setType(URI.create("/errors/" + ex.getErrorCode().getCode().toLowerCase()));
        problem.setProperty("errorCode", ex.getErrorCode().getCode());
        problem.setProperty("correlationId", MDC.get("correlationId"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getDescription(false).replace("uri=", ""));
        
        if (!ex.getMetadata().isEmpty()) {
            problem.setProperty("details", ex.getMetadata());
        }
        
        return problem;
    }
    
    private void logException(XaiForgeException ex, WebRequest request, HttpStatus status) {
        if (status.is5xxServerError()) {
            log.error("Server error [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage(), ex);
        } else if (status.is4xxClientError()) {
            log.warn("Client error [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());
        } else {
            log.info("Exception [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());
        }
    }
    
    private String getPropertyPath(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
    }
    
    private boolean isDevEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("dev") || profile.contains("development");
    }
    
    /**
     * Validation error details for Problem Detail response.
     */
    public record ValidationError(String field, String message, Object rejectedValue) {}
}

