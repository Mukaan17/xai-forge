package com.example.xaiapp.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Authentication token for API key authentication.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final Long apiKeyId;

    public ApiKeyAuthenticationToken(
            Long userId,
            Long apiKeyId,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKeyId;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }
}
