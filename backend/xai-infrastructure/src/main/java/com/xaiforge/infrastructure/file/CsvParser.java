package com.xaiforge.infrastructure.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for parsing CSV files
 * 
 * @since 1.0.0
 */
@Component

public class CsvParser {
    private static final Logger log = LoggerFactory.getLogger(CsvParser.class);
    
    /**
     * Parse CSV file and return rows as maps
     * 
     * @param inputStream CSV file input stream
     * @return List of maps, each representing a row with column names as keys
     * @throws IOException if file cannot be read
     */
    public List<Map<String, String>> parseCsv(InputStream inputStream) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setIgnoreEmptyLines(true)
                 .setTrim(true)
                 .build()
                 .parse(reader)) {
            
            List<String> headers = csvParser.getHeaderNames();
            
            for (CSVRecord record : csvParser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : headers) {
                    String value = record.get(header);
                    row.put(header, value != null ? value.trim() : "");
                }
                rows.add(row);
            }
            
            log.debug("Parsed {} rows from CSV", rows.size());
        }
        
        return rows;
    }
    
    /**
     * Write data to CSV format
     * 
     * @param headers Column headers
     * @param rows Data rows
     * @return CSV content as string
     */
    public String writeCsv(List<String> headers, List<Map<String, String>> rows) {
        StringBuilder csv = new StringBuilder();
        
        // Write headers
        csv.append(String.join(",", escapeCsvValues(headers))).append("\n");
        
        // Write rows
        for (Map<String, String> row : rows) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                values.add(row.getOrDefault(header, ""));
            }
            csv.append(String.join(",", escapeCsvValues(values))).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Escape CSV values (handle commas, quotes, newlines)
     */
    private List<String> escapeCsvValues(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                escaped.add("");
            } else if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                escaped.add("\"" + value.replace("\"", "\"\"") + "\"");
            } else {
                escaped.add(value);
            }
        }
        return escaped;
    }
}
