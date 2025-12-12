package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.UserSession;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.ActivityLogRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.repository.UserSessionRepository;
import com.example.xaiapp.util.DeviceParser;
import com.example.xaiapp.util.GeoIpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user sessions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final GeoIpService geoIpService;
    private final DeviceParser deviceParser;

    @Value("${app.session.expiration-hours:24}")
    private int sessionExpirationHours;

    /**
     * Create a new session after successful login.
     */
    public UserSession createSession(Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = deviceParser.parseUserAgent(userAgent);
        String location = geoIpService.getLocation(ipAddress);

        UserSession session = UserSession.builder()
            .user(user)
            .sessionToken(UUID.randomUUID().toString())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .countryCode(geoIpService.getCountryCode(ipAddress))
            .isActive(true)
            .lastActiveAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(sessionExpirationHours))
            .build();

        session = sessionRepository.save(session);
        log.info("Session created: userId={}, sessionId={}, device={}", userId, session.getId(), deviceInfo);
        return session;
    }

    /**
     * Get all active sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveSessions(Long userId, String currentSessionToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        return sessions.stream()
            .map(session -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", session.getId());
                dto.put("deviceInfo", session.getDeviceInfo());
                dto.put("ipAddress", maskIpAddress(session.getIpAddress()));
                dto.put("location", session.getLocation());
                dto.put("lastActiveAt", session.getLastActiveAt());
                dto.put("createdAt", session.getCreatedAt());
                dto.put("isCurrentSession", session.getSessionToken().equals(currentSessionToken));
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Update last active timestamp for a session.
     */
    public void updateLastActive(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
            .ifPresent(session -> {
                session.setLastActiveAt(LocalDateTime.now());
                sessionRepository.save(session);
            });
    }

    /**
     * Revoke a specific session.
     */
    public void revokeSession(Long userId, Long sessionId) {
        UserSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.revoke("User requested");
        sessionRepository.save(session);
        log.info("Session revoked: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * Revoke all sessions except the current one.
     */
    public void revokeAllOtherSessions(Long userId, String currentSessionToken) {
        UserSession currentSession = sessionRepository.findBySessionToken(currentSessionToken)
            .orElseThrow(() -> new ResourceNotFoundException("Current session not found"));
        sessionRepository.deactivateAllByUserIdExcept(userId, currentSession.getId(), LocalDateTime.now());
        log.info("All other sessions revoked: userId={}", userId);
    }

    /**
     * Revoke all sessions for a user.
     */
    public void revokeAllSessions(Long userId) {
        sessionRepository.deactivateAllByUserId(userId, LocalDateTime.now());
        log.info("All sessions revoked: userId={}", userId);
    }

    /**
     * Get login history for a user.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLoginHistory(Long userId, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        Page<ActivityLog> loginActivities = activityLogRepository.findByUserIdAndActionIn(
            userId, List.of(ActivityLog.ActionType.LOGIN_SUCCESS, ActivityLog.ActionType.LOGIN_FAILED), pageable);
        return loginActivities.getContent().stream()
            .map(log -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("success", log.getSuccess());
                dto.put("deviceInfo", log.getDeviceInfo());
                dto.put("ipAddress", maskIpAddress(log.getIpAddress()));
                dto.put("location", log.getLocation());
                dto.put("timestamp", log.getCreatedAt());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Record a login attempt.
     */
    public void recordLoginAttempt(String email, boolean success, HttpServletRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = deviceParser.parseUserAgent(userAgent);
        String location = geoIpService.getLocation(ipAddress);
        User user = userRepository.findByEmail(email).orElse(null);

        ActivityLog.ActionType action = success ? ActivityLog.ActionType.LOGIN_SUCCESS : ActivityLog.ActionType.LOGIN_FAILED;
        ActivityLog log = ActivityLog.builder()
            .user(user)
            .action(action)
            .resourceType("USER")
            .resourceId(user != null ? user.getId() : null)
            .description(success ? "Successful login" : "Failed login attempt")
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .deviceInfo(deviceInfo)
            .location(location)
            .success(success)
            .metadata(Map.of("email", email))
            .build();
        activityLogRepository.save(log);
    }

    /**
     * Validate a session token.
     */
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
            .map(UserSession::isValid)
            .orElse(false);
    }

    /**
     * Clean up expired sessions.
     */
    public int cleanupExpiredSessions() {
        List<UserSession> expired = sessionRepository.findExpiredSessions(LocalDateTime.now());
        for (UserSession session : expired) {
            session.revoke("Session expired");
        }
        sessionRepository.saveAll(expired);
        log.info("Cleaned up {} expired sessions", expired.size());
        return expired.size();
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
}
