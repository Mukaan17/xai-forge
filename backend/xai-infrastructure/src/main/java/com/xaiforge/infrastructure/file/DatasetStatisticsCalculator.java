package com.xaiforge.infrastructure.file;

import com.xaiforge.common.dto.DatasetStatisticsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating dataset statistics and column analysis.
 */
@Component
public class DatasetStatisticsCalculator {
    private static final Logger log = LoggerFactory.getLogger(DatasetStatisticsCalculator.class);
    
    /**
     * Calculate comprehensive statistics for a dataset.
     */
    public DatasetStatisticsDto calculateStatistics(Path filePath, List<String> headers) throws IOException {
        List<DatasetStatisticsDto.ColumnStatistics> columnStats = new ArrayList<>();
        List<String> qualityIssues = new ArrayList<>();
        long totalRows = 0;
        Set<String> duplicateRows = new HashSet<>();
        Map<String, String> rowSignatures = new HashMap<>();
        
        // Read all data rows
        List<Map<String, String>> allRows = new ArrayList<>();
        try (java.io.Reader reader = new java.io.FileReader(filePath.toFile());
             org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(
                 reader,
                 org.apache.commons.csv.CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build())) {
            
            for (org.apache.commons.csv.CSVRecord record : csvParser) {
                Map<String, String> row = new HashMap<>();
                StringBuilder rowSignature = new StringBuilder();
                
                for (String header : headers) {
                    String value = record.get(header);
                    row.put(header, value);
                    rowSignature.append(value != null ? value : "").append("|");
                }
                
                String signature = rowSignature.toString();
                if (rowSignatures.containsKey(signature)) {
                    duplicateRows.add(signature);
                } else {
                    rowSignatures.put(signature, signature);
                }
                
                allRows.add(row);
                totalRows++;
            }
        }
        
        // Calculate statistics for each column
        for (String column : headers) {
            columnStats.add(calculateColumnStatistics(column, allRows, totalRows));
        }
        
        // Calculate quality metrics
        long totalMissingValues = columnStats.stream()
            .mapToLong(DatasetStatisticsDto.ColumnStatistics::nullCount)
            .sum();
        
        double qualityScore = calculateQualityScore(totalRows, totalMissingValues, duplicateRows.size(), headers.size());
        
        if (totalMissingValues > totalRows * 0.1) {
            qualityIssues.add("High percentage of missing values (>10%)");
        }
        if (duplicateRows.size() > totalRows * 0.05) {
            qualityIssues.add("Significant number of duplicate rows (>5%)");
        }
        if (headers.isEmpty()) {
            qualityIssues.add("No headers found");
        }
        
        DatasetStatisticsDto.DataQualityMetrics qualityMetrics = new DatasetStatisticsDto.DataQualityMetrics(
            qualityScore,
            totalMissingValues,
            (long) duplicateRows.size(),
            qualityIssues
        );
        
        return new DatasetStatisticsDto(
            totalRows,
            headers.size(),
            columnStats,
            qualityMetrics
        );
    }
    
    /**
     * Calculate statistics for a single column.
     */
    private DatasetStatisticsDto.ColumnStatistics calculateColumnStatistics(
            String columnName,
            List<Map<String, String>> rows,
            long totalRows) {
        
        List<String> values = rows.stream()
            .map(row -> row.get(columnName))
            .collect(Collectors.toList());
        
        // Count nulls and non-nulls
        long nullCount = values.stream().filter(v -> v == null || v.trim().isEmpty()).count();
        long nonNullCount = totalRows - nullCount;
        double nullPercentage = totalRows > 0 ? (nullCount * 100.0 / totalRows) : 0.0;
        
        // Get non-null values
        List<String> nonNullValues = values.stream()
            .filter(v -> v != null && !v.trim().isEmpty())
            .collect(Collectors.toList());
        
        // Count unique values
        long uniqueCount = nonNullValues.stream().distinct().count();
        
        // Detect data type
        String dataType = detectDataType(nonNullValues);
        
        // Calculate type-specific statistics
        Double min = null;
        Double max = null;
        Double mean = null;
        Double median = null;
        Double stdDev = null;
        List<DatasetStatisticsDto.ValueFrequency> topValues = null;
        Integer avgLength = null;
        Integer maxLength = null;
        
        if ("numeric".equals(dataType)) {
            List<Double> numericValues = nonNullValues.stream()
                .map(this::parseDouble)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
            
            if (!numericValues.isEmpty()) {
                min = numericValues.get(0);
                max = numericValues.get(numericValues.size() - 1);
                double calculatedMean = numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                mean = calculatedMean;
                
                if (numericValues.size() % 2 == 0) {
                    median = (numericValues.get(numericValues.size() / 2 - 1) + 
                             numericValues.get(numericValues.size() / 2)) / 2.0;
                } else {
                    median = numericValues.get(numericValues.size() / 2);
                }
                
                // Calculate standard deviation
                final double finalMean = calculatedMean;
                double variance = numericValues.stream()
                    .mapToDouble(v -> Math.pow(v - finalMean, 2))
                    .average()
                    .orElse(0.0);
                stdDev = Math.sqrt(variance);
            }
        } else if ("categorical".equals(dataType)) {
            // Calculate value frequencies
            Map<String, Long> valueCounts = nonNullValues.stream()
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));
            
