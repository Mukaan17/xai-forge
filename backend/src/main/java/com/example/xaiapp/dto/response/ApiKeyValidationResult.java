package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Result of API key validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyValidationResult {
    private Long userId;
    private Long keyId;
    private Set<String> permissions;
    private String environment;
}
