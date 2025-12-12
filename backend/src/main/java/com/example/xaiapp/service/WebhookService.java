package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.Webhook;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for webhook management and delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;

    private static final int SECRET_LENGTH = 32;
    private static final int MAX_WEBHOOKS_PER_USER = 10;
    private static final int MAX_RETRIES = 3;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create a new webhook.
     */
    public Map<String, Object> createWebhook(Long userId, String name, String url, Set<String> events, String description) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long existingCount = webhookRepository.countByUserId(userId);
        if (existingCount >= MAX_WEBHOOKS_PER_USER) {
            throw new ValidationException("Maximum webhooks limit reached (" + MAX_WEBHOOKS_PER_USER + ")");
        }

        validateWebhookUrl(url);
        validateEvents(events);

        String secret = generateSecret();

        Webhook webhook = Webhook.builder()
            .user(user)
            .name(name)
            .url(url)
            .secret(secret)
            .events(events)
            .description(description)
            .active(true)
            .build();

        webhook = webhookRepository.save(webhook);

        activityLogService.logActivity(userId, ActivityLog.ActionType.WEBHOOK_CREATED, "WEBHOOK",
            webhook.getId(), webhook.getName(), "Webhook created: " + webhook.getName(),
            Map.of("events", events));

        log.info("Webhook created: userId={}, webhookId={}, url={}", userId, webhook.getId(), webhook.getUrl());

        Map<String, Object> dto = mapToDTO(webhook);
        dto.put("secret", secret); // Only included on creation
        return dto;
    }

    /**
     * Get all webhooks for a user.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWebhooks(Long userId) {
        return webhookRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update a webhook.
     */
    public Map<String, Object> updateWebhook(Long userId, Long webhookId, String name, String url,
                                             Set<String> events, String description, Boolean active) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        if (name != null) webhook.setName(name);
        if (url != null) {
            validateWebhookUrl(url);
            webhook.setUrl(url);
        }
        if (events != null) {
            validateEvents(events);
            webhook.setEvents(events);
        }
        if (description != null) webhook.setDescription(description);
        if (active != null) {
            webhook.setActive(active);
            if (active) {
                webhook.setAutoDisabled(false);
                webhook.setAutoDisabledAt(null);
                webhook.setFailureCount(0);
            }
        }

        webhook = webhookRepository.save(webhook);
        log.info("Webhook updated: webhookId={}", webhookId);
        return mapToDTO(webhook);
    }

    /**
     * Delete a webhook.
     */
    public void deleteWebhook(Long userId, Long webhookId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));
        webhookRepository.delete(webhook);

        activityLogService.logActivity(userId, ActivityLog.ActionType.WEBHOOK_DELETED, "WEBHOOK",
            webhookId, webhook.getName(), "Webhook deleted: " + webhook.getName(), null);

        log.info("Webhook deleted: webhookId={}", webhookId);
    }

    /**
     * Test a webhook by sending a test payload.
     */
    public Map<String, Object> testWebhook(Long userId, Long webhookId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        Map<String, Object> payload = Map.of(
            "event", "webhook.test",
            "timestamp", LocalDateTime.now().toString(),
            "data", Map.of("message", "This is a test webhook delivery from XAI-Forge", "webhookId", webhookId)
        );

        WebhookDeliveryResult result = deliverWebhook(webhook, payload);

        Map<String, Object> testResult = new HashMap<>();
        testResult.put("success", result.success);
        testResult.put("statusCode", result.statusCode);
        testResult.put("responseBody", truncate(result.responseBody, 500));
        testResult.put("responseTimeMs", result.responseTimeMs);
        testResult.put("errorMessage", result.errorMessage);
        return testResult;
    }

    /**
     * Trigger webhooks for a specific event.
     */
    @Async
    public void triggerWebhooks(String event, Map<String, Object> data) {
        List<Webhook> webhooks = webhookRepository.findActiveWebhooksForEvent(event);
        for (Webhook webhook : webhooks) {
            Map<String, Object> payload = Map.of(
                "event", event,
                "timestamp", LocalDateTime.now().toString(),
                "data", data
            );
            deliverWithRetry(webhook, payload);
        }
    }

    /**
     * Trigger webhooks for a specific user and event.
     */
    @Async
    public void triggerUserWebhooks(Long userId, String event, Map<String, Object> data) {
        List<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(userId).stream()
            .filter(w -> w.getEvents().contains(event))
            .collect(Collectors.toList());

        for (Webhook webhook : webhooks) {
            Map<String, Object> payload = Map.of(
                "event", event,
                "timestamp", LocalDateTime.now().toString(),
                "data", data
            );
            deliverWithRetry(webhook, payload);
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void deliverWithRetry(Webhook webhook, Map<String, Object> payload) {
        int attempts = 0;
        WebhookDeliveryResult result = null;

        while (attempts < MAX_RETRIES) {
            attempts++;
            result = deliverWebhook(webhook, payload);

            if (result.success) {
                webhook.recordSuccess(result.statusCode, result.responseBody);
                webhookRepository.save(webhook);
                log.debug("Webhook delivered: webhookId={}, event={}", webhook.getId(), payload.get("event"));
                return;
            }

            if (attempts < MAX_RETRIES) {
                try {
                    Thread.sleep((long) Math.pow(2, attempts) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (result != null) {
            webhook.recordFailure(result.statusCode, result.errorMessage);
            webhookRepository.save(webhook);
            log.warn("Webhook delivery failed after {} attempts: webhookId={}, error={}", 
                attempts, webhook.getId(), result.errorMessage);
        }
    }

    private WebhookDeliveryResult deliverWebhook(Webhook webhook, Map<String, Object> payload) {
        long startTime = System.currentTimeMillis();

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            String signature = generateSignature(payloadJson, webhook.getSecret());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-XAI-Signature", "sha256=" + signature);
            headers.set("X-XAI-Event", (String) payload.get("event"));
            headers.set("X-XAI-Delivery", UUID.randomUUID().toString());

            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                URI.create(webhook.getUrl()), HttpMethod.POST, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            return new WebhookDeliveryResult(
                response.getStatusCode().is2xxSuccessful(),
                response.getStatusCode().value(),
                response.getBody(),
                responseTime,
                null
            );
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.debug("Webhook delivery error: webhookId={}, error={}", webhook.getId(), e.getMessage());
            return new WebhookDeliveryResult(false, 0, null, responseTime, e.getMessage());
        }
    }

    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateSecret() {
        byte[] bytes = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validateWebhookUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new ValidationException("Webhook URL is required");
        }
        if (!url.startsWith("https://") && !url.startsWith("http://localhost")) {
            throw new ValidationException("Webhook URL must use HTTPS (except localhost for testing)");
        }
        try {
            new URI(url);
        } catch (Exception e) {
            throw new ValidationException("Invalid webhook URL");
        }
    }

    private void validateEvents(Set<String> events) {
        if (events == null || events.isEmpty()) {
            throw new ValidationException("At least one event must be selected");
        }
        for (String event : events) {
            if (!Webhook.Events.ALL.contains(event)) {
                throw new ValidationException("Invalid event type: " + event);
            }
        }
    }

    private Map<String, Object> mapToDTO(Webhook webhook) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", webhook.getId());
        dto.put("name", webhook.getName());
        dto.put("url", webhook.getUrl());
        dto.put("events", webhook.getEvents());
        dto.put("description", webhook.getDescription());
        dto.put("active", webhook.getActive());
        dto.put("autoDisabled", webhook.getAutoDisabled());
        dto.put("lastTriggeredAt", webhook.getLastTriggeredAt());
        dto.put("lastResponseCode", webhook.getLastResponseCode());
        dto.put("failureCount", webhook.getFailureCount());
        dto.put("successCount", webhook.getSuccessCount());
        dto.put("createdAt", webhook.getCreatedAt());
        return dto;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }

    private record WebhookDeliveryResult(
        boolean success, int statusCode, String responseBody, long responseTimeMs, String errorMessage
    ) {}
}
