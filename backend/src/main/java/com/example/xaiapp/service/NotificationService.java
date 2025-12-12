package com.example.xaiapp.service;

import com.example.xaiapp.entity.Notification;
import com.example.xaiapp.entity.Notification.NotificationPriority;
import com.example.xaiapp.entity.Notification.NotificationType;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.UserPreferences;
import com.example.xaiapp.exception.ResourceNotFoundException;
import com.example.xaiapp.repository.NotificationRepository;
import com.example.xaiapp.repository.UserPreferencesRepository;
import com.example.xaiapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing user notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final EmailService emailService;

    /**
     * Create a new notification for a user.
     */
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata) {
        
        return createNotification(userId, type, title, message, metadata, 
            NotificationPriority.NORMAL, null, null);
    }

    /**
     * Create a notification with full options.
     */
    public Notification createNotification(
            Long userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            NotificationPriority priority,
            String actionUrl,
            String actionLabel) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        UserPreferences prefs = preferencesRepository.findByUserId(userId).orElse(null);
        boolean inAppEnabled = prefs == null || prefs.isInAppEnabledFor(type.name());

        Notification notification = null;

        if (inAppEnabled) {
            notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .priority(priority)
                .actionUrl(actionUrl)
                .actionLabel(actionLabel)
                .isRead(false)
                .build();

            notification = notificationRepository.save(notification);
            log.debug("Notification created: userId={}, type={}, title={}", userId, type, title);
        }

        // Check if email notification should be sent
        boolean emailEnabled = prefs != null && prefs.isEmailEnabledFor(type.name());
        boolean inQuietHours = prefs != null && prefs.isInQuietHours();

        if (emailEnabled && !inQuietHours && priority != NotificationPriority.LOW) {
            sendEmailNotificationAsync(user, type, title, message, metadata);
        }

        return notification;
    }

    /**
     * Get paginated notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get unread notification count for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark a notification as read.
     */
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.markAsRead();
        notificationRepository.save(notification);
        log.debug("Notification marked as read: id={}", notificationId);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
        log.debug("All notifications marked as read: userId={}", userId);
    }

    /**
     * Delete a notification.
     */
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
        log.debug("Notification deleted: id={}", notificationId);
    }

    /**
     * Create notification for model training completion.
     */
    public void notifyModelTrained(Long userId, Long modelId, String modelName, Double accuracy) {
        createNotification(userId, NotificationType.MODEL_TRAINED, "Model Training Complete",
            String.format("\"%s\" finished training with %.1f%% accuracy", modelName, accuracy * 100),
            Map.of("modelId", modelId, "modelName", modelName, "accuracy", accuracy),
            NotificationPriority.NORMAL, "/models/" + modelId, "View Model");
    }

    /**
     * Create notification for model training failure.
     */
    public void notifyModelFailed(Long userId, Long modelId, String modelName, String error) {
        createNotification(userId, NotificationType.MODEL_FAILED, "Training Failed",
            String.format("\"%s\" encountered an error: %s", modelName, truncate(error, 100)),
            Map.of("modelId", modelId, "modelName", modelName, "error", error),
            NotificationPriority.HIGH, "/models/" + modelId, "View Details");
    }

    /**
     * Create notification for dataset upload completion.
     */
    public void notifyDatasetUploaded(Long userId, Long datasetId, String datasetName, 
                                       int rowCount, int columnCount) {
        createNotification(userId, NotificationType.DATASET_UPLOADED, "Dataset Upload Complete",
            String.format("\"%s\" processed successfully (%d rows, %d features)", datasetName, rowCount, columnCount),
            Map.of("datasetId", datasetId, "datasetName", datasetName, "rowCount", rowCount, "columnCount", columnCount),
            NotificationPriority.NORMAL, "/datasets/" + datasetId, "View Dataset");
    }

    /**
     * Create security alert notification.
     */
    public void notifySecurityAlert(Long userId, String eventType, String description, 
                                     Map<String, Object> details) {
        createNotification(userId, NotificationType.SECURITY_ALERT, "Security Alert", description,
            Map.of("eventType", eventType, "details", details),
            NotificationPriority.HIGH, "/settings/security", "Review Activity");
    }

    /**
     * Create export ready notification.
     */
    public void notifyExportReady(Long userId, Long exportJobId, String exportType) {
        createNotification(userId, NotificationType.EXPORT_READY, "Export Ready",
            String.format("Your %s export is ready for download", exportType.toLowerCase()),
            Map.of("exportJobId", exportJobId, "exportType", exportType),
            NotificationPriority.NORMAL, "/settings/export/" + exportJobId, "Download Export");
    }

    @Async
    protected void sendEmailNotificationAsync(User user, NotificationType type, 
                                              String title, String message, 
                                              Map<String, Object> metadata) {
        try {
            emailService.sendNotificationEmail(user.getEmail(), type, title, message, metadata);
            log.debug("Email notification sent: userId={}, type={}", user.getId(), type);
        } catch (Exception e) {
            log.error("Failed to send email notification: userId={}, type={}", user.getId(), type, e);
        }
    }


    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}
