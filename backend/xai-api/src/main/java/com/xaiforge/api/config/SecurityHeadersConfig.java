package com.xaiforge.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Security Headers Configuration
 * 
 * Configures comprehensive security headers for the application:
 * - Content-Security-Policy (CSP)
 * - X-Frame-Options
 * - X-Content-Type-Options
 * - Strict-Transport-Security (HSTS)
 * - Referrer-Policy
 * - Permissions-Policy
 * - X-XSS-Protection (legacy, but still useful for older browsers)
 * 
 * @since 1.0.0
 */
@Configuration
public class SecurityHeadersConfig {
    
    @Value("${app.security.csp.enabled:true}")
    private boolean cspEnabled;
    
    @Value("${app.security.csp.report-uri:}")
    private String cspReportUri;
    
    @Value("${app.security.hsts.enabled:true}")
    private boolean hstsEnabled;
    
    @Value("${app.security.hsts.max-age:31536000}")
    private long hstsMaxAge; // Default: 1 year in seconds
    
    @Value("${app.security.hsts.include-subdomains:true}")
    private boolean hstsIncludeSubdomains;
    
    @Value("${app.security.hsts.preload:false}")
    private boolean hstsPreload;
    
    /**
     * Build Content-Security-Policy header
     * 
     * Policy allows:
     * - Self for scripts, styles, images, fonts, connect
     * - Inline scripts and styles (for React and Tailwind)
     * - Data URIs for images
     * - Blob URIs for file downloads
     * - Google Fonts for fonts
     * - Swagger UI resources
     * 
     * @return CSP header value
     */
    public String buildContentSecurityPolicy() {
        if (!cspEnabled) {
            return null;
        }
        
        StringBuilder csp = new StringBuilder();
        
        // Default source - only allow same origin
        csp.append("default-src 'self'; ");
        
        // Scripts - allow self, inline (for React), eval (for development), and nonce
        csp.append("script-src 'self' 'unsafe-inline' 'unsafe-eval' https://fonts.googleapis.com; ");
        
        // Styles - allow self, inline (for Tailwind), and Google Fonts
        csp.append("style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://fonts.gstatic.com; ");
        
        // Images - allow self, data URIs, blob URIs, and external images
        csp.append("img-src 'self' data: blob: https:; ");
        
        // Fonts - allow self, data URIs, and Google Fonts
        csp.append("font-src 'self' data: https://fonts.gstatic.com; ");
        
        // Connect (XHR, fetch, WebSocket) - allow self
        csp.append("connect-src 'self' ws: wss:; ");
        
        // Media - allow self and blob
        csp.append("media-src 'self' blob:; ");
        
        // Object - deny all (no Flash, plugins)
        csp.append("object-src 'none'; ");
        
        // Base URI - only allow self
        csp.append("base-uri 'self'; ");
        
        // Form action - only allow self
        csp.append("form-action 'self'; ");
        
        // Frame ancestors - deny all (X-Frame-Options: DENY)
        csp.append("frame-ancestors 'none'; ");
        
        // Upgrade insecure requests in production
        if (hstsEnabled) {
            csp.append("upgrade-insecure-requests; ");
        }
        
        // Report URI (if configured)
        if (cspReportUri != null && !cspReportUri.isEmpty()) {
            csp.append("report-uri ").append(cspReportUri).append("; ");
        }
        
        return csp.toString().trim();
    }
    
    /**
     * Build Permissions-Policy header
     * 
     * Restricts browser features to prevent abuse:
     * - Disables geolocation, camera, microphone, payment, etc.
     * - Only allows features explicitly needed
     * 
     * @return Permissions-Policy header value
     */
    public String buildPermissionsPolicy() {
        StringBuilder policy = new StringBuilder();
        
        // Disable potentially dangerous features
        policy.append("geolocation=(), ");
        policy.append("camera=(), ");
        policy.append("microphone=(), ");
        policy.append("payment=(), ");
        policy.append("usb=(), ");
        policy.append("magnetometer=(), ");
        policy.append("gyroscope=(), ");
        policy.append("accelerometer=(), ");
        policy.append("ambient-light-sensor=(), ");
        policy.append("autoplay=(), ");
        policy.append("encrypted-media=(), ");
        policy.append("picture-in-picture=(), ");
        policy.append("sync-xhr=(), ");
        policy.append("wake-lock=(), ");
        policy.append("xr-spatial-tracking=()");
        
        return policy.toString();
    }
    
    /**
     * Build Referrer-Policy header value
     * 
     * @return Referrer-Policy value
     */
    public ReferrerPolicyHeaderWriter.ReferrerPolicy getReferrerPolicy() {
        return ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN;
    }
    
    /**
     * Build HSTS header value
     * Note: Spring Security handles HSTS header automatically, but this method
     * can be used for custom header writers if needed
     * 
     * @return HSTS header value string
     */
    public String buildHstsHeader() {
        if (!hstsEnabled) {
            return null;
        }
        
        StringBuilder hsts = new StringBuilder();
        hsts.append("max-age=").append(hstsMaxAge);
        
        if (hstsIncludeSubdomains) {
            hsts.append("; includeSubDomains");
        }
        
        if (hstsPreload) {
            hsts.append("; preload");
        }
        
        return hsts.toString();
    }
    
    /**
     * Get HSTS includeSubdomains flag
     * Used to configure Spring Security HSTS
     * 
     * @return true if subdomains should be included
     */
    public boolean shouldIncludeSubdomains() {
        return hstsIncludeSubdomains;
    }
    
    // Getters for use in SecurityConfig
    public boolean isCspEnabled() {
        return cspEnabled;
    }
    
    public String getContentSecurityPolicy() {
        return buildContentSecurityPolicy();
    }
    
    public String getPermissionsPolicy() {
        return buildPermissionsPolicy();
    }
    
    public ReferrerPolicyHeaderWriter.ReferrerPolicy getReferrerPolicyValue() {
        return getReferrerPolicy();
    }
    
    public String getHstsHeader() {
        return buildHstsHeader();
    }
    
    public boolean isHstsEnabled() {
        return hstsEnabled;
    }
    
    public long getHstsMaxAge() {
        return hstsMaxAge;
    }
    
    public boolean isHstsIncludeSubdomains() {
        return hstsIncludeSubdomains;
    }
}
