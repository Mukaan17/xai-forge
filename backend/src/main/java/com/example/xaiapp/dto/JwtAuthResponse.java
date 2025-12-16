package com.example.xaiapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    
    // Manual setters (Lombok not generating them)
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
}
