package com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for session management.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Session management endpoints")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @Operation(summary = "List active sessions")
    public ResponseEntity<List<SessionDTO>> getActiveSessions(
            @CurrentUser UserPrincipal currentUser,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String sessionToken = extractSessionToken(authHeader);
        List<Map<String, Object>> sessionsMap = sessionService.getActiveSessions(currentUser.getId(), sessionToken);
        List<SessionDTO> sessions = sessionsMap.stream()
            .map(this::mapToSessionDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Revoke a session")
    public ResponseEntity<MessageResponse> revokeSession(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long sessionId) {
        sessionService.revokeSession(currentUser.getId(), sessionId);
        return ResponseEntity.ok(new MessageResponse("Session revoked successfully"));
    }

    @DeleteMapping("/others")
    @Operation(summary = "Revoke all other sessions")
    public ResponseEntity<MessageResponse> revokeOtherSessions(
            @CurrentUser UserPrincipal currentUser,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String sessionToken = extractSessionToken(authHeader);
        sessionService.revokeAllOtherSessions(currentUser.getId(), sessionToken);
        return ResponseEntity.ok(new MessageResponse("All other sessions revoked"));
    }

    @GetMapping("/history")
    @Operation(summary = "Get login history")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistory(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> historyMap = sessionService.getLoginHistory(currentUser.getId(), limit);
        List<LoginHistoryDTO> history = historyMap.stream()
            .map(this::mapToLoginHistoryDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    private String extractSessionToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private SessionDTO mapToSessionDTO(Map<String, Object> map) {
        return SessionDTO.builder()
            .id(getLong(map, "id"))
            .deviceInfo((String) map.get("deviceInfo"))
            .ipAddress((String) map.get("ipAddress"))
            .location((String) map.get("location"))
            .lastActiveAt((java.time.LocalDateTime) map.get("lastActiveAt"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .isCurrentSession((Boolean) map.get("isCurrentSession"))
            .build();
    }

    private LoginHistoryDTO mapToLoginHistoryDTO(Map<String, Object> map) {
        return LoginHistoryDTO.builder()
            .success((Boolean) map.get("success"))
            .deviceInfo((String) map.get("deviceInfo"))
            .ipAddress((String) map.get("ipAddress"))
            .location((String) map.get("location"))
            .timestamp((java.time.LocalDateTime) map.get("timestamp"))
            .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
}
