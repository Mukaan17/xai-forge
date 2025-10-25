/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-09-04 16:05:59
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-10-24 18:39:01
 */
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
}
