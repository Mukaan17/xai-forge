package com.example.xaiapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.JwtAuthResponse;
import com.example.xaiapp.dto.LoginRequest;
import com.example.xaiapp.dto.UserDto;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, 
                         PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserDto userDto) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(userDto.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username is already taken!"));
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is already in use!"));
            }
            
            // Create new user
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            
            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success("User registered successfully!"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error occurred during registration: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Attempting login for username: " + loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            System.out.println("Authentication successful for: " + loginRequest.getUsername());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            JwtAuthResponse response = new JwtAuthResponse();
            response.setAccessToken(jwt);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("Bad credentials for: " + loginRequest.getUsername() + " - " + e.getMessage());
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            System.out.println("Login error for: " + loginRequest.getUsername() + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid username or password"));
        }
    }
}
