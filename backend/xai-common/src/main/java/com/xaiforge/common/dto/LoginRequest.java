package com.xaiforge.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequest(
    @Schema(description = "Username or email", example = "johndoe", required = true)
    @NotBlank(message = "Username is required")
    String username,
    
    @Schema(description = "User password", example = "SecurePass123!", required = true)
    @NotBlank(message = "Password is required")
    String password
) {}

