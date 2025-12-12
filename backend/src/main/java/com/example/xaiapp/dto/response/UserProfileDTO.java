package com.example.xaiapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String organization;
    private String role;
    private String location;
    private String bio;
    private String profileImageUrl;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
