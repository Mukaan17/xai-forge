package com.xaiforge.application.service;

import com.xaiforge.domain.notification.entity.Notification;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.domain.user.entity.UserPreferences;
import com.xaiforge.infrastructure.email.EmailService;
import com.xaiforge.infrastructure.persistence.notification.NotificationRepository;
import com.xaiforge.infrastructure.persistence.user.UserPreferencesRepository;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user notifications
 * 
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationApplicationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final EmailService emailService;
    
    /**
     * Create a notification for a user
     */
    public Notification createNotification(
            Long userId,
            Notification.NotificationType type,
            String title,
            String message,
            String detail) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Check user preferences
        UserPreferences preferences = preferencesRepository.findByUserId(userId).orElse(null);
        boolean inAppEnabled = preferences == null || isInAppEnabled(preferences, type);
        
        Notification notification = null;
        
        if (inAppEnabled) {
            notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setDetail(detail);
            notification.setRead(false);
            
            notification = notificationRepository.save(notification);
            log.debug("Notification created: userId={}, type={}, title={}", userId, type, title);
        }
        
        // Send email notification if enabled
        boolean emailEnabled = preferences != null && isEmailEnabled(preferences, type);
        if (emailEnabled && notification != null) {
            sendEmailNotificationAsync(user, type, title, message, detail);
        }
        
        return notification;
    }
    
    /**
     * Get paginated notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
            .forEach(notification -> {
                notification.setRead(true);
                notificationRepository.save(notification);
            });
    }
    
    /**
     * Delete a notification
     */
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }
        
        notificationRepository.delete(notification);
    }
    
    /**
     * Check if in-app notifications are enabled for a notification type
     */
    private boolean isInAppEnabled(UserPreferences preferences, Notification.NotificationType type) {
        // Default to enabled if preferences not set
        if (preferences.getNotificationPreferences() == null || preferences.getNotificationPreferences().isEmpty()) {
            return true;
        }
        
        try {
            // Parse JSON preferences
            // Format: {"TRAINING_COMPLETE": {"inApp": true, "email": false}, ...}
            // For now, default to enabled
            return true;
        } catch (Exception e) {
            log.warn("Error parsing notification preferences: {}", e.getMessage());
            return true; // Default to enabled
        }
    }
    
    /**
     * Check if email notifications are enabled for a notification type
     */
    private boolean isEmailEnabled(UserPreferences preferences, Notification.NotificationType type) {
        // Default to disabled for email
        if (preferences.getNotificationPreferences() == null || preferences.getNotificationPreferences().isEmpty()) {
            return false;
        }
        
        try {
            // Parse JSON preferences
            // For now, default to disabled
            return false;
        } catch (Exception e) {
            log.warn("Error parsing notification preferences: {}", e.getMessage());
            return false; // Default to disabled
        }
    }
    
    /**
     * Send email notification asynchronously
     */
    @Async
    public void sendEmailNotificationAsync(
            User user,
            Notification.NotificationType type,
            String title,
            String message,
            String detail) {
        try {
            String emailBody = buildEmailBody(title, message, detail);
            emailService.sendNotificationEmail(user.getEmail(), title, emailBody);
            log.info("Email notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }
    
    private String buildEmailBody(String title, String message, String detail) {
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append(title).append("\n\n");
        body.append(message).append("\n");
        if (detail != null && !detail.isEmpty()) {
            body.append("\n").append(detail).append("\n");
        }
        body.append("\nBest regards,\nXAI Forge Team");
        return body.toString();
    }
}
