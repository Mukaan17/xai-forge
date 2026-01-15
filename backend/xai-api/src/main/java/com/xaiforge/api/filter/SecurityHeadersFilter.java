package com.xaiforge.api.filter;

import com.xaiforge.api.config.SecurityHeadersConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter to add security headers that Spring Security doesn't support natively
 * 
 * Adds:
 * - Permissions-Policy header
 * - Additional custom security headers if needed
 * 
 * @since 1.0.0
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    private final SecurityHeadersConfig securityHeadersConfig;
    
    public SecurityHeadersFilter(SecurityHeadersConfig securityHeadersConfig) {
        this.securityHeadersConfig = securityHeadersConfig;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Add Permissions-Policy header
        String permissionsPolicy = securityHeadersConfig.getPermissionsPolicy();
        if (permissionsPolicy != null && !permissionsPolicy.isEmpty()) {
            response.setHeader("Permissions-Policy", permissionsPolicy);
        }
        
        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
