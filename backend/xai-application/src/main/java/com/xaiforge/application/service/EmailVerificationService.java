package com.xaiforge.application.service;

import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.email.EmailService;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for email verification functionality.
 * Handles generation of verification tokens and sending verification emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailVerificationService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    private static final int VERIFICATION_TOKEN_EXPIRY_MINUTES = 24 * 60; // 24 hours
    
    /**
     * Generate and send verification email for a user.
     */
    public void sendVerificationEmail(User user) {
        try {
            // Generate verification token
            String token = generateVerificationToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_EXPIRY_MINUTES);
            
            // Save token to user
            user.setEmailVerificationToken(token);
            user.setEmailVerificationExpires(expiresAt);
            userRepository.save(user);
            
            // Send verification email
            String verificationUrl = buildVerificationUrl(token);
            emailService.sendVerificationEmail(user.getEmail(), verificationUrl, user.getUsername());
            
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    /**
     * Verify email using token.
     */
    public boolean verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElse(null);
        
        if (user == null) {
            log.warn("Invalid verification token: {}", token);
            return false;
        }
        
        // Check if token expired
        if (user.getEmailVerificationExpires() != null && 
            user.getEmailVerificationExpires().isBefore(LocalDateTime.now())) {
            log.warn("Verification token expired for user: {}", user.getEmail());
            // Clear expired token
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpires(null);
            userRepository.save(user);
            return false;
        }
        
        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);
        userRepository.save(user);
        
        log.info("Email verified for user: {}", user.getEmail());
        return true;
    }
    
    /**
     * Resend verification email.
     */
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException("User not found with email: " + email));
        
        if (user.isEmailVerified()) {
            throw new com.xaiforge.common.exception.ValidationException("email", "Email is already verified");
        }
        
        sendVerificationEmail(user);
    }
    
    /**
     * Generate a secure verification token.
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Build verification URL (frontend URL + token).
     */
    private String buildVerificationUrl(String token) {
        // In production, this should come from configuration
        String baseUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:5173");
        return baseUrl + "/verify-email?token=" + token;
    }
}
