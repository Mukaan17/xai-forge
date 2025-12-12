package com.example.xaiapp.util;

import org.springframework.stereotype.Component;

/**
 * Utility for parsing User-Agent strings to extract device information.
 * Simplified implementation - can be enhanced with ua-parser library.
 */
@Component
public class DeviceParser {

    /**
     * Parse User-Agent string and return formatted device info.
     * 
     * @param userAgent User-Agent header value
     * @return Formatted string like "Chrome 120 on macOS" or "Safari on iOS"
     */
    public String parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        try {
            // Simple parsing - can be enhanced with ua-parser library
            String browser = "Unknown Browser";
            String os = "Unknown OS";
            
            // Detect browser
            if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
                browser = "Chrome";
            } else if (userAgent.contains("Firefox")) {
                browser = "Firefox";
            } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
                browser = "Safari";
            } else if (userAgent.contains("Edg")) {
                browser = "Edge";
            } else if (userAgent.contains("Opera")) {
                browser = "Opera";
            }
            
            // Detect OS
            if (userAgent.contains("Windows")) {
                os = "Windows";
            } else if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) {
                os = "macOS";
            } else if (userAgent.contains("Linux")) {
                os = "Linux";
            } else if (userAgent.contains("Android")) {
                os = "Android";
            } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                os = "iOS";
            }
            
            return browser + " on " + os;
        } catch (Exception e) {
            // Fallback parsing
            if (userAgent.contains("Chrome")) return "Chrome";
            if (userAgent.contains("Firefox")) return "Firefox";
            if (userAgent.contains("Safari")) return "Safari";
            if (userAgent.contains("Edge")) return "Edge";
            return "Unknown Device";
        }
    }
}
