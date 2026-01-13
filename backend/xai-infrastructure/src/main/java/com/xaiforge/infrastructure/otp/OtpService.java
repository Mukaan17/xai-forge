package com.xaiforge.infrastructure.otp;

import com.xaiforge.infrastructure.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {
    
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(15);
    private static final String OTP_KEY_PREFIX = "otp:password-reset:";
    private static final String OTP_VERIFIED_PREFIX = "otp:verified:";
    
    private final CacheService cacheService;
    private final SecureRandom random;
    
    public OtpService(CacheService cacheService) {
        this.cacheService = cacheService;
        this.random = new SecureRandom();
    }
    
    /**
     * Generates a 6-digit OTP code
     */
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // Generates number between 100000 and 999999
        return String.valueOf(otp);
    }
    
    /**
     * Stores OTP in Redis with expiration
     */
    public void storeOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email.toLowerCase();
        cacheService.set(key, otp, OTP_EXPIRATION);
        log.debug("OTP stored for email: {}", email);
    }
    
    /**
     * Verifies OTP code for the given email
     */
    public boolean verifyOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email.toLowerCase();
        return cacheService.get(key, String.class)
            .map(storedOtp -> {
                boolean isValid = storedOtp.equals(otp);
                if (isValid) {
                    // Mark OTP as verified (for use in password reset)
                    String verifiedKey = OTP_VERIFIED_PREFIX + email.toLowerCase();
                    cacheService.set(verifiedKey, "true", Duration.ofMinutes(10));
                    // Remove the OTP after verification
                    cacheService.evict(key);
                    log.debug("OTP verified successfully for email: {}", email);
                } else {
                    log.warn("Invalid OTP attempt for email: {}", email);
                }
                return isValid;
            })
            .orElse(false);
    }
    
    /**
     * Checks if OTP has been verified for the given email
     */
    public boolean isOtpVerified(String email) {
        String key = OTP_VERIFIED_PREFIX + email.toLowerCase();
        return cacheService.get(key, String.class)
            .map("true"::equals)
            .orElse(false);
    }
    
    /**
     * Invalidates OTP and verification status for the given email
     */
    public void invalidateOtp(String email) {
        String otpKey = OTP_KEY_PREFIX + email.toLowerCase();
        String verifiedKey = OTP_VERIFIED_PREFIX + email.toLowerCase();
        cacheService.evict(otpKey);
        cacheService.evict(verifiedKey);
        log.debug("OTP invalidated for email: {}", email);
    }
}
