package com.example.xaiapp.service;

import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.entity.MLModel;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.FileStorageException;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.exception.ValidationException;
import com.example.xaiapp.repository.*;
import com.example.xaiapp.security.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for user profile management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final DatasetRepository datasetRepository;
    private final MLModelRepository modelRepository;
    private final PredictionRepository predictionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final TwoFactorAuthService twoFactorAuthService;

    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_AVATAR_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * Get user profile by ID.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfile(Long userId) {
        User user = findUserById(userId);
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("fullName", user.getFullName());
        profile.put("organization", user.getOrganization());
        profile.put("role", user.getRole());
        profile.put("location", user.getLocation());
        profile.put("bio", user.getBio());
        profile.put("profileImageUrl", user.getProfileImageUrl());
        profile.put("emailVerified", user.getEmailVerified());
        profile.put("twoFactorEnabled", user.getTwoFactorEnabled());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLoginAt", user.getLastLoginAt());
        return profile;
    }

    /**
     * Update user profile.
     */
    public Map<String, Object> updateProfile(Long userId, Map<String, String> request) {
        User user = findUserById(userId);

        if (request.containsKey("firstName")) {
            user.setFirstName(request.get("firstName").trim());
        }
        if (request.containsKey("lastName")) {
            user.setLastName(request.get("lastName").trim());
        }
        if (request.containsKey("organization")) {
            user.setOrganization(request.get("organization").trim());
        }
        if (request.containsKey("role")) {
            user.setRole(request.get("role").trim());
        }
        if (request.containsKey("location")) {
            user.setLocation(request.get("location").trim());
        }
        if (request.containsKey("bio")) {
            user.setBio(request.get("bio").trim());
        }

        User savedUser = userRepository.save(user);
        
        activityLogService.logActivity(
            userId, ActivityLog.ActionType.PROFILE_UPDATED, "USER", userId,
            user.getEmail(), "Profile updated", null);

        log.info("User profile updated: userId={}", userId);
        return getUserProfile(userId);
    }

    /**
     * Upload user avatar image.
     */
    public String uploadAvatar(Long userId, MultipartFile file) {
        validateAvatarFile(file);
        User user = findUserById(userId);

        if (user.getProfileImageUrl() != null) {
            deleteAvatarFile(user.getProfileImageUrl());
        }

        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path uploadPath = Paths.get(AVATAR_UPLOAD_DIR, userId.toString());

        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "/api/users/" + userId + "/avatar/" + filename;
            user.setProfileImageUrl(avatarUrl);
            userRepository.save(user);

            log.info("Avatar uploaded: userId={}, filename={}", userId, filename);
            return avatarUrl;
        } catch (IOException e) {
            log.error("Failed to upload avatar: userId={}", userId, e);
            throw new FileStorageException("Failed to upload avatar image", e);
        }
    }

    /**
     * Delete user avatar.
     */
    public void deleteAvatar(Long userId) {
        User user = findUserById(userId);
        if (user.getProfileImageUrl() != null) {
            deleteAvatarFile(user.getProfileImageUrl());
            user.setProfileImageUrl(null);
            userRepository.save(user);
            log.info("Avatar deleted: userId={}", userId);
        }
    }

    /**
     * Get user statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        findUserById(userId);

        long datasetCount = datasetRepository.countByUserIdAndDeletedFalse(userId);
        long modelCount = modelRepository.countByUserIdAndStatusNot(userId, MLModel.ModelStatus.ARCHIVED);
        long predictionCount = predictionRepository.countByUserId(userId);
        Double avgAccuracy = modelRepository.getAverageAccuracyByUserId(userId);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentPredictions = predictionRepository.countByUserIdSince(userId, thirtyDaysAgo);
        long recentModels = modelRepository.countByUserIdAndCreatedAtAfter(userId, thirtyDaysAgo);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDatasets", datasetCount);
        stats.put("totalModels", modelCount);
        stats.put("totalPredictions", predictionCount);
        stats.put("averageModelAccuracy", avgAccuracy != null ? avgAccuracy : 0.0);
        stats.put("predictionsLast30Days", recentPredictions);
        stats.put("modelsTrainedLast30Days", recentModels);
        return stats;
    }

    /**
     * Change user password.
     */
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        validateNewPassword(newPassword, confirmPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        activityLogService.logActivity(
            userId, ActivityLog.ActionType.PASSWORD_CHANGED, "USER", userId,
            user.getEmail(), "Password changed", null);

        log.info("Password changed: userId={}", userId);
    }

    /**
     * Enable two-factor authentication.
     */
    public Map<String, Object> enable2FA(Long userId) {
        User user = findUserById(userId);
        if (user.getTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is already enabled");
        }

        String secret = twoFactorAuthService.generateSecret();
        String qrCodeDataUri = twoFactorAuthService.generateQRCodeDataUri(secret, user.getEmail());
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();

        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("secret", secret);
        result.put("qrCodeDataUri", qrCodeDataUri);
        result.put("backupCodes", backupCodes);
        return result;
    }

    /**
     * Verify and activate 2FA.
     */
    public boolean verify2FA(Long userId, String code) {
        User user = findUserById(userId);
        if (user.getTwoFactorSecret() == null) {
            throw new ValidationException("Two-factor authentication setup not initiated");
        }

        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        if (valid) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            activityLogService.logActivity(
                userId, ActivityLog.ActionType.TWO_FACTOR_ENABLED, "USER", userId,
                user.getEmail(), "Two-factor authentication enabled", null);
            log.info("2FA enabled: userId={}", userId);
        }
        return valid;
    }

    /**
     * Disable two-factor authentication.
     */
    public void disable2FA(Long userId, String code) {
        User user = findUserById(userId);
        if (!user.getTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is not enabled");
        }

        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        if (!valid) {
            throw new ValidationException("Invalid verification code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorBackupCodes(null);
        userRepository.save(user);

        activityLogService.logActivity(
            userId, ActivityLog.ActionType.TWO_FACTOR_DISABLED, "USER", userId,
            user.getEmail(), "Two-factor authentication disabled", null);
        log.info("2FA disabled: userId={}", userId);
    }

    /**
     * Delete user account.
     */
    public void deleteAccount(Long userId, String password) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Password is incorrect");
        }

        log.info("Account deletion initiated: userId={}, email={}", userId, user.getEmail());
        userRepository.delete(user);
        log.info("Account deleted: userId={}", userId);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("No file provided");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed (5MB)");
        }
        String contentType = file.getContentType();
        boolean validType = false;
        for (String allowedType : ALLOWED_AVATAR_TYPES) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            throw new ValidationException("Invalid file type. Allowed: JPEG, PNG, GIF, WebP");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }

    private void deleteAvatarFile(String avatarUrl) {
        try {
            String relativePath = avatarUrl.replace("/api/users/", "").replace("/avatar/", "/");
            Path filePath = Paths.get(AVATAR_UPLOAD_DIR, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete avatar file: {}", avatarUrl, e);
        }
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : newPassword.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new ValidationException(
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        }
    }
}
