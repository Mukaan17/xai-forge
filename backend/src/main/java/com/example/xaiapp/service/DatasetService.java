package com.example.xaiapp.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
@Slf4j
@Transactional
public class DatasetService {
    
    // Manual log field (Lombok @Slf4j not generating it)
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatasetService.class);
    
    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;
    
    // Manual constructor (Lombok @RequiredArgsConstructor not working with Java 24)
    public DatasetService(DatasetRepository datasetRepository, UserRepository userRepository) {
        this.datasetRepository = datasetRepository;
        this.userRepository = userRepository;
    }
    
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
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new DatasetException("File name is null", "Please provide a valid file name.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Parse CSV to get headers and row count
        List<String> headers = new ArrayList<>();
        long rowCount = 0;
        
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            headers = new ArrayList<>(csvParser.getHeaderNames());
            
            for (@SuppressWarnings("unused") CSVRecord record : csvParser) {
                rowCount++;
            }
        }
        
        // Note: Dataset type validation is now handled during model training
        // when the user specifies the target variable and model type
        
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
        
        return convertToDto(savedDataset, userId);
    }
    
    @Transactional(readOnly = true)
    public Optional<DatasetDto> getDataset(Long datasetId, Long userId) {
        return datasetRepository.findByIdAndOwnerId(datasetId, userId)
            .map(dataset -> {
                // Explicitly initialize headers collection to force Hibernate to load it
                if (dataset.getHeaders() != null) {
                    dataset.getHeaders().size(); // Force initialization
                }
                return convertToDto(dataset, userId);
            });
    }
    
    @Transactional(readOnly = true)
    public List<DatasetDto> listUserDatasets(Long userId) {
        return datasetRepository.findByOwnerId(userId)
            .stream()
            .map(dataset -> {
                // Explicitly initialize headers collection to force Hibernate to load it
                if (dataset.getHeaders() != null) {
                    dataset.getHeaders().size(); // Force initialization
                }
                return convertToDto(dataset, userId);
            })
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
    
    private DatasetDto convertToDto(Dataset dataset, Long ownerId) {
        DatasetDto dto = new DatasetDto();
        dto.setId(dataset.getId());
        dto.setFileName(dataset.getFileName());
        dto.setUploadDate(dataset.getUploadDate());
        dto.setHeaders(dataset.getHeaders());
        dto.setRowCount(dataset.getRowCount());
        // Use provided ownerId to avoid lazy loading issues
        dto.setOwnerId(ownerId);
        return dto;
    }
    
    
    /**
     * Automatically detect dataset type based on target column characteristics
     * 
     * This method analyzes the target column to determine if it's suitable for
     * regression (continuous numeric values) or classification (categorical/discrete values).
     * 
     * Detection logic:
     * - Classification: Non-numeric values, limited unique values (≤20 for small datasets, ≤50 for larger), low numeric ratio (<0.8)
     * - Regression: All numeric values, many unique values (continuous range), high numeric ratio (≥0.8)
     * 
     * @param filePath Path to the CSV file to analyze
     * @param targetColumn Name of the target column to analyze
     * @return "REGRESSION" or "CLASSIFICATION" based on data characteristics
     * @throws IOException if file cannot be read
     */
    public String detectDatasetType(Path filePath, String targetColumn) throws IOException {
        log.info("Detecting dataset type for target column: {}", targetColumn);
        
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            Set<String> uniqueValues = new HashSet<>();
            int numericCount = 0;
            int totalRows = 0;
            boolean hasNonNumericValues = false;
            
            for (CSVRecord record : csvParser) {
                totalRows++;
                String value = record.get(targetColumn);
                
                if (value != null && !value.trim().isEmpty()) {
                    uniqueValues.add(value.trim());
                    
                    // Check if value is numeric
                    try {
                        Double.parseDouble(value.trim());
                        numericCount++;
                    } catch (NumberFormatException e) {
                        hasNonNumericValues = true;
                    }
                }
            }
            
            // Decision logic for dataset type detection
            int uniqueCount = uniqueValues.size();
            double numericRatio = (double) numericCount / totalRows;
            
            log.info("Dataset analysis - Total rows: {}, Unique values: {}, Numeric ratio: {}", 
                    totalRows, uniqueCount, numericRatio);
            
            // Classification indicators:
            // 1. Non-numeric values present
            // 2. Limited unique values (≤20 for small datasets, ≤50 for larger datasets)
            // 3. Low numeric ratio (< 0.8)
            if (hasNonNumericValues || 
                (uniqueCount <= 20 && totalRows <= 1000) || 
                (uniqueCount <= 50 && totalRows > 1000) ||
                numericRatio < 0.8) {
                log.info("Detected CLASSIFICATION dataset - Non-numeric: {}, Unique: {}, Numeric ratio: {}", 
                        hasNonNumericValues, uniqueCount, numericRatio);
                return "CLASSIFICATION";
            }
            
            // Regression indicators:
            // 1. All numeric values
            // 2. Many unique values (continuous range)
            // 3. High numeric ratio (≥ 0.8)
            else {
                log.info("Detected REGRESSION dataset - All numeric: {}, Unique: {}, Numeric ratio: {}", 
                        !hasNonNumericValues, uniqueCount, numericRatio);
                return "REGRESSION";
            }
            
        } catch (Exception e) {
            log.warn("Error detecting dataset type, defaulting to REGRESSION: {}", e.getMessage());
            return "REGRESSION"; // Safe default
        }
    }
    
    /**
     * Validate regression dataset for data quality issues
     */
    private void validateRegressionDataset(Path filePath, List<String> headers) throws IOException {
        log.info("Validating regression dataset: {}", filePath);
        
        List<String> validationErrors = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            @SuppressWarnings("unchecked")
            List<Double>[] columnValues = new List[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                columnValues[i] = new ArrayList<>();
            }
            
            int rowNum = 0;
            for (CSVRecord record : csvParser) {
                rowNum++;
                
                // Check for missing values
                for (int i = 0; i < headers.size(); i++) {
                    String value = record.get(i);
                    if (value == null || value.trim().isEmpty()) {
                        validationErrors.add(String.format("Missing value in column '%s' at row %d", headers.get(i), rowNum));
                    } else {
                        try {
                            double numericValue = Double.parseDouble(value.trim());
                            columnValues[i].add(numericValue);
                        } catch (NumberFormatException e) {
                            validationErrors.add(String.format("Non-numeric value '%s' in column '%s' at row %d", value, headers.get(i), rowNum));
                        }
                    }
                }
            }
            
            // Check for outliers using IQR method
            for (int i = 0; i < headers.size(); i++) {
                if (!columnValues[i].isEmpty()) {
                    List<Double> sortedValues = new ArrayList<>(columnValues[i]);
                    sortedValues.sort(Double::compareTo);
                    
                    int size = sortedValues.size();
                    if (size >= 4) { // Need at least 4 values for IQR calculation
                        double q1 = sortedValues.get(size / 4);
                        double q3 = sortedValues.get(3 * size / 4);
                        double iqr = q3 - q1;
                        double lowerBound = q1 - 1.5 * iqr;
                        double upperBound = q3 + 1.5 * iqr;
                        
                        long outlierCount = sortedValues.stream()
                            .filter(v -> v < lowerBound || v > upperBound)
                            .count();
                        
                        if (outlierCount > size * 0.1) { // More than 10% outliers
                            validationErrors.add(String.format("Column '%s' has %d outliers (%.1f%% of data)", 
                                headers.get(i), outlierCount, (outlierCount * 100.0 / size)));
                        }
                    }
                }
            }
        }
        
        if (!validationErrors.isEmpty()) {
            String errorMessage = "Dataset validation failed: " + String.join("; ", validationErrors);
            log.warn("Dataset validation failed: {}", errorMessage);
            throw new DatasetParsingException(errorMessage);
        }
        
        log.info("Dataset validation passed");
    }
    
    /**
     * Validate classification dataset for data quality issues
     */
    private void validateClassificationDataset(Path filePath, List<String> headers) throws IOException {
        log.info("Validating classification dataset: {}", filePath);
        
        List<String> validationErrors = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            
            Map<String, Integer> classCounts = new HashMap<>();
            int totalRows = 0;
            
            for (CSVRecord record : csvParser) {
                totalRows++;
                
                // Check for missing values in all columns
                for (int i = 0; i < headers.size(); i++) {
                    String value = record.get(i);
                    if (value == null || value.trim().isEmpty()) {
                        validationErrors.add(String.format("Missing value in column '%s' at row %d", headers.get(i), totalRows));
                    }
                }
                
                // Count class occurrences (assuming last column is target)
                String targetValue = record.get(headers.size() - 1);
                if (targetValue != null && !targetValue.trim().isEmpty()) {
                    classCounts.put(targetValue.trim(), classCounts.getOrDefault(targetValue.trim(), 0) + 1);
                }
            }
            
            // Check class balance
            if (classCounts.size() < 2) {
                validationErrors.add("Classification requires at least 2 classes");
            } else {
                // Check for severe class imbalance (more than 90% in one class)
                int maxCount = classCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
                if (maxCount > totalRows * 0.9) {
                    validationErrors.add(String.format("Severe class imbalance detected: %d%% of data in one class", 
                        (maxCount * 100 / totalRows)));
                }
            }
            
            // Check for too many classes (more than 50)
            if (classCounts.size() > 50) {
                validationErrors.add(String.format("Too many classes (%d). Consider if this is a regression problem", classCounts.size()));
            }
        }
        
        if (!validationErrors.isEmpty()) {
            String errorMessage = "Classification dataset validation failed: " + String.join("; ", validationErrors);
            log.warn("Classification dataset validation failed: {}", errorMessage);
            throw new DatasetParsingException(errorMessage);
        }
        
        log.info("Classification dataset validation passed");
    }
}
