package com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.CreateApiKeyRequest;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.ApiKey.ApiKeyEnvironment;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for API key management.
 */
@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "API key management endpoints")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    @Operation(summary = "List API keys")
    public ResponseEntity<List<ApiKeyDTO>> getApiKeys(@CurrentUser UserPrincipal currentUser) {
        List<Map<String, Object>> keysMap = apiKeyService.getApiKeys(currentUser.getId());
        List<ApiKeyDTO> keys = keysMap.stream()
            .map(this::mapToApiKeyDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(keys);
    }

    @PostMapping
    @Operation(summary = "Generate new API key",
               description = "Returns the full key ONLY on creation. Store it securely.")
    public ResponseEntity<ApiKeyResponseDTO> generateApiKey(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateApiKeyRequest request) {
        Map<String, Object> responseMap = apiKeyService.generateApiKey(
            currentUser.getId(), request.getName(), request.getEnvironment(),
            request.getPermissions(), request.getDescription(), request.getExpiresAt());
        ApiKeyResponseDTO response = mapToApiKeyResponseDTO(responseMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke API key")
    public ResponseEntity<MessageResponse> revokeApiKey(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long keyId) {
        apiKeyService.revokeApiKey(currentUser.getId(), keyId);
        return ResponseEntity.ok(new MessageResponse("API key revoked successfully"));
    }

    private ApiKeyDTO mapToApiKeyDTO(Map<String, Object> map) {
        return ApiKeyDTO.builder()
            .id(getLong(map, "id"))
            .name((String) map.get("name"))
            .keyPreview((String) map.get("keyPreview"))
            .environment(map.get("environment") != null ? ((ApiKeyEnvironment) map.get("environment")).name() : null)
            .permissions((java.util.Set<String>) map.get("permissions"))
            .active((Boolean) map.get("active"))
            .lastUsedAt((java.time.LocalDateTime) map.get("lastUsedAt"))
            .usageCount(getLong(map, "usageCount"))
            .expiresAt((java.time.LocalDateTime) map.get("expiresAt"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .build();
    }

    private ApiKeyResponseDTO mapToApiKeyResponseDTO(Map<String, Object> map) {
        return ApiKeyResponseDTO.builder()
            .id(getLong(map, "id"))
            .name((String) map.get("name"))
            .key((String) map.get("key"))
            .keyPreview((String) map.get("keyPreview"))
            .environment(map.get("environment") != null ? ((ApiKeyEnvironment) map.get("environment")).name() : null)
            .permissions((java.util.Set<String>) map.get("permissions"))
            .expiresAt((java.time.LocalDateTime) map.get("expiresAt"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
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
