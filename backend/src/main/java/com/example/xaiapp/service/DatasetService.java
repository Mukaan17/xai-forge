package com.example.xaiapp.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.example.xaiapp.dto.DatasetDto;
import com.example.xaiapp.entity.Dataset;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.repository.DatasetRepository;
import com.example.xaiapp.repository.UserRepository;
import com.example.xaiapp.exception.DatasetException;
import com.example.xaiapp.exception.DatasetNotFoundException;
import com.example.xaiapp.exception.DatasetParsingException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DatasetService {
    
    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DatasetDto storeFile(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new DatasetException("File is empty", "Please select a file to upload.");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new DatasetException("Only CSV files are allowed", "Please upload a CSV file.");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Parse CSV to get headers and row count
        List<String> headers = new ArrayList<>();
        long rowCount = 0;
        
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            headers = new ArrayList<>(csvParser.getHeaderNames());
            
            for (CSVRecord record : csvParser) {
                rowCount++;
            }
        }
        
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create dataset entity
        Dataset dataset = new Dataset();
        dataset.setFileName(originalFilename);
        dataset.setFilePath(filePath.toString());
        dataset.setHeaders(headers);
        dataset.setRowCount(rowCount);
        dataset.setOwner(user);
        
        Dataset savedDataset = datasetRepository.save(dataset);
        
        return convertToDto(savedDataset);
    }
    
    @Transactional(readOnly = true)
    public Optional<DatasetDto> getDataset(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<DatasetDto> listUserDatasets(Long userId) {
        return datasetRepository.findByOwnerId(userId)
            .stream()
            .map(this::convertToDto)
            .toList();
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteDataset(Long datasetId, Long userId) throws IOException {
        Optional<Dataset> datasetOpt = datasetRepository.findByIdAndOwnerId(datasetId, userId);
        if (datasetOpt.isPresent()) {
            Dataset dataset = datasetOpt.get();
            
            try {
                // Delete file from filesystem
                Path filePath = Paths.get(dataset.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                
                // Delete from database
                datasetRepository.delete(dataset);
            } catch (IOException e) {
                log.error("Failed to delete dataset file: {}", dataset.getFilePath(), e);
                throw new DatasetException("Failed to delete dataset file", e);
            }
        } else {
            throw new DatasetNotFoundException(datasetId);
        }
    }
    
    @Transactional(readOnly = true)
    public Dataset getDatasetEntity(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
    }
    
    private DatasetDto convertToDto(Dataset dataset) {
        DatasetDto dto = new DatasetDto();
        dto.setId(dataset.getId());
        dto.setFileName(dataset.getFileName());
        dto.setUploadDate(dataset.getUploadDate());
        dto.setHeaders(dataset.getHeaders());
        dto.setRowCount(dataset.getRowCount());
        dto.setOwnerId(dataset.getOwner().getId());
        return dto;
    }
}
