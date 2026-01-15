package com.xaiforge.infrastructure.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Service for two-factor authentication using TOTP (Time-based One-Time Password).
 * Uses Google Authenticator library for TOTP generation and verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {
    
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.2fa.issuer:XAI-Forge}")
    private String issuer;
    
    /**
     * Generate a new TOTP secret for a user.
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }
    
    /**
     * Generate QR code data URI (otpauth:// URL) for authenticator app setup.
     * The frontend can use this URL with a QR code library to display the QR code.
     */
    public String generateQRCodeDataUri(String secret, String email) {
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, email, key);
        return otpAuthUrl;
    }
    
    /**
     * Verify a TOTP code against a secret.
     */
    public boolean verifyCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            log.warn("Invalid TOTP code format: {}", code);
            return false;
        }
    }
    
    /**
     * Generate backup codes for account recovery.
     * Returns a list of 10 backup codes, each 8 characters long.
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            byte[] randomBytes = new byte[6];
            secureRandom.nextBytes(randomBytes);
            String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            codes.add(code.substring(0, 8).toUpperCase());
        }
        return codes;
    }
    
    /**
     * Hash a backup code for storage (simple hash for now, can be enhanced with BCrypt).
     */
    public String hashBackupCode(String code) {
        // Simple hash - in production, use BCrypt or similar
        return String.valueOf(code.hashCode());
    }
    
    /**
     * Verify a backup code against stored hashed codes.
     */
    public boolean verifyBackupCode(String code, String storedHashedCodes) {
        if (storedHashedCodes == null || storedHashedCodes.isEmpty()) {
            return false;
        }
        String hashedCode = hashBackupCode(code);
        String[] codes = storedHashedCodes.split(",");
        for (String storedCode : codes) {
            if (storedCode.equals(hashedCode)) {
                return true;
            }
        }
        return false;
    }
}
