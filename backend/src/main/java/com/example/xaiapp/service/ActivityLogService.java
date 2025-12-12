package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.ActivityLog.ActionType;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.ActivityLogRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.DeviceParser;
import com.example.xaiapp.util.GeoIpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for audit logging all user actions.
 * Provides comprehensive activity tracking for security and compliance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final GeoIpService geoIpService;
    private final DeviceParser deviceParser;

    /**
     * Log an activity with full context.
     * Automatically extracts IP and device info from current request.
     */
    public void logActivity(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata) {

        logActivity(userId, action, resourceType, resourceId, resourceName, 
            description, metadata, true, null, null);
    }

    /**
     * Log an activity with explicit success/failure status.
     */
    public void logActivity(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata,
            boolean success,
            String errorMessage,
            Long durationMs) {

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        // Extract request info
        String ipAddress = "unknown";
        String userAgent = null;
        String deviceInfo = null;
        String location = null;

        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = extractIpAddress(request);
                userAgent = request.getHeader("User-Agent");
                deviceInfo = deviceParser.parseUserAgent(userAgent);
                location = geoIpService.getLocation(ipAddress);
            }
        } catch (Exception e) {
            log.debug("Could not extract request info for activity log", e);
        }

        ActivityLog activityLog = ActivityLog.builder()
            .user(user)
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .resourceName(resourceName)
            .description(description)
            .metadata(metadata != null ? metadata : new HashMap<>())
            .success(success)
            .errorMessage(errorMessage)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .durationMs(durationMs)
            .build();

        activityLogRepository.save(activityLog);

        log.debug("Activity logged: userId={}, action={}, resource={}:{}", 
            userId, action, resourceType, resourceId);
    }

    /**
     * Log activity asynchronously (for non-critical logging).
     */
    @Async
    public void logActivityAsync(
            Long userId,
            ActionType action,
            String resourceType,
            Long resourceId,
            String resourceName,
            String description,
            Map<String, Object> metadata) {
        
        logActivity(userId, action, resourceType, resourceId, resourceName, description, metadata);
    }

    /**
     * Get paginated activity logs for a user.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogs(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get activity logs within a date range.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogs(
            Long userId, 
            LocalDateTime start, 
            LocalDateTime end, 
            Pageable pageable) {
        
        return activityLogRepository.findByUserIdAndCreatedAtBetween(userId, start, end, pageable);
    }

    /**
     * Get activity logs filtered by action type.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLogsByAction(
            Long userId, 
            ActionType action, 
            Pageable pageable) {
        
        return activityLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(userId, action, pageable);
    }

    /**
     * Get recent activity for dashboard (limited).
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivity(Long userId, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return activityLogRepository.findRecentByUserId(userId, pageable);
    }

    /**
     * Export activity logs to CSV.
     */
    @Transactional(readOnly = true)
    public byte[] exportActivityLogsToCsv(Long userId, LocalDateTime start, LocalDateTime end) {
        List<ActivityLog> logs = activityLogRepository
            .findByUserIdAndCreatedAtBetween(userId, start, end, Pageable.unpaged())
            .getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // Header
        writer.println("Timestamp,Action,Resource Type,Resource Name,Description,IP Address,Location,Success");

        // Data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ActivityLog log : logs) {
            writer.printf("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                log.getCreatedAt().format(formatter),
                log.getAction().name(),
                nullSafe(log.getResourceType()),
                escapeCSV(log.getResourceName()),
                escapeCSV(log.getDescription()),
                nullSafe(log.getIpAddress()),
                escapeCSV(log.getLocation()),
                log.getSuccess()
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    /**
     * Get activity statistics for a user.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getActivityStatistics(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("logins", activityLogRepository.countByUserIdAndActionSince(
            userId, ActionType.LOGIN_SUCCESS, since));
        stats.put("predictions", activityLogRepository.countByUserIdAndActionSince(
            userId, ActionType.PREDICTION_MADE, since));
        stats.put("modelsTrained", activityLogRepository.countByUserIdAndActionSince(
            userId, ActionType.MODEL_TRAINING_COMPLETED, since));
        stats.put("datasetsUploaded", activityLogRepository.countByUserIdAndActionSince(
            userId, ActionType.DATASET_UPLOADED, since));
        return stats;
    }

    /**
     * Delete old activity logs (data retention).
     */
    public void deleteOldLogs(Long userId, int retentionDays) {
        LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
        activityLogRepository.deleteOldLogs(userId, before);
        log.info("Deleted old activity logs for user {}", userId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String maskIpAddress(String ip) {
        if (ip == null) return null;
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return ip;
    }

    private String nullSafe(String str) {
        return str != null ? str : "";
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
