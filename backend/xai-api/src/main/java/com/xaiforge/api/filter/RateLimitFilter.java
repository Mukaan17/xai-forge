package com.xaiforge.api.filter;

import com.xaiforge.common.exception.RateLimitExceededException;
import com.xaiforge.infrastructure.cache.RateLimitService;
import com.xaiforge.infrastructure.cache.RateLimitService.RateLimitInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Not a @Component - registered via FilterRegistrationBean in WebConfig
public class RateLimitFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final RateLimitService rateLimitService;
    
    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting for health checks and public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get user ID from security context
        String clientId = getClientId(request);
        String endpoint = request.getMethod() + ":" + path;
        
        if (!rateLimitService.isAllowed(clientId, endpoint)) {
            RateLimitInfo info = rateLimitService.getRateLimitInfo(clientId, endpoint);
            log.warn("Rate limit exceeded for client {} on endpoint {}", clientId, endpoint);
            throw new RateLimitExceededException(info.limit(), info.resetAfterSeconds());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Extract user ID from authentication
            return authentication.getName(); // username as client ID
        }
        // For unauthenticated requests, use IP address
        return getClientIpAddress(request);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

