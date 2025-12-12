package com.example.xaiapp.util;

import org.springframework.stereotype.Component;

/**
 * Service for IP geolocation.
 * In production, this would use a service like MaxMind GeoIP2 or ipapi.co
 * For now, provides a simple implementation.
 */
@Component
public class GeoIpService {

    /**
     * Get location string from IP address.
     * Format: "City, Country"
     * 
     * @param ipAddress IP address
     * @return Location string or "Unknown"
     */
    public String getLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("unknown")) {
            return "Unknown";
        }

        // Check for localhost/local IPs
        if (ipAddress.startsWith("127.") || ipAddress.startsWith("192.168.") || 
            ipAddress.startsWith("10.") || ipAddress.startsWith("172.") ||
            ipAddress.equals("::1") || ipAddress.equals("localhost")) {
            return "Local";
        }

        // In production, integrate with a geolocation service
        // For now, return a placeholder
        // TODO: Integrate with MaxMind GeoIP2 or similar service
        return "Unknown Location";
    }

    /**
     * Get country code from IP address.
     * 
     * @param ipAddress IP address
     * @return Two-letter country code or null
     */
    public String getCountryCode(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || ipAddress.equals("unknown")) {
            return null;
        }

        // Check for localhost/local IPs
        if (ipAddress.startsWith("127.") || ipAddress.startsWith("192.168.") || 
            ipAddress.startsWith("10.") || ipAddress.startsWith("172.") ||
            ipAddress.equals("::1") || ipAddress.equals("localhost")) {
            return null;
        }

        // In production, integrate with a geolocation service
        // TODO: Integrate with MaxMind GeoIP2 or similar service
        return null;
    }
}
