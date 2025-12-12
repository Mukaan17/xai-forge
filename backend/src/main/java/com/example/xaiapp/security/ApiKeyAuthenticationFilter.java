package com.example.xaiapp.security;

import com.example.xaiapp.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filter for API key authentication.
 * Checks for X-API-Key header and validates the key.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null && !apiKey.isEmpty()) {
            Optional<Map<String, Object>> validationResult = apiKeyService.validateApiKey(apiKey);

            if (validationResult.isPresent()) {
                Map<String, Object> result = validationResult.get();
                Long userId = ((Number) result.get("userId")).longValue();
                Long keyId = ((Number) result.get("keyId")).longValue();
                @SuppressWarnings("unchecked")
                Set<String> permissions = (Set<String>) result.get("permissions");

                // Convert permissions to authorities
                List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(perm -> new SimpleGrantedAuthority("PERM_" + perm.toUpperCase().replace(":", "_")))
                    .collect(Collectors.toList());

                // Create authentication token
                ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                    userId,
                    keyId,
                    authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Update last used
                String ipAddress = extractIpAddress(request);
                apiKeyService.updateLastUsed(keyId, ipAddress);

                log.debug("API key authenticated: userId={}, keyId={}", userId, keyId);
            } else {
                log.debug("Invalid API key provided");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid API key\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip for auth endpoints and public endpoints
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/api/auth/") || path.startsWith("/api/public/");
        if (shouldSkip) {
            log.debug("Skipping API key filter for path: {}", path);
        }
        return shouldSkip;
    }
}
