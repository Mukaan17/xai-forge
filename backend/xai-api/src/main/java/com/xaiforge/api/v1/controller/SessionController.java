package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.SessionApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.SessionDto;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for session management.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessions", description = "Session management endpoints")
@RequiredArgsConstructor
public class SessionController {
    
    private final SessionApplicationService sessionService;
    
    @GetMapping
    @Operation(summary = "List active sessions")
    public ResponseEntity<List<SessionDto>> getActiveSessions(
            Authentication authentication,
            HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        String currentToken = extractToken(request);
        List<SessionDto> sessions = sessionService.getActiveSessions(user.getId(), currentToken);
        return ResponseEntity.ok(sessions);
    }
    
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Revoke a session")
    @LogActivity(
        eventType = "SESSION_REVOKED",
        description = "Session revoked: #{#sessionId}",
        resourceType = "SESSION",
        resourceId = "#{#sessionId}"
    )
    public ResponseEntity<Map<String, String>> revokeSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        sessionService.revokeSession(user.getId(), sessionId);
        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }
    
    @DeleteMapping("/others")
    @Operation(summary = "Revoke all other sessions")
    @LogActivity(
        eventType = "ALL_OTHER_SESSIONS_REVOKED",
        description = "All other sessions revoked",
        resourceType = "SESSION"
    )
    public ResponseEntity<Map<String, String>> revokeOtherSessions(
            Authentication authentication,
            HttpServletRequest request) {
        User user = (User) authentication.getPrincipal();
        String currentToken = extractToken(request);
        sessionService.revokeAllOtherSessions(user.getId(), currentToken);
        return ResponseEntity.ok(Map.of("message", "All other sessions revoked successfully"));
    }
    
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
