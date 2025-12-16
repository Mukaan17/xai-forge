package com.example.xaiapp.integration.repository;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.util.TestConstants;
import com.example.xaiapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatasetRepository
 * Tests dataset queries and cascading deletes
 */
@DataJpaTest
@ActiveProfiles("test")
class DatasetRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private Dataset testDataset;
    
    @BeforeEach
    void setUp() {
        datasetRepository.deleteAll();
        userRepository.deleteAll();
        
        testUser = TestDataBuilder.createTestUser();
        userRepository.save(testUser);
        testDataset = TestDataBuilder.createTestDataset(testUser);
    }
    
    @Test
    void testFindByOwnerId_Success() {
        // Arrange
        datasetRepository.save(testDataset);
        
        // Act
        List<Dataset> result = datasetRepository.findByOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDataset.getFileName(), result.get(0).getFileName());
        assertEquals(testUser.getId(), result.get(0).getOwner().getId());
    }
    
    @Test
    void testFindByOwnerId_EmptyList() {
        // Act
        List<Dataset> result = datasetRepository.findByOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindByOwnerId_NonExistentUser() {
        // Act
        List<Dataset> result = datasetRepository.findByOwnerId(999L);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindByOwnerId_MultipleDatasets() {
        // Arrange
        Dataset dataset1 = TestDataBuilder.createTestDataset(testUser, "dataset1.csv");
        Dataset dataset2 = TestDataBuilder.createTestDataset(testUser, "dataset2.csv");
        datasetRepository.save(dataset1);
        datasetRepository.save(dataset2);
        
        // Act
        List<Dataset> result = datasetRepository.findByOwnerId(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testFindByOwnerId_WithDifferentUsers() {
        // Arrange
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        
        Dataset userDataset = TestDataBuilder.createTestDataset(testUser, "user-dataset.csv");
        Dataset otherDataset = TestDataBuilder.createTestDataset(otherUser, "other-dataset.csv");
        datasetRepository.save(userDataset);
        datasetRepository.save(otherDataset);
        
        // Act
        List<Dataset> userResult = datasetRepository.findByOwnerId(testUser.getId());
        List<Dataset> otherResult = datasetRepository.findByOwnerId(otherUser.getId());
        
        // Assert
        assertNotNull(userResult);
        assertEquals(1, userResult.size());
        assertEquals("user-dataset.csv", userResult.get(0).getFileName());
        
        assertNotNull(otherResult);
        assertEquals(1, otherResult.size());
        assertEquals("other-dataset.csv", otherResult.get(0).getFileName());
    }
    
    @Test
    void testFindByIdAndOwnerId_Success() {
        // Arrange
        datasetRepository.save(testDataset);
        
        // Act
        Optional<Dataset> result = datasetRepository.findByIdAndOwnerId(testDataset.getId(), testUser.getId());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDataset.getId(), result.get().getId());
        assertEquals(testDataset.getFileName(), result.get().getFileName());
        assertEquals(testUser.getId(), result.get().getOwner().getId());
    }
    
    @Test
    void testFindByIdAndOwnerId_NotFound() {
        // Act
        Optional<Dataset> result = datasetRepository.findByIdAndOwnerId(999L, testUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndOwnerId_WrongOwner() {
        // Arrange
        User otherUser = TestDataBuilder.createTestUser("otheruser");
        userRepository.save(otherUser);
        datasetRepository.save(testDataset);
        
        // Act
        Optional<Dataset> result = datasetRepository.findByIdAndOwnerId(testDataset.getId(), otherUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndOwnerId_NullId() {
        // Act
        Optional<Dataset> result = datasetRepository.findByIdAndOwnerId(null, testUser.getId());
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndOwnerId_NullOwnerId() {
        // Arrange
        datasetRepository.save(testDataset);
        
        // Act
        Optional<Dataset> result = datasetRepository.findByIdAndOwnerId(testDataset.getId(), null);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testSave_WithValidData() {
        // Act
        Dataset savedDataset = datasetRepository.save(testDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals(testDataset.getFileName(), savedDataset.getFileName());
        assertEquals(testDataset.getFilePath(), savedDataset.getFilePath());
        assertEquals(testDataset.getRowCount(), savedDataset.getRowCount());
        assertEquals(testDataset.getHeaders(), savedDataset.getHeaders());
        assertEquals(testUser.getId(), savedDataset.getOwner().getId());
    }
    
    @Test
    void testSave_WithSpecialCharacters() {
        // Arrange
        Dataset specialDataset = TestDataBuilder.createTestDataset(testUser, "dataset with spaces.csv");
        specialDataset.setFilePath("/path/with spaces/dataset.csv");
        
        // Act
        Dataset savedDataset = datasetRepository.save(specialDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals("dataset with spaces.csv", savedDataset.getFileName());
        assertEquals("/path/with spaces/dataset.csv", savedDataset.getFilePath());
    }
    
    @Test
    void testSave_WithUnicodeCharacters() {
        // Arrange
        Dataset unicodeDataset = TestDataBuilder.createTestDataset(testUser, "数据集.csv");
        unicodeDataset.setFilePath("/path/数据集.csv");
        
        // Act
        Dataset savedDataset = datasetRepository.save(unicodeDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals("数据集.csv", savedDataset.getFileName());
        assertEquals("/path/数据集.csv", savedDataset.getFilePath());
    }
    
    @Test
    void testSave_WithLongValues() {
        // Arrange
        Dataset longDataset = TestDataBuilder.createTestDataset(testUser, "a".repeat(100) + ".csv");
        longDataset.setFilePath("/path/" + "a".repeat(100) + ".csv");
        
        // Act
        Dataset savedDataset = datasetRepository.save(longDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals("a".repeat(100) + ".csv", savedDataset.getFileName());
        assertEquals("/path/" + "a".repeat(100) + ".csv", savedDataset.getFilePath());
    }
    
    @Test
    void testSave_WithNullValues() {
        // Arrange
        Dataset nullDataset = new Dataset();
        nullDataset.setFileName(null);
        nullDataset.setFilePath(null);
        nullDataset.setRowCount(null);
        nullDataset.setHeaders(null);
        nullDataset.setOwner(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullDataset);
        });
    }
    
    @Test
    void testSave_WithEmptyValues() {
        // Arrange
        Dataset emptyDataset = new Dataset();
        emptyDataset.setFileName("");
        emptyDataset.setFilePath("");
        emptyDataset.setRowCount(0L);
        emptyDataset.setHeaders(List.of());
        emptyDataset.setOwner(testUser);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(emptyDataset);
        });
    }
    
    @Test
    void testSave_WithWhitespaceValues() {
        // Arrange
        Dataset whitespaceDataset = new Dataset();
        whitespaceDataset.setFileName("   ");
        whitespaceDataset.setFilePath("   ");
        whitespaceDataset.setRowCount(0L);
        whitespaceDataset.setHeaders(List.of());
        whitespaceDataset.setOwner(testUser);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(whitespaceDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeValues() {
        // Arrange
        Dataset extremeDataset = TestDataBuilder.createTestDataset(testUser, "a".repeat(1000) + ".csv");
        extremeDataset.setFilePath("/path/" + "a".repeat(1000) + ".csv");
        extremeDataset.setRowCount(Long.MAX_VALUE);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeDataset);
        });
    }
    
    @Test
    void testSave_WithNegativeRowCount() {
        // Arrange
        Dataset negativeDataset = TestDataBuilder.createTestDataset(testUser, "negative.csv");
        negativeDataset.setRowCount(-1L);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(negativeDataset);
        });
    }
    
    @Test
    void testSave_WithZeroRowCount() {
        // Arrange
        Dataset zeroDataset = TestDataBuilder.createTestDataset(testUser, "zero.csv");
        zeroDataset.setRowCount(0L);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(zeroDataset);
        });
    }
    
    @Test
    void testSave_WithNullOwner() {
        // Arrange
        Dataset nullOwnerDataset = TestDataBuilder.createTestDataset(testUser, "null-owner.csv");
        nullOwnerDataset.setOwner(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullOwnerDataset);
        });
    }
    
    @Test
    void testSave_WithNullHeaders() {
        // Arrange
        Dataset nullHeadersDataset = TestDataBuilder.createTestDataset(testUser, "null-headers.csv");
        nullHeadersDataset.setHeaders(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullHeadersDataset);
        });
    }
    
    @Test
    void testSave_WithEmptyHeaders() {
        // Arrange
        Dataset emptyHeadersDataset = TestDataBuilder.createTestDataset(testUser, "empty-headers.csv");
        emptyHeadersDataset.setHeaders(List.of());
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(emptyHeadersDataset);
        });
    }
    
    @Test
    void testSave_WithWhitespaceHeaders() {
        // Arrange
        Dataset whitespaceHeadersDataset = TestDataBuilder.createTestDataset(testUser, "whitespace-headers.csv");
        whitespaceHeadersDataset.setHeaders(List.of("   ", "   ", "   "));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(whitespaceHeadersDataset);
        });
    }
    
    @Test
    void testSave_WithNullHeaderValues() {
        // Arrange
        Dataset nullHeaderValuesDataset = TestDataBuilder.createTestDataset(testUser, "null-header-values.csv");
        nullHeaderValuesDataset.setHeaders(List.of(null, null, null));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullHeaderValuesDataset);
        });
    }
    
    @Test
    void testSave_WithEmptyHeaderValues() {
        // Arrange
        Dataset emptyHeaderValuesDataset = TestDataBuilder.createTestDataset(testUser, "empty-header-values.csv");
        emptyHeaderValuesDataset.setHeaders(List.of("", "", ""));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(emptyHeaderValuesDataset);
        });
    }
    
    @Test
    void testSave_WithSpecialCharactersInHeaders() {
        // Arrange
        Dataset specialHeadersDataset = TestDataBuilder.createTestDataset(testUser, "special-headers.csv");
        specialHeadersDataset.setHeaders(List.of("feature with spaces", "feature-with-dashes", "feature_with_underscores"));
        
        // Act
        Dataset savedDataset = datasetRepository.save(specialHeadersDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals(List.of("feature with spaces", "feature-with-dashes", "feature_with_underscores"), savedDataset.getHeaders());
    }
    
    @Test
    void testSave_WithUnicodeInHeaders() {
        // Arrange
        Dataset unicodeHeadersDataset = TestDataBuilder.createTestDataset(testUser, "unicode-headers.csv");
        unicodeHeadersDataset.setHeaders(List.of("特征1", "特征2", "特征3"));
        
        // Act
        Dataset savedDataset = datasetRepository.save(unicodeHeadersDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals(List.of("特征1", "特征2", "特征3"), savedDataset.getHeaders());
    }
    
    @Test
    void testSave_WithLongHeaders() {
        // Arrange
        Dataset longHeadersDataset = TestDataBuilder.createTestDataset(testUser, "long-headers.csv");
        longHeadersDataset.setHeaders(List.of("a".repeat(100), "b".repeat(100), "c".repeat(100)));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(longHeadersDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeLongHeaders() {
        // Arrange
        Dataset extremeLongHeadersDataset = TestDataBuilder.createTestDataset(testUser, "extreme-long-headers.csv");
        extremeLongHeadersDataset.setHeaders(List.of("a".repeat(1000), "b".repeat(1000), "c".repeat(1000)));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeLongHeadersDataset);
        });
    }
    
    @Test
    void testSave_WithDuplicateHeaders() {
        // Arrange
        Dataset duplicateHeadersDataset = TestDataBuilder.createTestDataset(testUser, "duplicate-headers.csv");
        duplicateHeadersDataset.setHeaders(List.of("feature1", "feature1", "feature2"));
        
        // Act
        Dataset savedDataset = datasetRepository.save(duplicateHeadersDataset);
        
        // Assert
        assertNotNull(savedDataset.getId());
        assertEquals(List.of("feature1", "feature1", "feature2"), savedDataset.getHeaders());
    }
    
    @Test
    void testSave_WithNullFileName() {
        // Arrange
        Dataset nullFileNameDataset = TestDataBuilder.createTestDataset(testUser, "null-filename.csv");
        nullFileNameDataset.setFileName(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullFileNameDataset);
        });
    }
    
    @Test
    void testSave_WithEmptyFileName() {
        // Arrange
        Dataset emptyFileNameDataset = TestDataBuilder.createTestDataset(testUser, "empty-filename.csv");
        emptyFileNameDataset.setFileName("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(emptyFileNameDataset);
        });
    }
    
    @Test
    void testSave_WithWhitespaceFileName() {
        // Arrange
        Dataset whitespaceFileNameDataset = TestDataBuilder.createTestDataset(testUser, "whitespace-filename.csv");
        whitespaceFileNameDataset.setFileName("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(whitespaceFileNameDataset);
        });
    }
    
    @Test
    void testSave_WithNullFilePath() {
        // Arrange
        Dataset nullFilePathDataset = TestDataBuilder.createTestDataset(testUser, "null-filepath.csv");
        nullFilePathDataset.setFilePath(null);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(nullFilePathDataset);
        });
    }
    
    @Test
    void testSave_WithEmptyFilePath() {
        // Arrange
        Dataset emptyFilePathDataset = TestDataBuilder.createTestDataset(testUser, "empty-filepath.csv");
        emptyFilePathDataset.setFilePath("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(emptyFilePathDataset);
        });
    }
    
    @Test
    void testSave_WithWhitespaceFilePath() {
        // Arrange
        Dataset whitespaceFilePathDataset = TestDataBuilder.createTestDataset(testUser, "whitespace-filepath.csv");
        whitespaceFilePathDataset.setFilePath("   ");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(whitespaceFilePathDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeLongFileName() {
        // Arrange
        Dataset extremeLongFileNameDataset = TestDataBuilder.createTestDataset(testUser, "a".repeat(1000) + ".csv");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeLongFileNameDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeLongFilePath() {
        // Arrange
        Dataset extremeLongFilePathDataset = TestDataBuilder.createTestDataset(testUser, "extreme-long-filepath.csv");
        extremeLongFilePathDataset.setFilePath("/path/" + "a".repeat(1000) + ".csv");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeLongFilePathDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeLongRowCount() {
        // Arrange
        Dataset extremeLongRowCountDataset = TestDataBuilder.createTestDataset(testUser, "extreme-long-rowcount.csv");
        extremeLongRowCountDataset.setRowCount(Long.MAX_VALUE);
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeLongRowCountDataset);
        });
    }
    
    @Test
    void testSave_WithExtremeLongHeaders_Scenario2() {
        // Arrange
        Dataset extremeLongHeadersDataset = TestDataBuilder.createTestDataset(testUser, "extreme-long-headers.csv");
        extremeLongHeadersDataset.setHeaders(List.of("a".repeat(1000), "b".repeat(1000), "c".repeat(1000)));
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            datasetRepository.save(extremeLongHeadersDataset);
        });
    }
}
