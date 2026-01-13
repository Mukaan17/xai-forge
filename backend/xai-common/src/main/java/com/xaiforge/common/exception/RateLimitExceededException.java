package com.xaiforge.common.exception;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitExceededException extends XaiForgeException {
    
    public RateLimitExceededException(int limit, long retryAfterSeconds) {
        super(
            ErrorCode.RATE_LIMIT_EXCEEDED,
            String.format("Rate limit of %d requests exceeded. Retry after %d seconds", 
                limit, retryAfterSeconds)
        );
        withMetadata("limit", limit);
        withMetadata("retryAfter", retryAfterSeconds);
    }
}

