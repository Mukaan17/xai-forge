package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.ApiKey;
import com.example.xaiapp.entity.ApiKey.ApiKeyEnvironment;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.ApiKeyRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for API key management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    private static final int KEY_LENGTH = 32;
    private static final int MAX_KEYS_PER_USER = 10;
    private static final String KEY_PREFIX_LIVE = "xai_live_sk_";
    private static final String KEY_PREFIX_TEST = "xai_test_sk_";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new API key for a user.
     */
    public Map<String, Object> generateApiKey(Long userId, String name, ApiKeyEnvironment environment,
                                               Set<String> permissions, String description, LocalDateTime expiresAt) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long existingKeys = apiKeyRepository.countByUserIdAndActiveTrue(userId);
        if (existingKeys >= MAX_KEYS_PER_USER) {
            throw new ValidationException("Maximum number of API keys reached (" + MAX_KEYS_PER_USER + ")");
        }

        validatePermissions(permissions);

        String prefix = environment == ApiKeyEnvironment.PRODUCTION ? KEY_PREFIX_LIVE : KEY_PREFIX_TEST;
        String randomPart = generateRandomString(KEY_LENGTH);
        String fullKey = prefix + randomPart;
        String keyHash = hashKey(fullKey);
        String keySuffix = randomPart.substring(randomPart.length() - 4);

        ApiKey apiKey = ApiKey.builder()
            .user(user)
            .name(name)
            .keyHash(keyHash)
            .keyPrefix(prefix)
            .keySuffix(keySuffix)
            .environment(environment)
            .permissions(permissions)
            .description(description)
            .expiresAt(expiresAt)
            .build();

        apiKey = apiKeyRepository.save(apiKey);

        activityLogService.logActivity(userId, ActivityLog.ActionType.API_KEY_CREATED, "API_KEY",
            apiKey.getId(), apiKey.getName(), "API key created: " + apiKey.getName(),
            Map.of("environment", environment.name()));

        log.info("API key created: userId={}, keyId={}, name={}", userId, apiKey.getId(), apiKey.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("id", apiKey.getId());
        response.put("name", apiKey.getName());
        response.put("key", fullKey); // Full key - shown only once!
        response.put("keyPreview", prefix + "..." + keySuffix);
        response.put("environment", environment.name());
        response.put("permissions", permissions);
        response.put("expiresAt", expiresAt);
        response.put("createdAt", apiKey.getCreatedAt());
        return response;
    }

    /**
     * Get all API keys for a user (masked).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getApiKeys(Long userId) {
        return apiKeyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Revoke an API key.
     */
    public void revokeApiKey(Long userId, Long keyId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUserId(keyId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);

        activityLogService.logActivity(userId, ActivityLog.ActionType.API_KEY_REVOKED, "API_KEY",
            keyId, apiKey.getName(), "API key revoked: " + apiKey.getName(), null);

        log.info("API key revoked: userId={}, keyId={}", userId, keyId);
    }

    /**
     * Validate an API key and return validation result.
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }

        String keyHash = hashKey(apiKey);
        Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHash(keyHash);

        if (keyOpt.isEmpty() || !keyOpt.get().isValid()) {
            return Optional.empty();
        }

        ApiKey key = keyOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("keyId", key.getId());
        result.put("userId", key.getUser().getId());
        result.put("permissions", key.getPermissions());
        result.put("environment", key.getEnvironment());
        return Optional.of(result);
    }

    /**
     * Update last used timestamp for an API key.
     */
    public void updateLastUsed(Long keyId, String ipAddress) {
        apiKeyRepository.updateLastUsed(keyId, LocalDateTime.now(), ipAddress);
    }

    /**
     * Check if an API key has a specific permission.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long keyId, String permission) {
        return apiKeyRepository.findById(keyId)
            .map(key -> key.hasPermission(permission))
            .orElse(false);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void validatePermissions(Set<String> permissions) {
        Set<String> validPermissions = Set.of(
            "datasets:read", "datasets:write", "datasets:delete",
            "models:read", "models:write", "models:delete",
            "predictions:read", "predictions:write", "*"
        );

        for (String permission : permissions) {
            if (!validPermissions.contains(permission)) {
                throw new ValidationException("Invalid permission: " + permission);
            }
        }
    }

    private Map<String, Object> mapToDTO(ApiKey apiKey) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", apiKey.getId());
        dto.put("name", apiKey.getName());
        dto.put("keyPreview", apiKey.getKeyPrefix() + "..." + apiKey.getKeySuffix());
        dto.put("environment", apiKey.getEnvironment().name());
        dto.put("permissions", apiKey.getPermissions());
        dto.put("active", apiKey.getActive());
        dto.put("lastUsedAt", apiKey.getLastUsedAt());
        dto.put("usageCount", apiKey.getUsageCount());
        dto.put("expiresAt", apiKey.getExpiresAt());
        dto.put("createdAt", apiKey.getCreatedAt());
        return dto;
    }
}
