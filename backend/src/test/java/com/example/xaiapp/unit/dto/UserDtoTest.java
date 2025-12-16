package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.UserDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserDto
 * Tests constructors, Lombok methods, and validation annotations
 */
class UserDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor_createsEmptyDto() {
        // Act
        UserDto dto = new UserDto();
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertNull(dto.getPassword());
    }

    @Test
    void testAllArgsConstructor_createsCompleteDto() {
        // Arrange
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        
        // Act
        UserDto dto = new UserDto(id, username, email, password);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(password, dto.getPassword());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        UserDto dto = new UserDto();
        Long id = 2L;
        String username = "newuser";
        String email = "new@example.com";
        String password = "newpassword";
        
        // Act
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        
        // Assert
        assertEquals(id, dto.getId());
        assertEquals(username, dto.getUsername());
        assertEquals(email, dto.getEmail());
        assertEquals(password, dto.getPassword());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        UserDto dto = createTestDto();
        
        // Act & Assert
        assertEquals(dto, dto);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        UserDto dto1 = createTestDto();
        UserDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        UserDto dto1 = createTestDto();
        UserDto dto2 = createTestDto();
        dto2.setUsername("different");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        UserDto dto = createTestDto();
        
        // Act & Assert
        assertNotEquals(dto, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        UserDto dto = createTestDto();
        String other = "not a dto";
        
        // Act & Assert
        assertNotEquals(dto, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        UserDto dto1 = createTestDto();
        UserDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        UserDto dto = createTestDto();
        
        // Act
        String toString = dto.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("password123"));
    }

    @Test
    void testValidation_validDto_noViolations() {
        // Arrange
        UserDto dto = new UserDto(1L, "validuser", "valid@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_nullUsername_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, null, "test@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testValidation_blankUsername_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "", "test@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(2, violations.size()); // @NotBlank + @Size violations
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("Username"));
    }

    @Test
    void testValidation_whitespaceUsername_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "   ", "test@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testValidation_shortUsername_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "ab", "test@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username must be between 3 and 50 characters", violation.getMessage());
    }

    @Test
    void testValidation_longUsername_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "a".repeat(51), "test@example.com", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("username", violation.getPropertyPath().toString());
        assertEquals("Username must be between 3 and 50 characters", violation.getMessage());
    }

    @Test
    void testValidation_nullEmail_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", null, "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email is required", violation.getMessage());
    }

    @Test
    void testValidation_blankEmail_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", "", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email is required", violation.getMessage());
    }

    @Test
    void testValidation_invalidEmail_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", "invalid-email", "password123");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email should be valid", violation.getMessage());
    }

    @Test
    void testValidation_nullPassword_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", "test@example.com", null);
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password is required", violation.getMessage());
    }

    @Test
    void testValidation_blankPassword_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", "test@example.com", "");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(2, violations.size()); // @NotBlank + @Size violations
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertTrue(violation.getMessage().contains("Password"));
    }

    @Test
    void testValidation_shortPassword_violation() {
        // Arrange
        UserDto dto = new UserDto(1L, "testuser", "test@example.com", "12345");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UserDto> violation = violations.iterator().next();
        assertEquals("password", violation.getPropertyPath().toString());
        assertEquals("Password must be at least 6 characters", violation.getMessage());
    }

    @Test
    void testValidation_multipleViolations_returnsAllViolations() {
        // Arrange
        UserDto dto = new UserDto(null, "", "invalid-email", "12345");
        
        // Act
        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        
        // Assert
        assertEquals(4, violations.size());
        
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
        boolean hasEmailViolation = violations.stream()
            .anyMatch(v -> "email".equals(v.getPropertyPath().toString()));
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> "password".equals(v.getPropertyPath().toString()));
            
        assertTrue(hasUsernameViolation);
        assertTrue(hasEmailViolation);
        assertTrue(hasPasswordViolation);
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act
        dto.setId(null);
        dto.setUsername(null);
        dto.setEmail(null);
        dto.setPassword(null);
        
        // Assert
        assertNull(dto.getId());
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertNull(dto.getPassword());
    }

    @Test
    void testWithEmptyStrings_handlesEmptyStrings() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act
        dto.setUsername("");
        dto.setEmail("");
        dto.setPassword("");
        
        // Assert
        assertEquals("", dto.getUsername());
        assertEquals("", dto.getEmail());
        assertEquals("", dto.getPassword());
    }

    @Test
    void testWithZeroId_handlesZero() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act
        dto.setId(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), dto.getId());
    }

    @Test
    void testWithNegativeId_handlesNegativeValues() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act
        dto.setId(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), dto.getId());
    }

    @Test
    void testWithMaxId_handlesMaxValue() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act
        dto.setId(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, dto.getId());
    }

    @Test
    void testWithMinValidUsername_handlesMinLength() {
        // Arrange
        UserDto dto = new UserDto();
        String minUsername = "abc";
        
        // Act
        dto.setUsername(minUsername);
        
        // Assert
        assertEquals(minUsername, dto.getUsername());
    }

    @Test
    void testWithMaxValidUsername_handlesMaxLength() {
        // Arrange
        UserDto dto = new UserDto();
        String maxUsername = "a".repeat(50);
        
        // Act
        dto.setUsername(maxUsername);
        
        // Assert
        assertEquals(maxUsername, dto.getUsername());
    }

    @Test
    void testWithMinValidPassword_handlesMinLength() {
        // Arrange
        UserDto dto = new UserDto();
        String minPassword = "123456";
        
        // Act
        dto.setPassword(minPassword);
        
        // Assert
        assertEquals(minPassword, dto.getPassword());
    }

    @Test
    void testWithLongPassword_handlesLongStrings() {
        // Arrange
        UserDto dto = new UserDto();
        String longPassword = "very_long_password_with_many_characters_and_numbers_12345";
        
        // Act
        dto.setPassword(longPassword);
        
        // Assert
        assertEquals(longPassword, dto.getPassword());
    }

    @Test
    void testWithValidEmails_handlesValidEmails() {
        // Arrange
        UserDto dto = new UserDto();
        
        // Act & Assert
        dto.setEmail("test@example.com");
        assertEquals("test@example.com", dto.getEmail());
        
        dto.setEmail("user.name@domain.co.uk");
        assertEquals("user.name@domain.co.uk", dto.getEmail());
        
        dto.setEmail("user+tag@example.org");
        assertEquals("user+tag@example.org", dto.getEmail());
    }

    @Test
    void testWithSpecialCharactersInUsername_handlesSpecialChars() {
        // Arrange
        UserDto dto = new UserDto();
        String specialUsername = "user_name-123";
        
        // Act
        dto.setUsername(specialUsername);
        
        // Assert
        assertEquals(specialUsername, dto.getUsername());
    }

    @Test
    void testWithSpecialCharactersInPassword_handlesSpecialChars() {
        // Arrange
        UserDto dto = new UserDto();
        String specialPassword = "pass@word#123$";
        
        // Act
        dto.setPassword(specialPassword);
        
        // Assert
        assertEquals(specialPassword, dto.getPassword());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        UserDto dto = new UserDto();
        String unicodeUsername = "用户名123";
        String unicodeEmail = "用户@example.com";
        String unicodePassword = "密码456";
        
        // Act
        dto.setUsername(unicodeUsername);
        dto.setEmail(unicodeEmail);
        dto.setPassword(unicodePassword);
        
        // Assert
        assertEquals(unicodeUsername, dto.getUsername());
        assertEquals(unicodeEmail, dto.getEmail());
        assertEquals(unicodePassword, dto.getPassword());
    }

    @Test
    void testWithWhitespaceInUsername_handlesWhitespace() {
        // Arrange
        UserDto dto = new UserDto();
        String usernameWithWhitespace = " user name ";
        
        // Act
        dto.setUsername(usernameWithWhitespace);
        
        // Assert
        assertEquals(usernameWithWhitespace, dto.getUsername());
    }

    @Test
    void testWithWhitespaceInEmail_handlesWhitespace() {
        // Arrange
        UserDto dto = new UserDto();
        String emailWithWhitespace = " user@example.com ";
        
        // Act
        dto.setEmail(emailWithWhitespace);
        
        // Assert
        assertEquals(emailWithWhitespace, dto.getEmail());
    }

    @Test
    void testWithWhitespaceInPassword_handlesWhitespace() {
        // Arrange
        UserDto dto = new UserDto();
        String passwordWithWhitespace = " pass word ";
        
        // Act
        dto.setPassword(passwordWithWhitespace);
        
        // Assert
        assertEquals(passwordWithWhitespace, dto.getPassword());
    }

    private UserDto createTestDto() {
        return new UserDto(1L, "testuser", "test@example.com", "password123");
    }
}
