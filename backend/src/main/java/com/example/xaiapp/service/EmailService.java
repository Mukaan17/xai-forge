package com.example.xaiapp.service;

import com.example.xaiapp.entity.Notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for sending email notifications.
 * Stub implementation - integrate with email service provider in production.
 */
@Service
@Slf4j
public class EmailService {

    /**
     * Send notification email.
     * In production, integrate with SendGrid, AWS SES, or similar.
     */
    public void sendNotificationEmail(String email, NotificationType type, 
                                     String title, String message, 
                                     Map<String, Object> metadata) {
        // Stub implementation - integrate with email service in production
        // For now, just log
        log.debug("Email notification would be sent to: {} - {}", email, title);
    }
}
