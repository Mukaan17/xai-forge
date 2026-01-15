package com.xaiforge.common.dto;

import java.time.LocalDateTime;

/**
 * DTO for session information.
 */
public record SessionDto(
    Long id,
    String deviceInfo,
    String ipAddress,
    String location,
    LocalDateTime lastActiveAt,
    LocalDateTime createdAt,
    boolean isCurrentSession
) {}
