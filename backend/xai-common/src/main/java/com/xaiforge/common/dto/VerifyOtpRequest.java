package com.xaiforge.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "\\d{6}", message = "OTP code must be 6 digits")
    String code
) {}
