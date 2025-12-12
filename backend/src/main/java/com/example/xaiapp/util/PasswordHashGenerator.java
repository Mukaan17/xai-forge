package com.example.xaiapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes.
 * Run this as a main method to generate a hash for a password.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordHashGenerator <password>");
            System.out.println("Example: java PasswordHashGenerator mypassword123");
            return;
        }
        
        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("\nSQL to update password:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'mg8192';");
    }
}
