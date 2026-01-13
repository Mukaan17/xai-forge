package com.xaiforge.common.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
    String firstName,
    String lastName,
    @Email(message = "Email must be valid")
    String email,
    String organization,
    String role
) {}
