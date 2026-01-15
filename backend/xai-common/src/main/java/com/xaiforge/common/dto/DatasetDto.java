package com.xaiforge.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Dataset information")
public record DatasetDto(
    @Schema(description = "Dataset ID", example = "1")
    Long id,
    
    @Schema(description = "Original filename", example = "customer-churn.csv")
    String fileName,
    
    @Schema(description = "Upload timestamp", example = "2025-01-15T10:30:00")
    LocalDateTime uploadDate,
    
    @Schema(description = "Column headers", example = "[\"age\", \"tenure\", \"churn\"]")
    List<String> headers,
    
    @Schema(description = "Number of rows", example = "1000")
    Long rowCount,
    
    @Schema(description = "Owner user ID", example = "1")
    Long ownerId
) {}

