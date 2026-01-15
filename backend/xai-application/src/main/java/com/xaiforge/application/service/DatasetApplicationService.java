package com.xaiforge.application.service;

import com.xaiforge.common.dto.DatasetDto;
import com.xaiforge.common.dto.DatasetPreviewDto;
import com.xaiforge.common.dto.DatasetStatisticsDto;
import com.xaiforge.common.exception.DatasetNotFoundException;
import com.xaiforge.domain.dataset.entity.Dataset;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.domain.notification.entity.Notification;
import com.xaiforge.infrastructure.file.DatasetStatisticsCalculator;
import com.xaiforge.infrastructure.file.ExcelParser;
import com.xaiforge.application.service.NotificationApplicationService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class DatasetApplicationService {
    
    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;
    private final ExcelParser excelParser;
    private final DatasetStatisticsCalculator statisticsCalculator;
    private final NotificationApplicationService notificationService;
    
    @Value("${app.file.upload-dir}")
    private String uploadDir;
    
    public DatasetApplicationService(
            DatasetRepository datasetRepository, 
            UserRepository userRepository, 
            ExcelParser excelParser,
            DatasetStatisticsCalculator statisticsCalculator,
            NotificationApplicationService notificationService) {
        this.datasetRepository = datasetRepository;
        this.userRepository = userRepository;
        this.excelParser = excelParser;
        this.statisticsCalculator = statisticsCalculator;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public DatasetDto uploadDataset(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new com.xaiforge.common.exception.ValidationException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new com.xaiforge.common.exception.ValidationException("File name is null");
        }
        
        String lowerFilename = originalFilename.toLowerCase();
        boolean isExcel = lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls");
        boolean isCsv = lowerFilename.endsWith(".csv");
        
        if (!isCsv && !isExcel) {
            throw new com.xaiforge.common.exception.ValidationException("Only CSV, XLSX, and XLS files are allowed");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename (always save as CSV for model training compatibility)
        String uniqueFilename = UUID.randomUUID().toString() + ".csv";
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        List<String> headers;
        long rowCount;
        
        if (isExcel) {
            // Parse Excel file
            ExcelParser.ExcelParseResult excelResult = excelParser.parseExcel(file.getInputStream(), originalFilename);
            headers = excelResult.getHeaders();
            rowCount = excelResult.getRowCount();
            
            // Convert Excel to CSV and save
            String csvContent = excelParser.convertToCSV(excelResult.getSheet());
            Files.writeString(filePath, csvContent);
        } else {
            // Save CSV file as-is
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Parse CSV to get headers and row count
            headers = new java.util.ArrayList<>();
            rowCount = 0;
            
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
        
        // Create notification for successful upload
        notificationService.createNotification(
            userId,
            Notification.NotificationType.DATASET_UPLOADED,
            "Dataset Upload Complete",
            String.format("\"%s\" processed successfully", originalFilename),
            String.format("%d rows • %d features detected", rowCount, headers.size())
        );
        
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
    
    @Transactional(readOnly = true)
    public Page<DatasetDto> searchUserDatasets(Long userId, String search, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        Page<Dataset> datasets;
        
        if (dateFrom != null || dateTo != null) {
            datasets = datasetRepository.findByOwnerIdWithFilters(userId, search, dateFrom, dateTo, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            datasets = datasetRepository.findByOwnerIdWithSearch(userId, search.trim(), pageable);
        } else {
            // Simple pagination without filters
            datasets = datasetRepository.findByOwnerIdWithSearch(userId, null, pageable);
        }
        
        return datasets.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public DatasetPreviewDto previewDataset(Long datasetId, Long userId, int rows, int offset) throws IOException {
        Dataset dataset = datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
        
        Path filePath = Paths.get(dataset.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("Dataset file not found: " + filePath);
        }
        
        List<Map<String, String>> previewRows = new ArrayList<>();
        int totalRows = dataset.getRowCount() != null ? dataset.getRowCount().intValue() : 0;
        
        try (java.io.Reader reader = new java.io.FileReader(filePath.toFile());
             org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(
                 reader, 
                 org.apache.commons.csv.CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {
            
            List<String> headers = csvParser.getHeaderNames();
            int currentRow = 0;
            int rowsRead = 0;
            
            for (org.apache.commons.csv.CSVRecord record : csvParser) {
                if (currentRow >= offset && rowsRead < rows) {
                    Map<String, String> rowData = new HashMap<>();
                    for (String header : headers) {
                        rowData.put(header, record.get(header));
                    }
                    previewRows.add(rowData);
                    rowsRead++;
                }
                currentRow++;
                
                // Early exit if we've read enough rows
                if (rowsRead >= rows) {
                    break;
                }
            }
        }
        
        boolean hasMore = (offset + rows) < totalRows;
        
        return new DatasetPreviewDto(
            previewRows,
            totalRows,
            offset,
            rows,
            hasMore
        );
    }
    
    @Transactional(readOnly = true)
    public DatasetStatisticsDto getDatasetStatistics(Long datasetId, Long userId) throws IOException {
        Dataset dataset = datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
        
        Path filePath = Paths.get(dataset.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("Dataset file not found: " + filePath);
        }
        
        List<String> headers = dataset.getHeaders() != null ? dataset.getHeaders() : new ArrayList<>();
        return statisticsCalculator.calculateStatistics(filePath, headers);
    }
    
    @Transactional(readOnly = true)
    public Resource exportDataset(Long datasetId, Long userId) throws IOException {
        Dataset dataset = datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .orElseThrow(() -> new DatasetNotFoundException(datasetId));
        
        Path filePath = Paths.get(dataset.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("Dataset file not found: " + filePath);
        }
        
        return new FileSystemResource(filePath);
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

