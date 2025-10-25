/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:57
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:39:00
 */
package com.example.xaiapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}
