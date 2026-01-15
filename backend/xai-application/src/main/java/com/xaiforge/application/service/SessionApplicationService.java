package com.xaiforge.application.service;

import com.xaiforge.common.dto.SessionDto;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.domain.user.entity.UserSession;
import com.xaiforge.infrastructure.persistence.user.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for session management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionApplicationService {
    
    private final UserSessionRepository sessionRepository;
    
    /**
     * Create a new session for a user.
     */
    public UserSession createSession(User user, String token, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(token);
        session.setDeviceInfo(extractDeviceInfo(request));
        session.setIpAddress(extractIpAddress(request));
        session.setLocation(null); // Could be enhanced with geolocation service
        // isActive defaults to true in entity
        
        return sessionRepository.save(session);
    }
    
    /**
     * Get all active sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<SessionDto> getActiveSessions(Long userId, String currentToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        return sessions.stream()
            .map(session -> {
                boolean isCurrent = session.getSessionToken().equals(currentToken);
                return new SessionDto(
                    session.getId(),
                    session.getDeviceInfo(),
                    maskIpAddress(session.getIpAddress()),
                    session.getLocation(),
                    session.getLastActiveAt(),
                    session.getCreatedAt(),
                    isCurrent
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Revoke a specific session.
     */
    public void revokeSession(Long userId, Long sessionId) {
        UserSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new ValidationException("Session not found"));
        
        session.revoke("User requested revocation");
        sessionRepository.save(session);
        
        log.info("Session revoked: userId={}, sessionId={}", userId, sessionId);
    }
    
    /**
     * Revoke all sessions except the current one.
     */
    public void revokeAllOtherSessions(Long userId, String currentToken) {
        List<UserSession> sessions = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        int revokedCount = 0;
        
        for (UserSession session : sessions) {
            if (!session.getSessionToken().equals(currentToken)) {
                session.revoke("User revoked all other sessions");
                sessionRepository.save(session);
                revokedCount++;
            }
        }
        
        log.info("Revoked {} other sessions for userId={}", revokedCount, userId);
    }
    
    /**
     * Update last active timestamp for a session.
     */
    public void updateLastActive(String token) {
        sessionRepository.findBySessionToken(token)
            .ifPresent(session -> {
                session.updateLastActive();
                sessionRepository.save(session);
            });
    }
    
    /**
     * Extract device info from request.
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }
        
        // Simple device detection (can be enhanced with a library)
        if (userAgent.contains("Mobile")) {
            return "Mobile Device";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac")) {
            return "macOS";
        } else if (userAgent.contains("Linux")) {
            return "Linux";
        }
        
        return "Unknown Device";
    }
    
    /**
     * Extract IP address from request, handling proxies.
     */
    private String extractIpAddress(HttpServletRequest request) {
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
    
    /**
     * Mask IP address for privacy (show only last octet).
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "Unknown";
        }
        
        // IPv4 masking
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".xxx";
            }
        }
        
        // IPv6 masking (simplified)
        if (ip.contains(":")) {
            return "xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx";
        }
        
        return ip;
    }
}
