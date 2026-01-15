package com.xaiforge.common.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    String type,
    String title,
    String message,
    String detail,
    boolean read,
    LocalDateTime createdAt
) {}
