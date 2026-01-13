package com.xaiforge.application.service;

import com.xaiforge.common.dto.DatasetDto;
import com.xaiforge.common.exception.DatasetNotFoundException;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.dataset.DatasetRepository;
import com.xaiforge.infrastructure.persistence.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DatasetApplicationService {
    
    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public DatasetApplicationService(DatasetRepository datasetRepository, UserRepository userRepository) {
        this.datasetRepository = datasetRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public DatasetDto uploadDataset(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new com.xaiforge.common.exception.ValidationException("File is empty");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new com.xaiforge.common.exception.ValidationException("Only CSV files are allowed");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new com.xaiforge.common.exception.ValidationException("File name is null");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Parse CSV to get headers and row count
        List<String> headers = new java.util.ArrayList<>();
        long rowCount = 0;
        
        try (java.io.Reader reader = new java.io.FileReader(filePath.toFile());
             org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(
                 reader, 
                 org.apache.commons.csv.CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {
            
            headers = new java.util.ArrayList<>(csvParser.getHeaderNames());
            
            for (@SuppressWarnings("unused") org.apache.commons.csv.CSVRecord record : csvParser) {
                rowCount++;
            }
        }
        
        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.xaiforge.common.exception.UserNotFoundException(userId));
        
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
    
    @Transactional
    public void deleteDataset(Long datasetId, Long userId) throws IOException {
        Dataset dataset = datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
        
        try {
            // Delete file from filesystem
            Path filePath = Paths.get(dataset.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // Delete from database
            datasetRepository.delete(dataset);
        } catch (IOException e) {
            throw new IOException("Failed to delete dataset file: " + e.getMessage(), e);
        }
    }
    
    private DatasetDto convertToDto(Dataset dataset) {
        return new DatasetDto(
            dataset.getId(),
            dataset.getFileName(),
            dataset.getUploadDate(),
            dataset.getHeaders(),
            dataset.getRowCount(),
            dataset.getOwner().getId()
        );
    }
}

