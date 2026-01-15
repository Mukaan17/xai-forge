package com.xaiforge.application.service;

import com.xaiforge.common.dto.TwoFactorSetupDto;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import com.xaiforge.infrastructure.security.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service for two-factor authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TwoFactorApplicationService {
    
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final ActivityLogApplicationService activityLogService;
    
    /**
     * Enable 2FA setup - generates secret, QR code, and backup codes.
     * The user must verify with a TOTP code before 2FA is fully enabled.
     */
    public TwoFactorSetupDto enable2FA(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException(userId));
        
        if (user.isTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is already enabled");
        }
        
        // Generate secret
        String secret = twoFactorAuthService.generateSecret();
        
        // Generate QR code data URI
        String qrCodeDataUri = twoFactorAuthService.generateQRCodeDataUri(secret, user.getEmail());
        
        // Generate backup codes
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();
        String hashedBackupCodes = backupCodes.stream()
            .map(twoFactorAuthService::hashBackupCode)
            .collect(Collectors.joining(","));
        
        // Store secret and backup codes temporarily (will be confirmed on verification)
        user.setTwoFactorSecret(secret);
        user.setTwoFactorBackupCodes(hashedBackupCodes);
        userRepository.save(user);
        
        log.info("2FA setup initiated for user: {}", userId);
        
        return new TwoFactorSetupDto(
            secret,
            qrCodeDataUri,
            backupCodes
        );
    }
    
    /**
     * Verify and activate 2FA with a TOTP code.
     */
    public boolean verify2FA(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException(userId));
        
        if (user.getTwoFactorSecret() == null) {
            throw new ValidationException("Two-factor authentication setup not initiated");
        }
        
        boolean valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        
        if (valid) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            
            // Log activity
            activityLogService.logActivityAsync(
                userId,
                com.xaiforge.domain.activity.entity.ActivityLog.EventType.TWO_FACTOR_ENABLED,
                "Two-factor authentication enabled",
                Map.of("resourceType", "USER", "resourceId", userId.toString(), "resourceName", user.getEmail())
            );
            
            log.info("2FA enabled for user: {}", userId);
        }
        
        return valid;
    }
    
    /**
     * Disable 2FA (requires verification code or backup code).
     */
    public void disable2FA(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException(userId));
        
        if (!user.isTwoFactorEnabled()) {
            throw new ValidationException("Two-factor authentication is not enabled");
        }
        
        // Verify with TOTP code or backup code
        boolean valid = false;
        if (user.getTwoFactorSecret() != null) {
            valid = twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), code);
        }
        
        if (!valid && user.getTwoFactorBackupCodes() != null) {
            valid = twoFactorAuthService.verifyBackupCode(code, user.getTwoFactorBackupCodes());
        }
        
        if (!valid) {
            throw new ValidationException("Invalid verification code");
        }
        
        // Disable 2FA
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorBackupCodes(null);
        userRepository.save(user);
        
        // Log activity
        activityLogService.logActivityAsync(
            userId,
            com.xaiforge.domain.activity.entity.ActivityLog.EventType.TWO_FACTOR_DISABLED,
            "Two-factor authentication disabled",
            Map.of("resourceType", "USER", "resourceId", userId.toString(), "resourceName", user.getEmail())
        );
        
        log.info("2FA disabled for user: {}", userId);
    }
}
