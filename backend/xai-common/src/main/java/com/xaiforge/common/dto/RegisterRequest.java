package com.xaiforge.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequest(
    @Schema(description = "Unique username (3-50 characters)", example = "johndoe", required = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Schema(description = "Valid email address", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    @Schema(description = "Password (minimum 6 characters)", example = "SecurePass123!", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password
) {}

