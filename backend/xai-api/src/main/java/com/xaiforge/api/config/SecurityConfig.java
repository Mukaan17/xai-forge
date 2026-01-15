package com.xaiforge.api.config;

import com.xaiforge.api.filter.SecurityHeadersFilter;
import com.xaiforge.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityHeadersConfig securityHeadersConfig;
    private final SecurityHeadersFilter securityHeadersFilter;
    
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter, 
            UserDetailsService userDetailsService,
            SecurityHeadersConfig securityHeadersConfig,
            SecurityHeadersFilter securityHeadersFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.securityHeadersConfig = securityHeadersConfig;
        this.securityHeadersFilter = securityHeadersFilter;
    }
    
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http.authenticationProvider(authenticationProvider)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/account").authenticated() // Delete account requires authentication - must come before general auth rule
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> {
                headers.frameOptions(frameOptions -> frameOptions.deny());
                headers.contentTypeOptions(contentTypeOptions -> {});
                
                // HSTS Configuration
                if (securityHeadersConfig.isHstsEnabled()) {
                    headers.httpStrictTransportSecurity(hstsConfig -> {
                        hstsConfig.maxAgeInSeconds(securityHeadersConfig.getHstsMaxAge());
                        // Note: includeSubdomains is automatically included in Spring Security 6+
                        // The header value will include it based on the configuration
                    });
                }
                
                // Referrer Policy
                headers.referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(securityHeadersConfig.getReferrerPolicyValue())
                );
                
                // XSS Protection (legacy, but still useful)
                headers.xssProtection(xssConfig -> 
                    xssConfig.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                );
                
                // Content Security Policy
                String csp = securityHeadersConfig.getContentSecurityPolicy();
                if (csp != null && !csp.isEmpty()) {
                    headers.contentSecurityPolicy(cspConfig -> 
                        cspConfig.policyDirectives(csp)
                    );
                }
            })
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

