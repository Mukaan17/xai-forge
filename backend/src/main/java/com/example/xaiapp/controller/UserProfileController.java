package com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.*;
import com.example.xaiapp.dto.response.*;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(@CurrentUser UserPrincipal currentUser) {
        Map<String, Object> profileMap = userProfileService.getUserProfile(currentUser.getId());
        UserProfileDTO profile = mapToUserProfileDTO(profileMap);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        Map<String, String> requestMap = new HashMap<>();
        if (request.getFirstName() != null) requestMap.put("firstName", request.getFirstName());
        if (request.getLastName() != null) requestMap.put("lastName", request.getLastName());
        if (request.getOrganization() != null) requestMap.put("organization", request.getOrganization());
        if (request.getRole() != null) requestMap.put("role", request.getRole());
        if (request.getLocation() != null) requestMap.put("location", request.getLocation());
        if (request.getBio() != null) requestMap.put("bio", request.getBio());
        
        Map<String, Object> profileMap = userProfileService.updateProfile(currentUser.getId(), requestMap);
        UserProfileDTO profile = mapToUserProfileDTO(profileMap);
        return ResponseEntity.ok(profile);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userProfileService.uploadAvatar(currentUser.getId(), file);
        return ResponseEntity.ok(new AvatarUploadResponse(avatarUrl));
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Delete profile avatar")
    public ResponseEntity<Void> deleteAvatar(@CurrentUser UserPrincipal currentUser) {
        userProfileService.deleteAvatar(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/statistics")
    @Operation(summary = "Get user statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(@CurrentUser UserPrincipal currentUser) {
        Map<String, Object> statsMap = userProfileService.getUserStatistics(currentUser.getId());
        UserStatisticsDTO stats = mapToUserStatisticsDTO(statsMap);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<MessageResponse> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(currentUser.getId(), request.getCurrentPassword(),
            request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PostMapping("/me/2fa/enable")
    @Operation(summary = "Enable 2FA - returns QR code and backup codes")
    public ResponseEntity<TwoFactorSetupDTO> enable2FA(@CurrentUser UserPrincipal currentUser) {
        Map<String, Object> setupMap = userProfileService.enable2FA(currentUser.getId());
        TwoFactorSetupDTO setup = TwoFactorSetupDTO.builder()
            .secret((String) setupMap.get("secret"))
            .qrCodeDataUri((String) setupMap.get("qrCodeDataUri"))
            .backupCodes((java.util.List<String>) setupMap.get("backupCodes"))
            .build();
        return ResponseEntity.ok(setup);
    }

    @PostMapping("/me/2fa/verify")
    @Operation(summary = "Verify 2FA code and activate")
    public ResponseEntity<TwoFactorVerifyResponse> verify2FA(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        boolean valid = userProfileService.verify2FA(currentUser.getId(), request.getCode());
        return ResponseEntity.ok(new TwoFactorVerifyResponse(valid,
            valid ? "2FA enabled successfully" : "Invalid verification code"));
    }

    @DeleteMapping("/me/2fa")
    @Operation(summary = "Disable 2FA")
    public ResponseEntity<MessageResponse> disable2FA(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody TwoFactorVerifyRequest request) {
        userProfileService.disable2FA(currentUser.getId(), request.getCode());
        return ResponseEntity.ok(new MessageResponse("2FA disabled successfully"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete user account")
    public ResponseEntity<MessageResponse> deleteAccount(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody DeleteAccountRequest request) {
        userProfileService.deleteAccount(currentUser.getId(), request.getPassword());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }

    // Helper methods to convert Maps to DTOs
    private UserProfileDTO mapToUserProfileDTO(Map<String, Object> map) {
        return UserProfileDTO.builder()
            .id(getLong(map, "id"))
            .email((String) map.get("email"))
            .firstName((String) map.get("firstName"))
            .lastName((String) map.get("lastName"))
            .fullName((String) map.get("fullName"))
            .organization((String) map.get("organization"))
            .role((String) map.get("role"))
            .location((String) map.get("location"))
            .bio((String) map.get("bio"))
            .profileImageUrl((String) map.get("profileImageUrl"))
            .emailVerified((Boolean) map.get("emailVerified"))
            .twoFactorEnabled((Boolean) map.get("twoFactorEnabled"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .lastLoginAt((java.time.LocalDateTime) map.get("lastLoginAt"))
            .build();
    }

    private UserStatisticsDTO mapToUserStatisticsDTO(Map<String, Object> map) {
        return UserStatisticsDTO.builder()
            .totalDatasets(getLong(map, "totalDatasets"))
            .totalModels(getLong(map, "totalModels"))
            .totalPredictions(getLong(map, "totalPredictions"))
            .averageModelAccuracy(getDouble(map, "averageModelAccuracy"))
            .accountCreatedAt((java.time.LocalDateTime) map.get("accountCreatedAt"))
            .lastLoginAt((java.time.LocalDateTime) map.get("lastLoginAt"))
            .activeApiKeys(getLong(map, "activeApiKeys"))
            .activeSessions(getLong(map, "activeSessions"))
            .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return null;
    }
}
