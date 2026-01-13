package com.xaiforge.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "theme")
    private String theme = "dark"; // dark, light, system
    
    @Column(name = "accent_color")
    private String accentColor = "#00d9ff";
    
    @Column(name = "notification_prefs", columnDefinition = "TEXT")
    private String notificationPreferences; // JSON string
}

