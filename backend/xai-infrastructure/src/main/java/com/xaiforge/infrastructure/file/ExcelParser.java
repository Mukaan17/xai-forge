package com.xaiforge.infrastructure.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing Excel files (.xlsx, .xls) and converting to CSV-like data.
 */
@Component
@Slf4j
public class ExcelParser {
    
    /**
     * Parse Excel file and extract headers and row count.
     * 
     * @param inputStream Excel file input stream
     * @param filename Original filename (to determine format)
     * @return ExcelParseResult containing headers and row count
     * @throws IOException if file cannot be read
     */
    public ExcelParseResult parseExcel(InputStream inputStream, String filename) throws IOException {
        Workbook workbook = null;
        try {
            // Determine workbook type based on file extension
            if (filename.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (filename.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                throw new IllegalArgumentException("Unsupported Excel format. Only .xlsx and .xls are supported.");
            }
            
            // Use first sheet
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel file has no sheets");
            }
            
            // Get headers from first row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file has no header row");
            }
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String headerValue = getCellValueAsString(cell);
                if (headerValue != null && !headerValue.trim().isEmpty()) {
                    headers.add(headerValue.trim());
                }
            }
            
            if (headers.isEmpty()) {
                throw new IllegalArgumentException("Excel file has no valid headers");
            }
            
            // Count data rows (excluding header)
            int rowCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && hasData(row)) {
                    rowCount++;
                }
            }
            
            log.info("Parsed Excel file: {} headers, {} data rows", headers.size(), rowCount);
            
            return new ExcelParseResult(headers, rowCount, sheet);
            
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.warn("Error closing workbook", e);
                }
            }
        }
    }
    
    /**
     * Convert Excel sheet to CSV format (for model training compatibility).
     * 
     * @param sheet Excel sheet
     * @return CSV content as string
     */
    public String convertToCSV(Sheet sheet) {
        StringBuilder csv = new StringBuilder();
        
        // Write headers
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
            csv.append(String.join(",", headers)).append("\n");
        }
        
        // Write data rows
        if (headerRow != null) {
            int headerCount = headerRow.getLastCellNum();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && hasData(row)) {
                    List<String> values = new ArrayList<>();
                    for (int j = 0; j < headerCount; j++) {
                        Cell cell = row.getCell(j);
                        String value = getCellValueAsString(cell);
                        // Escape commas and quotes in CSV
                        if (value != null && (value.contains(",") || value.contains("\""))) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                        values.add(value != null ? value : "");
                    }
                    csv.append(String.join(",", values)).append("\n");
                }
            }
        }
        
        return csv.toString();
    }
    
    /**
     * Get cell value as string, handling different cell types.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numeric values without scientific notation
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Evaluate formula
                try {
                    return getCellValueAsString(cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
    
    /**
     * Check if a row has any data (non-empty cells).
     */
    private boolean hasData(Row row) {
        if (row == null) {
            return false;
        }
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Result of Excel parsing operation.
     */
    public static class ExcelParseResult {
        private final List<String> headers;
        private final int rowCount;
        private final Sheet sheet;
        
        public ExcelParseResult(List<String> headers, int rowCount, Sheet sheet) {
            this.headers = headers;
            this.rowCount = rowCount;
            this.sheet = sheet;
        }
        
        public List<String> getHeaders() {
            return headers;
        }
        
        public int getRowCount() {
            return rowCount;
        }
        
        public Sheet getSheet() {
            return sheet;
        }
    }
}
