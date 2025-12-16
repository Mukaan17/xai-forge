package com.example.xaiapp.unit.service;

import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.exception.DatasetParsingException;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.service.DatasetService;
import com.example.xaiapp.util.TestDataBuilder;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatasetService
 * Tests dataset upload, parsing, retrieval, and file operations
 */
@ExtendWith(MockitoExtension.class)
class DatasetServiceTest {
    
    @Mock
    private DatasetRepository datasetRepository;
    
    @InjectMocks
    private DatasetService datasetService;
    
    private User testUser;
    private Dataset testDataset;
    private MultipartFile testFile;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testDataset = TestDataBuilder.createTestDataset(testUser);
        
        // Create test CSV content
        String csvContent = TestDataBuilder.createTestCSVContent();
        testFile = new MockMultipartFile(
            "file",
            "test-dataset.csv",
            "text/csv",
            csvContent.getBytes()
        );
    }
    
    @Test
    void testUploadDataset_Success() throws Exception {
        // Arrange
        when(datasetRepository.save(any(Dataset.class)))
            .thenAnswer(invocation -> {
                Dataset dataset = invocation.getArgument(0);
                dataset.setId(1L);
                return dataset;
            });
        
        // Act
        com.example.xaiapp.dto.DatasetDto result = datasetService.storeFile(testFile, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals("test-dataset.csv", result.getFileName());
        assertEquals(testUser.getId(), result.getOwnerId());
        assertNotNull(result.getHeaders());
        assertTrue(result.getHeaders().contains("feature1"));
        assertTrue(result.getHeaders().contains("feature2"));
        assertTrue(result.getHeaders().contains("feature3"));
        assertTrue(result.getHeaders().contains("target"));
        
        verify(datasetRepository).save(any(Dataset.class));
    }
    
    @Test
    void testUploadDataset_EmptyFile() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            new byte[0]
        );
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(emptyFile, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_InvalidCSVFormat() {
        // Arrange
        String invalidCsv = "invalid,csv,format\nwithout,proper,structure";
        MultipartFile invalidFile = new MockMultipartFile(
            "file",
            "invalid.csv",
            "text/csv",
            invalidCsv.getBytes()
        );
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(invalidFile, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_NonCSVFile() {
        // Arrange
        MultipartFile nonCsvFile = new MockMultipartFile(
            "file",
            "document.txt",
            "text/plain",
            "This is not a CSV file".getBytes()
        );
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(nonCsvFile, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_LargeFile() throws IOException {
        // Arrange - Create a large CSV content
        StringBuilder largeCsv = new StringBuilder("feature1,feature2,feature3,target\n");
        for (int i = 0; i < 10000; i++) {
            largeCsv.append(i).append(",").append(i * 2).append(",").append(i * 3).append(",").append(i % 2).append("\n");
        }
        
        MultipartFile largeFile = new MockMultipartFile(
            "file",
            "large-dataset.csv",
            "text/csv",
            largeCsv.toString().getBytes()
        );
        
        when(datasetRepository.save(any(Dataset.class)))
            .thenAnswer(invocation -> {
                Dataset dataset = invocation.getArgument(0);
                dataset.setId(1L);
                return dataset;
            });
        
        // Act
        com.example.xaiapp.dto.DatasetDto result = datasetService.storeFile(largeFile, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(10000L, result.getRowCount());
        
        verify(datasetRepository).save(any(Dataset.class));
    }
    
    @Test
    void testUploadDataset_SpecialCharactersInFilename() throws Exception {
        // Arrange
        String specialFilename = "test_dataset_with_special-chars.csv";
        MultipartFile specialFile = new MockMultipartFile(
            "file",
            specialFilename,
            "text/csv",
            TestDataBuilder.createTestCSVContent().getBytes()
        );
        
        when(datasetRepository.save(any(Dataset.class)))
            .thenAnswer(invocation -> {
                Dataset dataset = invocation.getArgument(0);
                dataset.setId(1L);
                return dataset;
            });
        
        // Act
        com.example.xaiapp.dto.DatasetDto result = datasetService.storeFile(specialFile, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(specialFilename, result.getFileName());
        
        verify(datasetRepository).save(any(Dataset.class));
    }
    
    @Test
    void testGetUserDatasets_Success() {
        // Arrange
        List<Dataset> userDatasets = Arrays.asList(
            TestDataBuilder.createTestDataset(testUser, "dataset1.csv"),
            TestDataBuilder.createTestDataset(testUser, "dataset2.csv")
        );
        when(datasetRepository.findByOwnerId(testUser.getId()))
            .thenReturn(userDatasets);
        
        // Act
        List<com.example.xaiapp.dto.DatasetDto> result = datasetService.listUserDatasets(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("dataset1.csv", result.get(0).getFileName());
        assertEquals("dataset2.csv", result.get(1).getFileName());
        
        verify(datasetRepository).findByOwnerId(testUser.getId());
    }
    
    @Test
    void testGetUserDatasets_EmptyList() {
        // Arrange
        when(datasetRepository.findByOwnerId(testUser.getId()))
            .thenReturn(Arrays.asList());
        
        // Act
        List<com.example.xaiapp.dto.DatasetDto> result = datasetService.listUserDatasets(testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(datasetRepository).findByOwnerId(testUser.getId());
    }
    
    @Test
    void testGetDataset_Success() {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        
        // Act
        Optional<com.example.xaiapp.dto.DatasetDto> result = datasetService.getDataset(1L, testUser.getId());
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDataset.getId(), result.get().getId());
        assertEquals(testDataset.getFileName(), result.get().getFileName());
        
        verify(datasetRepository).findByIdAndOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testGetDataset_NotFound() {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            datasetService.getDataset(1L, testUser.getId());
        });
        
        verify(datasetRepository).findByIdAndOwnerId(1L, testUser.getId());
    }
    
    @Test
    void testDeleteDataset_Success() throws IOException {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.of(testDataset));
        
        // Act
        datasetService.deleteDataset(1L, testUser.getId());
        
        // Assert
        verify(datasetRepository).findByIdAndOwnerId(1L, testUser.getId());
        verify(datasetRepository).delete(testDataset);
    }
    
    @Test
    void testDeleteDataset_NotFound() {
        // Arrange
        when(datasetRepository.findByIdAndOwnerId(1L, testUser.getId()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            datasetService.deleteDataset(1L, testUser.getId());
        });
        
        verify(datasetRepository).findByIdAndOwnerId(1L, testUser.getId());
        verify(datasetRepository, never()).delete(any(Dataset.class));
    }
    
    @Test
    void testUploadDataset_NullFile() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            datasetService.storeFile(null, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_NullUserId() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            datasetService.storeFile(testFile, null);
        });
    }
    
    @Test
    void testUploadDataset_IOException() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
        when(mockFile.getContentType()).thenReturn("text/csv");
        when(mockFile.getBytes()).thenThrow(new IOException("File read error"));
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(mockFile, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_MissingHeaders() throws IOException {
        // Arrange
        String csvWithoutHeaders = "1.0,2.0,3.0,0\n2.0,3.0,4.0,1";
        MultipartFile fileWithoutHeaders = new MockMultipartFile(
            "file",
            "no-headers.csv",
            "text/csv",
            csvWithoutHeaders.getBytes()
        );
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(fileWithoutHeaders, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_InconsistentColumns() {
        // Arrange
        String inconsistentCsv = "feature1,feature2,feature3,target\n1.0,2.0,3.0,0\n2.0,3.0\n3.0,4.0,5.0,0";
        MultipartFile inconsistentFile = new MockMultipartFile(
            "file",
            "inconsistent.csv",
            "text/csv",
            inconsistentCsv.getBytes()
        );
        
        // Act & Assert
        assertThrows(DatasetParsingException.class, () -> {
            datasetService.storeFile(inconsistentFile, testUser.getId());
        });
    }
    
    @Test
    void testUploadDataset_UnicodeContent() throws Exception {
        // Arrange
        String unicodeCsv = "name,age,city\n" +
            "José,25,Madrid\n" +
            "François,30,Paris\n" +
            "李小明,28,北京";
        MultipartFile unicodeFile = new MockMultipartFile(
            "file",
            "unicode.csv",
            "text/csv",
            unicodeCsv.getBytes("UTF-8")
        );
        
        when(datasetRepository.save(any(Dataset.class)))
            .thenAnswer(invocation -> {
                Dataset dataset = invocation.getArgument(0);
                dataset.setId(1L);
                return dataset;
            });
        
        // Act
        com.example.xaiapp.dto.DatasetDto result = datasetService.storeFile(unicodeFile, testUser.getId());
        
        // Assert
        assertNotNull(result);
        assertEquals("unicode.csv", result.getFileName());
        assertTrue(result.getHeaders().contains("name"));
        assertTrue(result.getHeaders().contains("age"));
        assertTrue(result.getHeaders().contains("city"));
        
        verify(datasetRepository).save(any(Dataset.class));
    }
}
