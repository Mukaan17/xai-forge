package com.xaiforge.common.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for dataset statistics and column analysis.
 */
public record DatasetStatisticsDto(
    Long totalRows,
    Integer totalColumns,
    List<ColumnStatistics> columns,
    DataQualityMetrics qualityMetrics
) {
    /**
     * Statistics for a single column.
     */
    public record ColumnStatistics(
        String columnName,
        String dataType, // "numeric", "categorical", "text", "date", "boolean"
        Long nullCount,
        Long nonNullCount,
        Long uniqueCount,
        Double nullPercentage,
        // Numeric statistics (null if not numeric)
        Double min,
        Double max,
        Double mean,
        Double median,
        Double stdDev,
        // Categorical statistics (null if not categorical)
        List<ValueFrequency> topValues, // Top 10 most frequent values
        // Text statistics (null if not text)
        Integer avgLength,
        Integer maxLength
    ) {}
    
    /**
     * Value frequency for categorical columns.
     */
    public record ValueFrequency(
        String value,
        Long count,
        Double percentage
    ) {}
    
    /**
     * Overall data quality metrics.
     */
    public record DataQualityMetrics(
        Double overallQualityScore, // 0-100
        Long totalMissingValues,
        Long duplicateRows,
        List<String> qualityIssues // List of detected issues
    ) {}
}
