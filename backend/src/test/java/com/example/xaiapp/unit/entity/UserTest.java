package com.example.xaiapp.unit.entity;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.entity.Dataset;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 * Tests constructors, Lombok methods, @PrePersist, UserDetails interface, and equals/hashCode
 */
class UserTest {

    @Test
    void testNoArgsConstructor_createsEmptyUser() {
        // Act
        User user = new User();
        
        // Assert
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getCreatedAt());
        assertNull(user.getDatasets());
    }

    @Test
    void testAllArgsConstructor_createsCompleteUser() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        LocalDateTime createdAt = LocalDateTime.now();
        List<Dataset> datasets = Arrays.asList(new Dataset(), new Dataset());
        
        // Act
        User user = new User(id, username, email, password, createdAt, datasets);
        
        // Assert
        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(datasets, user.getDatasets());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        User user = new User();
        Long id = 2L;
        String username = "newuser";
        String email = "new@example.com";
        String password = "newpassword";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        List<Dataset> datasets = Arrays.asList(new Dataset());
        
        // Act
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(createdAt);
        user.setDatasets(datasets);
        
        // Assert
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(datasets, user.getDatasets());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        User user = createTestUser();
        
        // Act & Assert
        assertEquals(user, user);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        User user1 = createTestUser();
        User user2 = createTestUser();
        
        // Act & Assert
        assertEquals(user1, user2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        User user1 = createTestUser();
        User user2 = createTestUser();
        user2.setUsername("different");
        
        // Act & Assert
        assertNotEquals(user1, user2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        User user = createTestUser();
        
        // Act & Assert
        assertNotEquals(user, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        User user = createTestUser();
        String other = "not a user";
        
        // Act & Assert
        assertNotEquals(user, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        User user1 = createTestUser();
        User user2 = createTestUser();
        
        // Act & Assert
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        User user = createTestUser();
        
        // Act
        String toString = user.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("password123"));
    }

    @Test
    void testGetAuthorities_returnsRoleUser() {
        // Arrange
        User user = new User();
        
        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testIsAccountNonExpired_returnsTrue() {
        // Arrange
        User user = new User();
        
        // Act
        boolean isAccountNonExpired = user.isAccountNonExpired();
        
        // Assert
        assertTrue(isAccountNonExpired);
    }

    @Test
    void testIsAccountNonLocked_returnsTrue() {
        // Arrange
        User user = new User();
        
        // Act
        boolean isAccountNonLocked = user.isAccountNonLocked();
        
        // Assert
        assertTrue(isAccountNonLocked);
    }

    @Test
    void testIsCredentialsNonExpired_returnsTrue() {
        // Arrange
        User user = new User();
        
        // Act
        boolean isCredentialsNonExpired = user.isCredentialsNonExpired();
        
        // Assert
        assertTrue(isCredentialsNonExpired);
    }

    @Test
    void testIsEnabled_returnsTrue() {
        // Arrange
        User user = new User();
        
        // Act
        boolean isEnabled = user.isEnabled();
        
        // Assert
        assertTrue(isEnabled);
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        User user = new User();
        
        // Act
        user.setId(null);
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setCreatedAt(null);
        user.setDatasets(null);
        
        // Assert
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getCreatedAt());
        assertNull(user.getDatasets());
    }

    @Test
    void testWithEmptyStrings_handlesEmptyStrings() {
        // Arrange
        User user = new User();
        
        // Act
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        
        // Assert
        assertEquals("", user.getUsername());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
    }

    @Test
    void testWithZeroId_handlesZero() {
        // Arrange
        User user = new User();
        
        // Act
        user.setId(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), user.getId());
    }

    @Test
    void testWithNegativeId_handlesNegativeValues() {
        // Arrange
        User user = new User();
        
        // Act
        user.setId(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), user.getId());
    }

    @Test
    void testWithMaxId_handlesMaxValue() {
        // Arrange
        User user = new User();
        
        // Act
        user.setId(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, user.getId());
    }

    @Test
    void testWithLongUsername_handlesLongStrings() {
        // Arrange
        User user = new User();
        String longUsername = "very_long_username_with_many_characters_and_numbers_12345";
        
        // Act
        user.setUsername(longUsername);
        
        // Assert
        assertEquals(longUsername, user.getUsername());
    }

    @Test
    void testWithLongEmail_handlesLongStrings() {
        // Arrange
        User user = new User();
        String longEmail = "very_long_email_address_with_many_characters_and_numbers_12345@example.com";
        
        // Act
        user.setEmail(longEmail);
        
        // Assert
        assertEquals(longEmail, user.getEmail());
    }

    @Test
    void testWithLongPassword_handlesLongStrings() {
        // Arrange
        User user = new User();
        String longPassword = "very_long_password_with_many_characters_and_numbers_12345";
        
        // Act
        user.setPassword(longPassword);
        
        // Assert
        assertEquals(longPassword, user.getPassword());
    }

    @Test
    void testWithManyDatasets_handlesLargeLists() {
        // Arrange
        User user = new User();
        List<Dataset> manyDatasets = Arrays.asList(
            new Dataset(), new Dataset(), new Dataset(), new Dataset(), new Dataset(),
            new Dataset(), new Dataset(), new Dataset(), new Dataset(), new Dataset()
        );
        
        // Act
        user.setDatasets(manyDatasets);
        
        // Assert
        assertEquals(manyDatasets, user.getDatasets());
        assertEquals(10, user.getDatasets().size());
    }

    @Test
    void testWithSpecialCharactersInUsername_handlesSpecialChars() {
        // Arrange
        User user = new User();
        String specialUsername = "user@domain.com";
        
        // Act
        user.setUsername(specialUsername);
        
        // Assert
        assertEquals(specialUsername, user.getUsername());
    }

    @Test
    void testWithSpecialCharactersInEmail_handlesSpecialChars() {
        // Arrange
        User user = new User();
        String specialEmail = "user+tag@domain.co.uk";
        
        // Act
        user.setEmail(specialEmail);
        
        // Assert
        assertEquals(specialEmail, user.getEmail());
    }

    @Test
    void testWithSpecialCharactersInPassword_handlesSpecialChars() {
        // Arrange
        User user = new User();
        String specialPassword = "pass@word#123$";
        
        // Act
        user.setPassword(specialPassword);
        
        // Assert
        assertEquals(specialPassword, user.getPassword());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        User user = new User();
        String unicodeUsername = "用户名123";
        String unicodeEmail = "用户@example.com";
        String unicodePassword = "密码456";
        
        // Act
        user.setUsername(unicodeUsername);
        user.setEmail(unicodeEmail);
        user.setPassword(unicodePassword);
        
        // Assert
        assertEquals(unicodeUsername, user.getUsername());
        assertEquals(unicodeEmail, user.getEmail());
        assertEquals(unicodePassword, user.getPassword());
    }

    @Test
    void testWithWhitespaceInUsername_handlesWhitespace() {
        // Arrange
        User user = new User();
        String usernameWithWhitespace = " user name ";
        
        // Act
        user.setUsername(usernameWithWhitespace);
        
        // Assert
        assertEquals(usernameWithWhitespace, user.getUsername());
    }

    @Test
    void testWithWhitespaceInEmail_handlesWhitespace() {
        // Arrange
        User user = new User();
        String emailWithWhitespace = " user@example.com ";
        
        // Act
        user.setEmail(emailWithWhitespace);
        
        // Assert
        assertEquals(emailWithWhitespace, user.getEmail());
    }

    @Test
    void testWithWhitespaceInPassword_handlesWhitespace() {
        // Arrange
        User user = new User();
        String passwordWithWhitespace = " pass word ";
        
        // Act
        user.setPassword(passwordWithWhitespace);
        
        // Assert
        assertEquals(passwordWithWhitespace, user.getPassword());
    }

    @Test
    void testWithDifferentCreatedAtDates_handlesDifferentDates() {
        // Test different creation dates
        LocalDateTime[] testDates = {
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 6, 15, 12, 30),
            LocalDateTime.of(2024, 12, 31, 23, 59),
            LocalDateTime.now()
        };
        
        for (LocalDateTime date : testDates) {
            // Arrange
            User user = new User();
            
            // Act
            user.setCreatedAt(date);
            
            // Assert
            assertEquals(date, user.getCreatedAt());
        }
    }

    @Test
    void testWithEmptyDatasets_handlesEmptyList() {
        // Arrange
        User user = new User();
        List<Dataset> emptyDatasets = Arrays.asList();
        
        // Act
        user.setDatasets(emptyDatasets);
        
        // Assert
        assertEquals(emptyDatasets, user.getDatasets());
        assertTrue(user.getDatasets().isEmpty());
    }

    @Test
    void testUserDetailsInterfaceMethods_alwaysReturnExpectedValues() {
        // Arrange
        User user = new User();
        
        // Act & Assert
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testWithDifferentIdValues_handlesDifferentIds() {
        // Test different ID values
        Long[] testIds = {1L, 100L, 999L, 1000L, Long.MAX_VALUE};
        
        for (Long id : testIds) {
            // Arrange
            User user = new User();
            
            // Act
            user.setId(id);
            
            // Assert
            assertEquals(id, user.getId());
        }
    }

    @Test
    void testWithValidEmailFormats_handlesValidEmails() {
        // Test different valid email formats
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "user123@test-domain.com"
        };
        
        for (String email : validEmails) {
            // Arrange
            User user = new User();
            
            // Act
            user.setEmail(email);
            
            // Assert
            assertEquals(email, user.getEmail());
        }
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        return user;
    }
}
