package com.xaiforge.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DatasetDto(
    Long id,
    String fileName,
    LocalDateTime uploadDate,
    List<String> headers,
    Long rowCount,
    Long ownerId
) {}

