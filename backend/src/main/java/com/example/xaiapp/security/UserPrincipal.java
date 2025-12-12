package com.example.xaiapp.security;

import com.example.xaiapp.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Wrapper for User entity to provide UserDetails interface.
 */
@Getter
public class UserPrincipal implements UserDetails {
    
    private final User user;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
    
    public Long getId() {
        return user.getId();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }
    
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
