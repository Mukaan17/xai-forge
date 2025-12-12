package com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.entity.Notification;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications")
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Notification> notifications = notificationService.getNotifications(currentUser.getId(), pageable);
        Page<NotificationDTO> dtoPage = notifications.map(this::mapToNotificationDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(currentUser.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Void> deleteNotification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long notificationId) {
        notificationService.deleteNotification(currentUser.getId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    private NotificationDTO mapToNotificationDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .type(notification.getType() != null ? notification.getType().name() : null)
            .title(notification.getTitle())
            .message(notification.getMessage())
            .metadata(notification.getMetadata())
            .isRead(notification.getIsRead())
            .readAt(notification.getReadAt())
            .priority(notification.getPriority() != null ? notification.getPriority().name() : null)
            .actionUrl(notification.getActionUrl())
            .actionLabel(notification.getActionLabel())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
