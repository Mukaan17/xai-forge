package com.example.xaiapp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    // Manual constructor (Lombok @RequiredArgsConstructor not working with Java 24)
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("UserDetailsService: Loading user: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        System.out.println("UserDetailsService: Found user - username: " + user.getUsername() + ", password hash: " + (user.getPassword() != null ? user.getPassword().substring(0, Math.min(30, user.getPassword().length())) : "null"));
        
        return user;
    }
}
