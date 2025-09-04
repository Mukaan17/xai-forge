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
import main.java.com.example.xaiapp.dto.DatasetDto;
import main.java.com.example.xaiapp.entity.Dataset;
import main.java.com.example.xaiapp.entity.User;
import main.java.com.example.xaiapp.repository.DatasetRepository;
import main.java.com.example.xaiapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {
    
    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public DatasetDto storeFile(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are allowed");
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
    
    public Optional<DatasetDto> getDataset(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .map(this::convertToDto);
    }
    
    public List<DatasetDto> listUserDatasets(Long userId) {
        return datasetRepository.findByOwnerId(userId)
            .stream()
            .map(this::convertToDto)
            .toList();
    }
    
    public void deleteDataset(Long datasetId, Long userId) throws IOException {
        Optional<Dataset> datasetOpt = datasetRepository.findByIdAndOwnerId(datasetId, userId);
        if (datasetOpt.isPresent()) {
            Dataset dataset = datasetOpt.get();
            
            // Delete file from filesystem
            Path filePath = Paths.get(dataset.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // Delete from database
            datasetRepository.delete(dataset);
        } else {
            throw new RuntimeException("Dataset not found or access denied");
        }
    }
    
    public Dataset getDatasetEntity(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new RuntimeException("Dataset not found or access denied"));
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
