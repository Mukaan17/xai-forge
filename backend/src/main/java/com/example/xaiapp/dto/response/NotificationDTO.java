package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String priority;
    private String actionUrl;
    private String actionLabel;
    private LocalDateTime createdAt;
}
