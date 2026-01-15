package com.xaiforge.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token and user information")
public record AuthResponse(
    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "User information")
    UserDto user
) {
    @Schema(description = "User information")
    public record UserDto(
        @Schema(description = "User ID", example = "1")
        Long id,
        
        @Schema(description = "Username", example = "johndoe")
        String username,
        
        @Schema(description = "Email address", example = "john.doe@example.com")
        String email,
        
        @Schema(description = "First name", example = "John")
        String firstName,
        
        @Schema(description = "Last name", example = "Doe")
        String lastName,
        
        @Schema(description = "Whether 2FA is enabled", example = "false")
        boolean twoFactorEnabled
    ) {}
}

