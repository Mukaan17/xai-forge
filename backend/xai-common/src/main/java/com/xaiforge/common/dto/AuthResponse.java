package com.xaiforge.common.dto;

public record AuthResponse(
    String token,
    UserDto user
) {
    public record UserDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean twoFactorEnabled
    ) {}
}

