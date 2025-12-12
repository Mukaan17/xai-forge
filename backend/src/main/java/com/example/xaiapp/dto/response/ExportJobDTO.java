package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobDTO {
    private Long id;
    private String status;
    private String exportType;
    private String format;
    private Set<String> includeItems;
    private Integer progress;
    private String currentStep;
    private Long fileSizeBytes;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private Integer downloadCount;
}
