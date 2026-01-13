package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.UserApplicationService;
import com.xaiforge.common.dto.*;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.email.EmailService;
import com.xaiforge.infrastructure.otp.OtpService;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import com.xaiforge.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserApplicationService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    
    public AuthController(AuthenticationManager authenticationManager, 
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider,
                          UserApplicationService userService,
                          OtpService otpService,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or user already exists")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new ValidationException("username", "Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("email", "Email is already in use");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        
        User savedUser = userRepository.save(user);
        
        // Generate token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            savedUser, null, savedUser.getAuthorities());
        String token = tokenProvider.generateToken(authentication);
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getProfile() != null ? savedUser.getProfile().getFirstName() : null,
            savedUser.getProfile() != null ? savedUser.getProfile().getLastName() : null,
            savedUser.isTwoFactorEnabled()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(token, userDto));
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
            
            AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile() != null ? user.getProfile().getFirstName() : null,
                user.getProfile() != null ? user.getProfile().getLastName() : null,
                user.isTwoFactorEnabled()
            );
            
            return ResponseEntity.ok(new AuthResponse(jwt, userDto));
            
        } catch (BadCredentialsException e) {
            throw new com.xaiforge.common.exception.InvalidCredentialsException();
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<AuthResponse.UserDto> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfile() != null ? user.getProfile().getFirstName() : null,
            user.getProfile() != null ? user.getProfile().getLastName() : null,
            user.isTwoFactorEnabled()
        );
        
        return ResponseEntity.ok(userDto);
    }
    
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<AuthResponse.UserDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            org.springframework.security.core.Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        userService.updateProfile(user.getId(), request);
        
        // Reload user to get updated profile
        User updatedUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException(user.getId()));
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getProfile() != null ? updatedUser.getProfile().getFirstName() : null,
            updatedUser.getProfile() != null ? updatedUser.getProfile().getLastName() : null,
            updatedUser.isTwoFactorEnabled()
        );
        
        return ResponseEntity.ok(userDto);
    }
    
    @PutMapping("/password")
    @Operation(summary = "Change password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            org.springframework.security.core.Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        userService.changePassword(user.getId(), request);
        
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
    
    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request,
            org.springframework.security.core.Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        userService.updatePreferences(user.getId(), request);
        
        return ResponseEntity.ok(Map.of("message", "Preferences updated successfully"));
    }
    
    @DeleteMapping("/account")
    @Operation(summary = "Delete user account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                log.warn("Delete account request without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required. Please log in again."));
            }
            
            User user = (User) authentication.getPrincipal();
            log.info("Deleting account for user: {} (username: {})", user.getId(), user.getUsername());
            
            userService.deleteAccount(user.getId());
            
            log.info("Account deleted successfully for user: {}", user.getId());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (com.xaiforge.common.exception.UserNotFoundException e) {
            log.error("User not found during account deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Database constraint violation during account deletion", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Cannot delete account due to database constraints. Please contact support."));
        } catch (Exception e) {
            log.error("Error deleting account", e);
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "An unexpected error occurred";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete account: " + errorMessage));
        }
    }
    
    @PostMapping("/forgot-password/check-email")
    @Operation(summary = "Check if email exists")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email check completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ResponseEntity<CheckEmailResponse> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        // Note: This reveals if an email exists, which could be a security concern.
        // In production, consider always returning true and handling validation in send-otp endpoint.
        // Rate limiting should also be implemented here to prevent abuse.
        boolean exists = userRepository.existsByEmail(request.email());
        return ResponseEntity.ok(new CheckEmailResponse(exists));
    }
    
    @PostMapping("/forgot-password/send-otp")
    @Operation(summary = "Send OTP to email for password reset")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Failed to send email")
    })
    public ResponseEntity<SendOtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        // Check if email exists
        if (!userRepository.existsByEmail(request.email())) {
            throw new ValidationException("email", "Email not found");
        }
        
        // Generate and store OTP
        String otp = otpService.generateOtp();
        otpService.storeOtp(request.email(), otp);
        
        // Send OTP via email
        try {
            emailService.sendOtpEmail(request.email(), otp);
        } catch (EmailService.EmailSendingException e) {
            throw new com.xaiforge.common.exception.InternalServerException("Failed to send OTP email");
        }
        
        return ResponseEntity.ok(new SendOtpResponse("OTP sent successfully"));
    }
    
    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Verify OTP code")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verification completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid OTP code")
    })
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.email(), request.code());
        return ResponseEntity.ok(new VerifyOtpResponse(isValid));
    }
    
    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset password after OTP verification")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "OTP not verified or invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Verify that OTP was previously validated
        if (!otpService.isOtpVerified(request.email())) {
            throw new ValidationException("email", "OTP must be verified before password reset");
        }
        
        // Find user
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ValidationException("email", "User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        // Invalidate OTP after successful password reset
        otpService.invalidateOtp(request.email());
        
        return ResponseEntity.ok(new ResetPasswordResponse("Password reset successfully"));
    }
}

