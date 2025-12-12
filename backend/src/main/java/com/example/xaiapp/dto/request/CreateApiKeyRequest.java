package com.example.xaiapp.dto.request;

import com.example.xaiapp.entity.ApiKey.ApiKeyEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateApiKeyRequest {

    @NotBlank(message = "Key name is required")
    @Size(max = 100, message = "Key name must be at most 100 characters")
    private String name;

    @NotNull(message = "Environment is required")
    private ApiKeyEnvironment environment;

    @NotEmpty(message = "At least one permission is required")
    private Set<String> permissions;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private LocalDateTime expiresAt;
}
