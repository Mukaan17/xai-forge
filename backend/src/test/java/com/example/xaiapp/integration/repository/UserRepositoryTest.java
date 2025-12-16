package com.example.xaiapp.integration.repository;

import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository
 * Tests custom queries with H2 database
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean up before each test
        testUser = TestDataBuilder.createTestUser();
    }
    
    @Test
    void testFindByUsername_Success() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByUsername(TestConstants.TEST_USERNAME);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_USERNAME, result.get().getUsername());
        assertEquals(TestConstants.TEST_EMAIL, result.get().getEmail());
        assertNotNull(result.get().getPassword());
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Act
        Optional<User> result = userRepository.findByUsername("nonexistent");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByUsername_CaseInsensitive() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByUsername(TestConstants.TEST_USERNAME.toUpperCase());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_USERNAME, result.get().getUsername());
    }
    
    @Test
    void testFindByUsername_WithWhitespace() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByUsername(" " + TestConstants.TEST_USERNAME + " ");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_USERNAME, result.get().getUsername());
    }
    
    @Test
    void testFindByUsername_NullUsername() {
        // Act
        Optional<User> result = userRepository.findByUsername(null);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByUsername_EmptyUsername() {
        // Act
        Optional<User> result = userRepository.findByUsername("");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByEmail_Success() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByEmail(TestConstants.TEST_EMAIL);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_EMAIL, result.get().getEmail());
        assertEquals(TestConstants.TEST_USERNAME, result.get().getUsername());
    }
    
    @Test
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByEmail_CaseInsensitive() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByEmail(TestConstants.TEST_EMAIL.toUpperCase());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_EMAIL, result.get().getEmail());
    }
    
    @Test
    void testFindByEmail_WithWhitespace() {
        // Arrange
        userRepository.save(testUser);
        
        // Act
        Optional<User> result = userRepository.findByEmail(" " + TestConstants.TEST_EMAIL + " ");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(TestConstants.TEST_EMAIL, result.get().getEmail());
    }
    
    @Test
    void testFindByEmail_NullEmail() {
        // Act
        Optional<User> result = userRepository.findByEmail(null);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByEmail_EmptyEmail() {
        // Act
        Optional<User> result = userRepository.findByEmail("");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testSave_WithEncryptedPassword() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        String originalPassword = user.getPassword();
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getPassword());
        assertNotEquals(originalPassword, savedUser.getPassword()); // Should be encrypted
        assertTrue(savedUser.getPassword().startsWith("$2a$")); // BCrypt format
    }
    
    @Test
    void testSave_WithSpecialCharacters() {
        // Arrange
        User user = TestDataBuilder.createTestUser("user@domain.com");
        user.setEmail("user@domain.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("user@domain.com", savedUser.getUsername());
        assertEquals("user@domain.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithUnicodeCharacters() {
        // Arrange
        User user = TestDataBuilder.createTestUser("测试用户");
        user.setEmail("test@测试.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("测试用户", savedUser.getUsername());
        assertEquals("test@测试.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithLongValues() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername("a".repeat(100));
        user.setEmail("a".repeat(100) + "@example.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("a".repeat(100), savedUser.getUsername());
        assertEquals("a".repeat(100) + "@example.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithNullValues() {
        // Arrange
        User user = new User();
        user.setUsername(null);
        user.setEmail(null);
        user.setPassword(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithEmptyValues() {
        // Arrange
        User user = new User();
        user.setUsername("");
        user.setEmail("");
        user.setPassword("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithWhitespaceValues() {
        // Arrange
        User user = new User();
        user.setUsername("   ");
        user.setEmail("   ");
        user.setPassword("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithDuplicateUsername() {
        // Arrange
        User user1 = TestDataBuilder.createTestUser();
        User user2 = TestDataBuilder.createTestUser();
        user2.setEmail("different@example.com");
        
        userRepository.save(user1);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
        });
    }
    
    @Test
    void testSave_WithDuplicateEmail() {
        // Arrange
        User user1 = TestDataBuilder.createTestUser();
        User user2 = TestDataBuilder.createTestUser();
        user2.setUsername("differentuser");
        
        userRepository.save(user1);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
        });
    }
    
    @Test
    void testSave_WithExtremeValues() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername("a".repeat(1000));
        user.setEmail("a".repeat(1000) + "@example.com");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithSpecialCharactersInUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser("user@domain.com");
        user.setEmail("user@domain.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("user@domain.com", savedUser.getUsername());
        assertEquals("user@domain.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithSpecialCharactersInEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail("user+tag@domain.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("user+tag@domain.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithUnicodeInUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser("测试用户");
        user.setEmail("test@测试.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("测试用户", savedUser.getUsername());
        assertEquals("test@测试.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithUnicodeInEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail("test@测试.com");
        
        // Act
        User savedUser = userRepository.save(user);
        
        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("test@测试.com", savedUser.getEmail());
    }
    
    @Test
    void testSave_WithExtremeLongUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername("a".repeat(1000));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithExtremeLongEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail("a".repeat(1000) + "@example.com");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithExtremeLongPassword() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setPassword("a".repeat(1000));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithNullUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithNullEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithNullPassword() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setPassword(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithEmptyUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithEmptyEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithEmptyPassword() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setPassword("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithWhitespaceUsername() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setUsername("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithWhitespaceEmail() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setEmail("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
    
    @Test
    void testSave_WithWhitespacePassword() {
        // Arrange
        User user = TestDataBuilder.createTestUser();
        user.setPassword("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user);
        });
    }
}
