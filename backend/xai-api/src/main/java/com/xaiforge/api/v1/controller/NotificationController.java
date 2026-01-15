package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.NotificationApplicationService;
import com.xaiforge.common.dto.NotificationDto;
import com.xaiforge.common.dto.PaginatedResponse;
import com.xaiforge.domain.notification.entity.Notification;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management operations")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationApplicationService notificationService;
    
    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationService.getNotifications(user.getId(), pageable);
        
        Page<NotificationDto> dtoPage = notificationPage.map(this::toDto);
        
        PaginatedResponse<NotificationDto> response = PaginatedResponse.of(
            dtoPage.getContent(),
            page,
            size,
            notificationPage.getTotalElements()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.deleteNotification(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
    
    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getType().name(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getDetail(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
