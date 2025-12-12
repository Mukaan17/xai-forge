package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response for API key creation.
 * Includes the full key which is shown ONLY ONCE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponseDTO {
    private Long id;
    private String name;
    private String key; // Full key - shown only on creation!
    private String keyPreview;
    private String environment;
    private Set<String> permissions;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
