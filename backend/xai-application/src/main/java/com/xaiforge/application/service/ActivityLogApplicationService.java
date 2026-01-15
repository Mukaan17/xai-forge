package com.xaiforge.application.service;

import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for audit logging all user actions.
 * Provides comprehensive activity tracking for security and compliance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogApplicationService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Log an activity with full context.
     * Automatically extracts IP and device info from current request.
     */
    public void logActivity(
            Long userId,
            ActivityLog.EventType eventType,
            String details,
            Map<String, Object> metadata) {

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        // Extract request info
        String ipAddress = "unknown";
        String userAgent = null;

        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = extractIpAddress(request);
                userAgent = request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not extract request info for activity log", e);
        }

        // Convert metadata to JSON string if provided
        String detailsJson = details;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> fullDetails = new HashMap<>();
                if (details != null) {
                    fullDetails.put("description", details);
                }
                fullDetails.putAll(metadata);
                detailsJson = mapper.writeValueAsString(fullDetails);
            } catch (Exception e) {
                log.warn("Failed to serialize metadata to JSON", e);
            }
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setEventType(eventType);
        activityLog.setDetails(detailsJson);
        activityLog.setIpAddress(ipAddress);
        activityLog.setUserAgent(userAgent);

        activityLogRepository.save(activityLog);

        log.debug("Activity logged: userId={}, eventType={}", userId, eventType);
    }

    /**
     * Log activity asynchronously (for non-critical logging).
     */
    @Async
    public void logActivityAsync(
            Long userId,
            ActivityLog.EventType eventType,
            String details,
            Map<String, Object> metadata) {
        
        logActivity(userId, eventType, details, metadata);
    }

    /**
     * Extract IP address from request, handling proxies.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
