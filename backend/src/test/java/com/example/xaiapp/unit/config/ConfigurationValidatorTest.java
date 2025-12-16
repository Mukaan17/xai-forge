package com.example.xaiapp.unit.config;

import com.example.xaiapp.config.ConfigurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationValidator
 * Tests startup validation for critical configuration parameters
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {
    
    @InjectMocks
    private ConfigurationValidator configurationValidator;
    
    @BeforeEach
    void setUp() {
        // Reset any static state if needed
    }
    
    @Test
    void testValidateJwtSecret_ValidSecret() {
        // Arrange
        String validSecret = "test-jwt-secret-key-for-testing-purposes-only-32-chars";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", validSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_NullSecret() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", (String) null);
        });
    }
    
    @Test
    void testValidateJwtSecret_EmptySecret() {
        // Arrange
        String emptySecret = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", emptySecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_ShortSecret() {
        // Arrange
        String shortSecret = "short";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", shortSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_ExactlyMinimumLength() {
        // Arrange
        String minimumSecret = "a".repeat(32); // Exactly 32 characters
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", minimumSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_OneCharacterShort() {
        // Arrange
        String shortSecret = "a".repeat(31); // One character short of minimum
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", shortSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_WhitespaceOnly() {
        // Arrange
        String whitespaceSecret = "   ";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", whitespaceSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_WithSpecialCharacters() {
        // Arrange
        String specialSecret = "test-secret-with-special-chars-@#$%^&*()";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", specialSecret);
        });
    }
    
    @Test
    void testValidateJwtSecret_WithUnicodeCharacters() {
        // Arrange
        String unicodeSecret = "test-secret-with-unicode-测试-32-chars";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateJwtSecret", unicodeSecret);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_ValidUrl() {
        // Arrange
        String validUrl = "jdbc:postgresql://localhost:5432/xai_db";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", validUrl);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_NullUrl() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", (String) null);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_EmptyUrl() {
        // Arrange
        String emptyUrl = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", emptyUrl);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_InvalidUrl() {
        // Arrange
        String invalidUrl = "invalid-url";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", invalidUrl);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_WithPort() {
        // Arrange
        String urlWithPort = "jdbc:postgresql://localhost:5432/xai_db";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", urlWithPort);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_WithoutPort() {
        // Arrange
        String urlWithoutPort = "jdbc:postgresql://localhost/xai_db";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", urlWithoutPort);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_WithCredentials() {
        // Arrange
        String urlWithCredentials = "jdbc:postgresql://user:password@localhost:5432/xai_db";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", urlWithCredentials);
        });
    }
    
    @Test
    void testValidateDatabaseUrl_WithParameters() {
        // Arrange
        String urlWithParams = "jdbc:postgresql://localhost:5432/xai_db?ssl=true&timeout=30";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateDatabaseUrl", urlWithParams);
        });
    }
    
    @Test
    void testValidateFilePaths_ValidPaths() {
        // Arrange
        String validUploadDir = "./uploads";
        String validDatasetsDir = "./uploads/datasets";
        String validModelsDir = "./uploads/models";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                validUploadDir, validDatasetsDir, validModelsDir);
        });
    }
    
    @Test
    void testValidateFilePaths_NullUploadDir() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                null, "./uploads/datasets", "./uploads/models");
        });
    }
    
    @Test
    void testValidateFilePaths_EmptyUploadDir() {
        // Arrange
        String emptyUploadDir = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                emptyUploadDir, "./uploads/datasets", "./uploads/models");
        });
    }
    
    @Test
    void testValidateFilePaths_InvalidPaths() {
        // Arrange
        String invalidPath = "/nonexistent/invalid/path";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                invalidPath, "./uploads/datasets", "./uploads/models");
        });
    }
    
    @Test
    void testValidateFilePaths_RelativePaths() {
        // Arrange
        String relativeUploadDir = "uploads";
        String relativeDatasetsDir = "uploads/datasets";
        String relativeModelsDir = "uploads/models";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                relativeUploadDir, relativeDatasetsDir, relativeModelsDir);
        });
    }
    
    @Test
    void testValidateFilePaths_AbsolutePaths() {
        // Arrange
        String absoluteUploadDir = "/tmp/uploads";
        String absoluteDatasetsDir = "/tmp/uploads/datasets";
        String absoluteModelsDir = "/tmp/uploads/models";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                absoluteUploadDir, absoluteDatasetsDir, absoluteModelsDir);
        });
    }
    
    @Test
    void testValidateFilePaths_WithSpecialCharacters() {
        // Arrange
        String specialUploadDir = "./uploads with spaces";
        String specialDatasetsDir = "./uploads with spaces/datasets";
        String specialModelsDir = "./uploads with spaces/models";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                specialUploadDir, specialDatasetsDir, specialModelsDir);
        });
    }
    
    @Test
    void testValidateFilePaths_WithUnicodeCharacters() {
        // Arrange
        String unicodeUploadDir = "./uploads测试";
        String unicodeDatasetsDir = "./uploads测试/datasets";
        String unicodeModelsDir = "./uploads测试/models";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateFilePaths", 
                unicodeUploadDir, unicodeDatasetsDir, unicodeModelsDir);
        });
    }
    
    @Test
    void testValidateConfiguration_ValidConfiguration() {
        // Arrange
        String validJwtSecret = "test-jwt-secret-key-for-testing-purposes-only-32-chars";
        String validDbUrl = "jdbc:postgresql://localhost:5432/xai_db";
        String validUploadDir = "./uploads";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                validJwtSecret, validDbUrl, validUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_InvalidJwtSecret() {
        // Arrange
        String invalidJwtSecret = "short";
        String validDbUrl = "jdbc:postgresql://localhost:5432/xai_db";
        String validUploadDir = "./uploads";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                invalidJwtSecret, validDbUrl, validUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_InvalidDatabaseUrl() {
        // Arrange
        String validJwtSecret = "test-jwt-secret-key-for-testing-purposes-only-32-chars";
        String invalidDbUrl = "invalid-url";
        String validUploadDir = "./uploads";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                validJwtSecret, invalidDbUrl, validUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_InvalidUploadDir() {
        // Arrange
        String validJwtSecret = "test-jwt-secret-key-for-testing-purposes-only-32-chars";
        String validDbUrl = "jdbc:postgresql://localhost:5432/xai_db";
        String invalidUploadDir = "/nonexistent/path";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                validJwtSecret, validDbUrl, invalidUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_AllInvalid() {
        // Arrange
        String invalidJwtSecret = "short";
        String invalidDbUrl = "invalid-url";
        String invalidUploadDir = "/nonexistent/path";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                invalidJwtSecret, invalidDbUrl, invalidUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_NullValues() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                null, null, null);
        });
    }
    
    @Test
    void testValidateConfiguration_EmptyValues() {
        // Arrange
        String emptyJwtSecret = "";
        String emptyDbUrl = "";
        String emptyUploadDir = "";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                emptyJwtSecret, emptyDbUrl, emptyUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_WhitespaceValues() {
        // Arrange
        String whitespaceJwtSecret = "   ";
        String whitespaceDbUrl = "   ";
        String whitespaceUploadDir = "   ";
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                whitespaceJwtSecret, whitespaceDbUrl, whitespaceUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_ExtremeValues() {
        // Arrange
        String extremeJwtSecret = "a".repeat(1000); // Very long secret
        String extremeDbUrl = "jdbc:postgresql://localhost:5432/xai_db?param=" + "x".repeat(1000);
        String extremeUploadDir = "./uploads/" + "x".repeat(1000);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                extremeJwtSecret, extremeDbUrl, extremeUploadDir);
        });
    }
    
    @Test
    void testValidateConfiguration_EdgeCaseValues() {
        // Arrange
        String edgeJwtSecret = "a".repeat(32); // Exactly minimum length
        String edgeDbUrl = "jdbc:postgresql://localhost/xai_db"; // Without port
        String edgeUploadDir = "uploads"; // Relative path
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(configurationValidator, "validateConfiguration", 
                edgeJwtSecret, edgeDbUrl, edgeUploadDir);
        });
    }
}
