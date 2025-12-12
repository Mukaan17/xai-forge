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
public class ApiKeyDTO {
    private Long id;
    private String name;
    private String keyPreview; // e.g., "xai_live_sk_...a1b2"
    private String environment;
    private Set<String> permissions;
    private Boolean active;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
