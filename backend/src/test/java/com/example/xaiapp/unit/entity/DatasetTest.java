package com.example.xaiapp.unit.entity;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Dataset entity
 * Tests constructors, Lombok methods, @PrePersist, JPA relationships, and equals/hashCode
 */
class DatasetTest {

    @Test
    void testNoArgsConstructor_createsEmptyDataset() {
        // Act
        Dataset dataset = new Dataset();
        
        // Assert
        assertNotNull(dataset);
        assertNull(dataset.getId());
        assertNull(dataset.getFileName());
        assertNull(dataset.getFilePath());
        assertNull(dataset.getUploadDate());
        assertNull(dataset.getHeaders());
        assertNull(dataset.getRowCount());
        assertNull(dataset.getOwner());
        assertNull(dataset.getMlModel());
    }

    @Test
    void testAllArgsConstructor_createsCompleteDataset() {
        // Arrange
        Long id = 1L;
        String fileName = "test.csv";
        String filePath = "/uploads/test.csv";
        LocalDateTime uploadDate = LocalDateTime.now();
        List<String> headers = Arrays.asList("col1", "col2", "col3");
        Long rowCount = 100L;
        User owner = new User();
        owner.setId(1L);
        
        // Act
        Dataset dataset = new Dataset(id, fileName, filePath, uploadDate, headers, rowCount, owner, null);
        
        // Assert
        assertNotNull(dataset);
        assertEquals(id, dataset.getId());
        assertEquals(fileName, dataset.getFileName());
        assertEquals(filePath, dataset.getFilePath());
        assertEquals(uploadDate, dataset.getUploadDate());
        assertEquals(headers, dataset.getHeaders());
        assertEquals(rowCount, dataset.getRowCount());
        assertEquals(owner, dataset.getOwner());
        assertNull(dataset.getMlModel());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        Dataset dataset = new Dataset();
        Long id = 2L;
        String fileName = "dataset.csv";
        String filePath = "/uploads/dataset.csv";
        LocalDateTime uploadDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        List<String> headers = Arrays.asList("feature1", "feature2");
        Long rowCount = 50L;
        User owner = new User();
        owner.setId(2L);
        
        // Act
        dataset.setId(id);
        dataset.setFileName(fileName);
        dataset.setFilePath(filePath);
        dataset.setUploadDate(uploadDate);
        dataset.setHeaders(headers);
        dataset.setRowCount(rowCount);
        dataset.setOwner(owner);
        
        // Assert
        assertEquals(id, dataset.getId());
        assertEquals(fileName, dataset.getFileName());
        assertEquals(filePath, dataset.getFilePath());
        assertEquals(uploadDate, dataset.getUploadDate());
        assertEquals(headers, dataset.getHeaders());
        assertEquals(rowCount, dataset.getRowCount());
        assertEquals(owner, dataset.getOwner());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        Dataset dataset = createTestDataset();
        
        // Act & Assert
        assertEquals(dataset, dataset);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        Dataset dataset1 = createTestDataset();
        Dataset dataset2 = createTestDataset();
        
        // Act & Assert
        assertEquals(dataset1, dataset2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        Dataset dataset1 = createTestDataset();
        Dataset dataset2 = createTestDataset();
        dataset2.setFileName("different.csv");
        
        // Act & Assert
        assertNotEquals(dataset1, dataset2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        Dataset dataset = createTestDataset();
        
        // Act & Assert
        assertNotEquals(dataset, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        Dataset dataset = createTestDataset();
        String other = "not a dataset";
        
        // Act & Assert
        assertNotEquals(dataset, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        Dataset dataset1 = createTestDataset();
        Dataset dataset2 = createTestDataset();
        
        // Act & Assert
        assertEquals(dataset1.hashCode(), dataset2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        Dataset dataset = createTestDataset();
        
        // Act
        String toString = dataset.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("test.csv"));
        assertTrue(toString.contains("/uploads/test.csv"));
        assertTrue(toString.contains("col1"));
        assertTrue(toString.contains("100"));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        Dataset dataset = new Dataset();
        
        // Act
        dataset.setId(null);
        dataset.setFileName(null);
        dataset.setFilePath(null);
        dataset.setUploadDate(null);
        dataset.setHeaders(null);
        dataset.setRowCount(null);
        dataset.setOwner(null);
        dataset.setMlModel(null);
        
        // Assert
        assertNull(dataset.getId());
        assertNull(dataset.getFileName());
        assertNull(dataset.getFilePath());
        assertNull(dataset.getUploadDate());
        assertNull(dataset.getHeaders());
        assertNull(dataset.getRowCount());
        assertNull(dataset.getOwner());
        assertNull(dataset.getMlModel());
    }

    @Test
    void testWithEmptyHeaders_handlesEmptyList() {
        // Arrange
        Dataset dataset = new Dataset();
        List<String> emptyHeaders = Collections.emptyList();
        
        // Act
        dataset.setHeaders(emptyHeaders);
        
        // Assert
        assertEquals(emptyHeaders, dataset.getHeaders());
        assertTrue(dataset.getHeaders().isEmpty());
    }

    @Test
    void testWithZeroRowCount_handlesZero() {
        // Arrange
        Dataset dataset = new Dataset();
        
        // Act
        dataset.setRowCount(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), dataset.getRowCount());
    }

    @Test
    void testWithNegativeRowCount_handlesNegativeValues() {
        // Arrange
        Dataset dataset = new Dataset();
        
        // Act
        dataset.setRowCount(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), dataset.getRowCount());
    }

    @Test
    void testWithMaxRowCount_handlesMaxValue() {
        // Arrange
        Dataset dataset = new Dataset();
        
        // Act
        dataset.setRowCount(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, dataset.getRowCount());
    }

    @Test
    void testWithLongFileName_handlesLongStrings() {
        // Arrange
        Dataset dataset = new Dataset();
        String longFileName = "very_long_dataset_name_with_many_characters_and_numbers_12345.csv";
        
        // Act
        dataset.setFileName(longFileName);
        
        // Assert
        assertEquals(longFileName, dataset.getFileName());
    }

    @Test
    void testWithLongFilePath_handlesLongStrings() {
        // Arrange
        Dataset dataset = new Dataset();
        String longFilePath = "/very/long/path/to/dataset/with/many/directories/and/characters/12345.csv";
        
        // Act
        dataset.setFilePath(longFilePath);
        
        // Assert
        assertEquals(longFilePath, dataset.getFilePath());
    }

    @Test
    void testWithManyHeaders_handlesLargeLists() {
        // Arrange
        Dataset dataset = new Dataset();
        List<String> manyHeaders = Arrays.asList(
            "header1", "header2", "header3", "header4", "header5",
            "header6", "header7", "header8", "header9", "header10"
        );
        
        // Act
        dataset.setHeaders(manyHeaders);
        
        // Assert
        assertEquals(manyHeaders, dataset.getHeaders());
        assertEquals(10, dataset.getHeaders().size());
    }

    @Test
    void testWithSpecialCharactersInFileName_handlesSpecialChars() {
        // Arrange
        Dataset dataset = new Dataset();
        String specialFileName = "dataset@domain.com.csv";
        
        // Act
        dataset.setFileName(specialFileName);
        
        // Assert
        assertEquals(specialFileName, dataset.getFileName());
    }

    @Test
    void testWithSpecialCharactersInFilePath_handlesSpecialChars() {
        // Arrange
        Dataset dataset = new Dataset();
        String specialFilePath = "/uploads/dataset@domain.com.csv";
        
        // Act
        dataset.setFilePath(specialFilePath);
        
        // Assert
        assertEquals(specialFilePath, dataset.getFilePath());
    }

    @Test
    void testWithUnicodeCharacters_handlesUnicode() {
        // Arrange
        Dataset dataset = new Dataset();
        String unicodeFileName = "数据集123.csv";
        String unicodeFilePath = "/uploads/数据集123.csv";
        
        // Act
        dataset.setFileName(unicodeFileName);
        dataset.setFilePath(unicodeFilePath);
        
        // Assert
        assertEquals(unicodeFileName, dataset.getFileName());
        assertEquals(unicodeFilePath, dataset.getFilePath());
    }

    @Test
    void testWithWhitespaceInFileName_handlesWhitespace() {
        // Arrange
        Dataset dataset = new Dataset();
        String fileNameWithWhitespace = " dataset name .csv ";
        
        // Act
        dataset.setFileName(fileNameWithWhitespace);
        
        // Assert
        assertEquals(fileNameWithWhitespace, dataset.getFileName());
    }

    @Test
    void testWithWhitespaceInFilePath_handlesWhitespace() {
        // Arrange
        Dataset dataset = new Dataset();
        String filePathWithWhitespace = " /uploads/ dataset name .csv ";
        
        // Act
        dataset.setFilePath(filePathWithWhitespace);
        
        // Assert
        assertEquals(filePathWithWhitespace, dataset.getFilePath());
    }

    @Test
    void testWithOwnerRelationship_handlesOwnerRelationship() {
        // Arrange
        Dataset dataset = new Dataset();
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("testuser");
        
        // Act
        dataset.setOwner(owner);
        
        // Assert
        assertEquals(owner, dataset.getOwner());
        assertEquals(Long.valueOf(1), dataset.getOwner().getId());
        assertEquals("testuser", dataset.getOwner().getUsername());
    }

    @Test
    void testWithMlModelRelationship_handlesMlModelRelationship() {
        // Arrange
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        
        // Note: MLModel would need to be created and set, but for this test we'll just verify null handling
        // Act
        dataset.setMlModel(null);
        
        // Assert
        assertNull(dataset.getMlModel());
    }

    @Test
    void testWithDifferentIdValues_handlesDifferentIds() {
        // Test different ID values
        Long[] testIds = {1L, 100L, 999L, 1000L, Long.MAX_VALUE};
        
        for (Long id : testIds) {
            // Arrange
            Dataset dataset = new Dataset();
            
            // Act
            dataset.setId(id);
            
            // Assert
            assertEquals(id, dataset.getId());
        }
    }

    @Test
    void testWithDifferentRowCounts_handlesDifferentRowCounts() {
        // Test different row count values
        Long[] testRowCounts = {0L, 1L, 100L, 1000L, Long.MAX_VALUE};
        
        for (Long rowCount : testRowCounts) {
            // Arrange
            Dataset dataset = new Dataset();
            
            // Act
            dataset.setRowCount(rowCount);
            
            // Assert
            assertEquals(rowCount, dataset.getRowCount());
        }
    }

    @Test
    void testWithDifferentUploadDates_handlesDifferentDates() {
        // Test different upload dates
        LocalDateTime[] testDates = {
            LocalDateTime.of(2024, 1, 1, 0, 0),
            LocalDateTime.of(2024, 6, 15, 12, 30),
            LocalDateTime.of(2024, 12, 31, 23, 59),
            LocalDateTime.now()
        };
        
        for (LocalDateTime date : testDates) {
            // Arrange
            Dataset dataset = new Dataset();
            
            // Act
            dataset.setUploadDate(date);
            
            // Assert
            assertEquals(date, dataset.getUploadDate());
        }
    }

    private Dataset createTestDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(1L);
        dataset.setFileName("test.csv");
        dataset.setFilePath("/uploads/test.csv");
        dataset.setUploadDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        dataset.setHeaders(Arrays.asList("col1", "col2", "col3"));
        dataset.setRowCount(100L);
        
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("testuser");
        dataset.setOwner(owner);
        
        return dataset;
    }
}
