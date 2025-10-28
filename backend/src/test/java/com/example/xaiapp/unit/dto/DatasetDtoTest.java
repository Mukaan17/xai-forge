/**
 * @Author: Mukhil Sundararaj
 * @Date:   2025-01-15 10:00:00
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2025-01-15 10:00:00
 */
package com.example.xaiapp.unit.dto;

import com.example.xaiapp.dto.DatasetDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatasetDto
 * Tests constructors, Lombok methods, and all fields
 */
class DatasetDtoTest {

    @Test
    void testNoArgsConstructor_createsEmptyDto() {
        // Act
        DatasetDto dto = new DatasetDto();
        
        // Assert
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getFileName());
        assertNull(dto.getUploadDate());
        assertNull(dto.getHeaders());
        assertNull(dto.getRowCount());
        assertNull(dto.getOwnerId());
    }

    @Test
    void testAllArgsConstructor_createsCompleteDto() {
        // Arrange
        Long id = 1L;
        String fileName = "test.csv";
        LocalDateTime uploadDate = LocalDateTime.now();
        List<String> headers = Arrays.asList("col1", "col2", "col3");
        Long rowCount = 100L;
        Long ownerId = 2L;
        
        // Act
        DatasetDto dto = new DatasetDto(id, fileName, uploadDate, headers, rowCount, ownerId);
        
        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(fileName, dto.getFileName());
        assertEquals(uploadDate, dto.getUploadDate());
        assertEquals(headers, dto.getHeaders());
        assertEquals(rowCount, dto.getRowCount());
        assertEquals(ownerId, dto.getOwnerId());
    }

    @Test
    void testSetters_setAllFields() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        Long id = 3L;
        String fileName = "dataset.csv";
        LocalDateTime uploadDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        List<String> headers = Arrays.asList("feature1", "feature2");
        Long rowCount = 50L;
        Long ownerId = 4L;
        
        // Act
        dto.setId(id);
        dto.setFileName(fileName);
        dto.setUploadDate(uploadDate);
        dto.setHeaders(headers);
        dto.setRowCount(rowCount);
        dto.setOwnerId(ownerId);
        
        // Assert
        assertEquals(id, dto.getId());
        assertEquals(fileName, dto.getFileName());
        assertEquals(uploadDate, dto.getUploadDate());
        assertEquals(headers, dto.getHeaders());
        assertEquals(rowCount, dto.getRowCount());
        assertEquals(ownerId, dto.getOwnerId());
    }

    @Test
    void testEquals_sameObject_returnsTrue() {
        // Arrange
        DatasetDto dto = createTestDto();
        
        // Act & Assert
        assertEquals(dto, dto);
    }

    @Test
    void testEquals_equalObjects_returnsTrue() {
        // Arrange
        DatasetDto dto1 = createTestDto();
        DatasetDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1, dto2);
    }

    @Test
    void testEquals_differentObjects_returnsFalse() {
        // Arrange
        DatasetDto dto1 = createTestDto();
        DatasetDto dto2 = createTestDto();
        dto2.setFileName("different.csv");
        
        // Act & Assert
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testEquals_nullObject_returnsFalse() {
        // Arrange
        DatasetDto dto = createTestDto();
        
        // Act & Assert
        assertNotEquals(dto, null);
    }

    @Test
    void testEquals_differentClass_returnsFalse() {
        // Arrange
        DatasetDto dto = createTestDto();
        String other = "not a dto";
        
        // Act & Assert
        assertNotEquals(dto, other);
    }

    @Test
    void testHashCode_equalObjects_sameHashCode() {
        // Arrange
        DatasetDto dto1 = createTestDto();
        DatasetDto dto2 = createTestDto();
        
        // Act & Assert
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString_containsAllFields() {
        // Arrange
        DatasetDto dto = createTestDto();
        
        // Act
        String toString = dto.toString();
        
        // Assert
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("test.csv"));
        assertTrue(toString.contains("col1"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("2"));
    }

    @Test
    void testWithNullValues_handlesNullsCorrectly() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        
        // Act
        dto.setId(null);
        dto.setFileName(null);
        dto.setUploadDate(null);
        dto.setHeaders(null);
        dto.setRowCount(null);
        dto.setOwnerId(null);
        
        // Assert
        assertNull(dto.getId());
        assertNull(dto.getFileName());
        assertNull(dto.getUploadDate());
        assertNull(dto.getHeaders());
        assertNull(dto.getRowCount());
        assertNull(dto.getOwnerId());
    }

    @Test
    void testWithEmptyHeaders_handlesEmptyList() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        List<String> emptyHeaders = Collections.emptyList();
        
        // Act
        dto.setHeaders(emptyHeaders);
        
        // Assert
        assertEquals(emptyHeaders, dto.getHeaders());
        assertTrue(dto.getHeaders().isEmpty());
    }

    @Test
    void testWithZeroRowCount_handlesZero() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        
        // Act
        dto.setRowCount(0L);
        
        // Assert
        assertEquals(Long.valueOf(0), dto.getRowCount());
    }

    @Test
    void testWithNegativeId_handlesNegativeValues() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        
        // Act
        dto.setId(-1L);
        
        // Assert
        assertEquals(Long.valueOf(-1), dto.getId());
    }

    @Test
    void testWithLongFileName_handlesLongStrings() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        String longFileName = "very_long_dataset_name_with_many_characters_and_numbers_12345.csv";
        
        // Act
        dto.setFileName(longFileName);
        
        // Assert
        assertEquals(longFileName, dto.getFileName());
    }

    @Test
    void testWithManyHeaders_handlesLargeLists() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        List<String> manyHeaders = Arrays.asList(
            "header1", "header2", "header3", "header4", "header5",
            "header6", "header7", "header8", "header9", "header10"
        );
        
        // Act
        dto.setHeaders(manyHeaders);
        
        // Assert
        assertEquals(manyHeaders, dto.getHeaders());
        assertEquals(10, dto.getHeaders().size());
    }

    @Test
    void testWithMaxLongValues_handlesMaxValues() {
        // Arrange
        DatasetDto dto = new DatasetDto();
        
        // Act
        dto.setId(Long.MAX_VALUE);
        dto.setRowCount(Long.MAX_VALUE);
        dto.setOwnerId(Long.MAX_VALUE);
        
        // Assert
        assertEquals(Long.MAX_VALUE, dto.getId());
        assertEquals(Long.MAX_VALUE, dto.getRowCount());
        assertEquals(Long.MAX_VALUE, dto.getOwnerId());
    }

    private DatasetDto createTestDto() {
        return new DatasetDto(
            1L,
            "test.csv",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            Arrays.asList("col1", "col2", "col3"),
            100L,
            2L
        );
    }
}
