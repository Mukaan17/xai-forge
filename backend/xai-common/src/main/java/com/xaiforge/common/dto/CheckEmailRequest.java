package com.xaiforge.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email
) {}
