package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.ActivityLogApplicationService;
import com.xaiforge.application.service.EmailVerificationService;
import com.xaiforge.application.service.SessionApplicationService;
import com.xaiforge.application.service.TwoFactorApplicationService;
import com.xaiforge.application.service.UserApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.*;
import com.xaiforge.common.dto.TwoFactorSetupDto;
import com.xaiforge.common.exception.ValidationException;
import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.email.EmailService;
import com.xaiforge.infrastructure.otp.OtpService;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final EmailVerificationService emailVerificationService;
    private final TwoFactorApplicationService twoFactorApplicationService;
    private final SessionApplicationService sessionService;
    private final ActivityLogApplicationService activityLogService;
    
    public AuthController(AuthenticationManager authenticationManager, 
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider,
                          UserApplicationService userService,
                          OtpService otpService,
                          EmailService emailService,
                          EmailVerificationService emailVerificationService,
                          TwoFactorApplicationService twoFactorApplicationService,
                          SessionApplicationService sessionService,
                          ActivityLogApplicationService activityLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.emailVerificationService = emailVerificationService;
        this.twoFactorApplicationService = twoFactorApplicationService;
        this.sessionService = sessionService;
        this.activityLogService = activityLogService;
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = """
            Register a new user account. After registration:
            - A verification email will be sent to the provided email address
            - A JWT token will be returned for immediate authentication
            - The user must verify their email before accessing certain features
            
            **Validation Rules:**
            - Username: 3-50 characters, unique
            - Email: Valid email format, unique
            - Password: Minimum 6 characters
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john.doe@example.com",
                            "firstName": null,
                            "lastName": null,
                            "twoFactorEnabled": false
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation failed or user already exists",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "type": "https://example.com/problems/validation-error",
                          "title": "Validation Failed",
                          "status": 400,
                          "detail": "Username is already taken"
                        }
                        """
                )
            )
        )
    })
    @LogActivity(
        eventType = "LOGIN_SUCCESS",
        description = "New user registered: #{#request.username}",
        resourceType = "USER"
    )
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
        user.setEmailVerified(false); // Email not verified yet
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        try {
            emailVerificationService.sendVerificationEmail(savedUser);
        } catch (Exception e) {
            log.warn("Failed to send verification email, but user was created: {}", e.getMessage());
            // Don't fail registration if email sending fails
        }
        
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
    @Operation(
        summary = "Authenticate user and get JWT token",
        description = """
            Authenticate a user with username/email and password. Returns a JWT token that must be included
            in the Authorization header for subsequent API requests.
            
            **Authentication:**
            - Use the returned token in the Authorization header: `Bearer <token>`
            - Token expires after a configured period (default: 24 hours)
            - Failed login attempts are logged for security monitoring
            
            **2FA:**
            - If 2FA is enabled, an additional verification step may be required
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AuthResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "user": {
                            "id": 1,
                            "username": "johndoe",
                            "email": "john.doe@example.com",
                            "firstName": "John",
                            "lastName": "Doe",
                            "twoFactorEnabled": true
                          }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "type": "https://example.com/problems/authentication-error",
                          "title": "Authentication Failed",
                          "status": 401,
                          "detail": "Invalid username or password"
                        }
                        """
                )
            )
        )
    })
    @LogActivity(
        eventType = "LOGIN_SUCCESS",
        description = "User logged in: #{#request.username}",
        resourceType = "USER"
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
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
            
            // Create session
            sessionService.createSession(user, jwt, httpRequest);
            
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
            // Log failed login attempt
            try {
                User user = userRepository.findByUsername(request.username()).orElse(null);
                Long userId = user != null ? user.getId() : null;
                activityLogService.logActivityAsync(
                    userId,
                    ActivityLog.EventType.LOGIN_FAILED,
                    "Failed login attempt for username: " + request.username(),
                    java.util.Map.of("username", request.username())
                );
            } catch (Exception logError) {
                log.debug("Failed to log login failure", logError);
            }
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
    @LogActivity(
        eventType = "PROFILE_UPDATED",
        description = "Profile updated",
        resourceType = "USER"
    )
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
    @LogActivity(
        eventType = "PASSWORD_CHANGED",
        description = "Password changed",
        resourceType = "USER"
    )
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
    
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<Map<String, String>> verifyEmail(
            @RequestParam String token) {
        boolean verified = emailVerificationService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid or expired verification token"));
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification email sent"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email already verified or not found")
    })
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Verification email sent successfully"));
    }
    
    @PostMapping("/2fa/enable")
    @Operation(summary = "Enable 2FA - returns QR code and backup codes")
    @LogActivity(
        eventType = "TWO_FACTOR_SETUP_INITIATED",
        description = "2FA setup initiated",
        resourceType = "USER",
        resourceId = "#{#authentication.principal.id}",
        resourceName = "#{#authentication.principal.username}"
    )
    public ResponseEntity<TwoFactorSetupDto> enable2FA(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TwoFactorSetupDto setup = twoFactorApplicationService.enable2FA(user.getId());
        return ResponseEntity.ok(setup);
    }
    
    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify 2FA code and activate")
    @LogActivity(
        eventType = "TWO_FACTOR_ENABLED",
        description = "2FA verification attempted",
        resourceType = "USER",
        resourceId = "#{#authentication.principal.id}",
        resourceName = "#{#authentication.principal.username}"
    )
    public ResponseEntity<Map<String, Object>> verify2FA(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            throw new ValidationException("code", "Verification code is required");
        }
        
        boolean valid = twoFactorApplicationService.verify2FA(user.getId(), code);
        return ResponseEntity.ok(Map.of(
            "valid", valid,
            "message", valid ? "2FA enabled successfully" : "Invalid verification code"
        ));
    }
    
    @DeleteMapping("/2fa")
    @Operation(summary = "Disable 2FA")
    @LogActivity(
        eventType = "TWO_FACTOR_DISABLED",
        description = "2FA disabled",
        resourceType = "USER",
        resourceId = "#{#authentication.principal.id}",
        resourceName = "#{#authentication.principal.username}"
    )
    public ResponseEntity<Map<String, String>> disable2FA(
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            throw new ValidationException("code", "Verification code is required");
        }
        
        twoFactorApplicationService.disable2FA(user.getId(), code);
        return ResponseEntity.ok(Map.of("message", "2FA disabled successfully"));
    }
}