            topValues = valueCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new DatasetStatisticsDto.ValueFrequency(
                    entry.getKey(),
                    entry.getValue(),
                    (entry.getValue() * 100.0 / nonNullCount)
                ))
                .collect(Collectors.toList());
        } else if ("text".equals(dataType)) {
            List<Integer> lengths = nonNullValues.stream()
                .map(String::length)
                .collect(Collectors.toList());
            
            if (!lengths.isEmpty()) {
                avgLength = (int) lengths.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                maxLength = lengths.stream().mapToInt(Integer::intValue).max().orElse(0);
            }
        }
        
        return new DatasetStatisticsDto.ColumnStatistics(
            columnName,
            dataType,
            nullCount,
            nonNullCount,
            uniqueCount,
            nullPercentage,
            min,
            max,
            mean,
            median,
            stdDev,
            topValues,
            avgLength,
            maxLength
        );
    }
    
    /**
     * Detect the data type of a column based on its values.
     */
    private String detectDataType(List<String> values) {
        if (values.isEmpty()) {
            return "unknown";
        }
        
        // Check if all values are numeric
        boolean allNumeric = values.stream()
            .allMatch(v -> {
                try {
                    Double.parseDouble(v);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
        
        if (allNumeric) {
            return "numeric";
        }
        
        // Check if all values are boolean
        boolean allBoolean = values.stream()
            .allMatch(v -> v.equalsIgnoreCase("true") || 
                          v.equalsIgnoreCase("false") ||
                          v.equals("1") || v.equals("0") ||
                          v.equalsIgnoreCase("yes") ||
                          v.equalsIgnoreCase("no"));
        
        if (allBoolean) {
            return "boolean";
        }
        
        // Check if values look like dates (simplified check)
        boolean looksLikeDate = values.stream()
            .limit(10) // Check first 10 values
            .anyMatch(v -> v.matches("\\d{4}-\\d{2}-\\d{2}.*") || 
                         v.matches("\\d{2}/\\d{2}/\\d{4}.*"));
        
        if (looksLikeDate) {
            return "date";
        }
        
        // Check if it's categorical (low unique count relative to total)
        long uniqueCount = values.stream().distinct().count();
        double uniqueRatio = (double) uniqueCount / values.size();
        
        if (uniqueRatio < 0.5 && uniqueCount < 50) {
            return "categorical";
        }
        
        // Check average length for text
        double avgLength = values.stream()
            .mapToInt(String::length)
            .average()
            .orElse(0.0);
        
        if (avgLength > 20) {
            return "text";
        }
        
        // Default to categorical if we can't determine
        return "categorical";
    }
    
    /**
     * Parse a string as double, returning null if not parseable.
     */
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Calculate overall data quality score (0-100).
     */
    private double calculateQualityScore(long totalRows, long missingValues, long duplicateRows, int columnCount) {
        if (totalRows == 0 || columnCount == 0) {
            return 0.0;
        }
        
        double completenessScore = 100.0 * (1.0 - (double) missingValues / (totalRows * columnCount));
        double uniquenessScore = 100.0 * (1.0 - (double) duplicateRows / totalRows);
        
        // Weighted average: 70% completeness, 30% uniqueness
        return (completenessScore * 0.7) + (uniquenessScore * 0.3);
    }
}
