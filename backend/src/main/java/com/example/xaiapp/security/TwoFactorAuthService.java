package com.example.xaiapp.security;

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
 * Service for two-factor authentication using TOTP.
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
     * Generate a new TOTP secret.
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Generate QR code data URI for authenticator app setup.
     */
    public String generateQRCodeDataUri(String secret, String email) {
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            issuer, email, new GoogleAuthenticatorKey.Builder(secret).build());
        
        // In production, generate actual QR code image
        // For now, return the URL that can be used with a QR code library on frontend
        return otpAuthUrl;
    }

    /**
     * Verify a TOTP code.
     */
    public boolean verifyCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Generate backup codes for account recovery.
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            byte[] bytes = new byte[5];
            secureRandom.nextBytes(bytes);
            String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                .substring(0, 8).toUpperCase();
            codes.add(code);
        }
        return codes;
    }

    /**
     * Hash backup codes for storage.
     */
    public String hashBackupCodes(List<String> codes) {
        // In production, hash each code individually
        return String.join(",", codes);
    }

    /**
     * Verify a backup code.
     */
    public boolean verifyBackupCode(String storedCodes, String providedCode) {
        if (storedCodes == null || providedCode == null) return false;
        String[] codes = storedCodes.split(",");
        for (String code : codes) {
            if (code.equalsIgnoreCase(providedCode.trim())) {
                return true;
            }
        }
        return false;
    }
}
