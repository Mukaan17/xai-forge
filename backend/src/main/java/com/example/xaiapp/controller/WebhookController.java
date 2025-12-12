package com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.CreateWebhookRequest;
import com.example.xaiapp.dto.request.UpdateWebhookRequest;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.WebhookService;
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
 * REST controller for webhook management.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook management endpoints")
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    @Operation(summary = "List webhooks")
    public ResponseEntity<List<WebhookDTO>> getWebhooks(@CurrentUser UserPrincipal currentUser) {
        List<Map<String, Object>> webhooksMap = webhookService.getWebhooks(currentUser.getId());
        List<WebhookDTO> webhooks = webhooksMap.stream()
            .map(this::mapToWebhookDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(webhooks);
    }

    @PostMapping
    @Operation(summary = "Create webhook")
    public ResponseEntity<WebhookDTO> createWebhook(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateWebhookRequest request) {
        Map<String, Object> webhookMap = webhookService.createWebhook(
            currentUser.getId(), request.getName(), request.getUrl(),
            request.getEvents(), request.getDescription());
        WebhookDTO webhook = mapToWebhookDTO(webhookMap);
        return ResponseEntity.status(HttpStatus.CREATED).body(webhook);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update webhook")
    public ResponseEntity<WebhookDTO> updateWebhook(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWebhookRequest request) {
        Map<String, Object> webhookMap = webhookService.updateWebhook(
            currentUser.getId(), id, request.getName(), request.getUrl(),
            request.getEvents(), request.getDescription(), request.getActive());
        WebhookDTO webhook = mapToWebhookDTO(webhookMap);
        return ResponseEntity.ok(webhook);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete webhook")
    public ResponseEntity<Void> deleteWebhook(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        webhookService.deleteWebhook(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test webhook")
    public ResponseEntity<WebhookTestResultDTO> testWebhook(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        Map<String, Object> resultMap = webhookService.testWebhook(currentUser.getId(), id);
        WebhookTestResultDTO result = mapToWebhookTestResultDTO(resultMap);
        return ResponseEntity.ok(result);
    }

    private WebhookDTO mapToWebhookDTO(Map<String, Object> map) {
        return WebhookDTO.builder()
            .id(getLong(map, "id"))
            .name((String) map.get("name"))
            .url((String) map.get("url"))
            .events((java.util.Set<String>) map.get("events"))
            .description((String) map.get("description"))
            .active((Boolean) map.get("active"))
            .autoDisabled((Boolean) map.get("autoDisabled"))
            .lastTriggeredAt((java.time.LocalDateTime) map.get("lastTriggeredAt"))
            .lastResponseCode(getInteger(map, "lastResponseCode"))
            .failureCount(getLong(map, "failureCount"))
            .successCount(getLong(map, "successCount"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .build();
    }

    private WebhookTestResultDTO mapToWebhookTestResultDTO(Map<String, Object> map) {
        return WebhookTestResultDTO.builder()
            .success((Boolean) map.get("success"))
            .statusCode(getInteger(map, "statusCode"))
            .responseBody((String) map.get("responseBody"))
            .responseTimeMs(getLong(map, "responseTimeMs"))
            .errorMessage((String) map.get("errorMessage"))
            .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
}
