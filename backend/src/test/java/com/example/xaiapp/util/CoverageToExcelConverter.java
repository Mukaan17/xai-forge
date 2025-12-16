package com.example.xaiapp.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting code coverage data to Excel format
 * Creates detailed coverage reports with visual indicators and charts
 */
@Component
public class CoverageToExcelConverter {
    
    private static final String COVERAGE_FILENAME = "coverage-report-%s.xlsx";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Convert coverage data to Excel format
     */
    public void convertCoverageToExcel(CoverageReportData coverageData) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String filename = String.format(COVERAGE_FILENAME, timestamp);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create overview sheet
            createOverviewSheet(workbook, coverageData);
            
            // Create package coverage sheet
            createPackageCoverageSheet(workbook, coverageData.getPackageCoverages());
            
            // Create class coverage sheet
            createClassCoverageSheet(workbook, coverageData.getClassCoverages());
            
            // Create method coverage sheet
            createMethodCoverageSheet(workbook, coverageData.getMethodCoverages());
            
            // Create line coverage sheet
            createLineCoverageSheet(workbook, coverageData.getLineCoverages());
            
            // Create branch coverage sheet
            createBranchCoverageSheet(workbook, coverageData.getBranchCoverages());
            
            // Create summary sheet
            createSummarySheet(workbook, coverageData.getSummary());
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
        }
        
        System.out.println("Coverage report generated: " + filename);
    }
    
    private void createOverviewSheet(Workbook workbook, CoverageReportData coverageData) {
        Sheet sheet = workbook.createSheet("Overview");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle coverageStyle = createCoverageStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Metric", "Value", "Target", "Status", "Percentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (CoverageMetric metric : coverageData.getMetrics()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(metric.getName());
            row.createCell(1).setCellValue(metric.getValue());
            row.createCell(2).setCellValue(metric.getTarget());
            row.createCell(3).setCellValue(metric.getStatus());
            row.createCell(4).setCellValue(metric.getPercentage());
            
            // Apply appropriate style based on status
            Cell statusCell = row.getCell(3);
            if ("PASS".equals(metric.getStatus())) {
                statusCell.setCellStyle(createPassStyle(workbook));
            } else if ("FAIL".equals(metric.getStatus())) {
                statusCell.setCellStyle(createFailStyle(workbook));
            } else {
                statusCell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createPackageCoverageSheet(Workbook workbook, List<PackageCoverage> packageCoverages) {
        Sheet sheet = workbook.createSheet("Package Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Line Coverage %", "Branch Coverage %", "Method Coverage %", "Classes", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (PackageCoverage coverage : packageCoverages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coverage.getPackageName());
            row.createCell(1).setCellValue(coverage.getLineCoverage());
            row.createCell(2).setCellValue(coverage.getBranchCoverage());
            row.createCell(3).setCellValue(coverage.getMethodCoverage());
            row.createCell(4).setCellValue(coverage.getClassCount());
            row.createCell(5).setCellValue(coverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createClassCoverageSheet(Workbook workbook, List<ClassCoverage> classCoverages) {
        Sheet sheet = workbook.createSheet("Class Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Class", "Line Coverage %", "Branch Coverage %", "Method Coverage %", "Lines", "Branches", "Methods", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (ClassCoverage coverage : classCoverages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coverage.getPackageName());
            row.createCell(1).setCellValue(coverage.getClassName());
            row.createCell(2).setCellValue(coverage.getLineCoverage());
            row.createCell(3).setCellValue(coverage.getBranchCoverage());
            row.createCell(4).setCellValue(coverage.getMethodCoverage());
            row.createCell(5).setCellValue(coverage.getLineCount());
            row.createCell(6).setCellValue(coverage.getBranchCount());
            row.createCell(7).setCellValue(coverage.getMethodCount());
            row.createCell(8).setCellValue(coverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 9; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createMethodCoverageSheet(Workbook workbook, List<MethodCoverage> methodCoverages) {
        Sheet sheet = workbook.createSheet("Method Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Class", "Method", "Line Coverage %", "Branch Coverage %", "Lines", "Branches", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (MethodCoverage coverage : methodCoverages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coverage.getPackageName());
            row.createCell(1).setCellValue(coverage.getClassName());
            row.createCell(2).setCellValue(coverage.getMethodName());
            row.createCell(3).setCellValue(coverage.getLineCoverage());
            row.createCell(4).setCellValue(coverage.getBranchCoverage());
            row.createCell(5).setCellValue(coverage.getLineCount());
            row.createCell(6).setCellValue(coverage.getBranchCount());
            row.createCell(7).setCellValue(coverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createLineCoverageSheet(Workbook workbook, List<LineCoverage> lineCoverages) {
        Sheet sheet = workbook.createSheet("Line Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Class", "Method", "Line Number", "Covered", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (LineCoverage coverage : lineCoverages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coverage.getPackageName());
            row.createCell(1).setCellValue(coverage.getClassName());
            row.createCell(2).setCellValue(coverage.getMethodName());
            row.createCell(3).setCellValue(coverage.getLineNumber());
            row.createCell(4).setCellValue(coverage.isCovered());
            row.createCell(5).setCellValue(coverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createBranchCoverageSheet(Workbook workbook, List<BranchCoverage> branchCoverages) {
        Sheet sheet = workbook.createSheet("Branch Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Class", "Method", "Branch Number", "Covered", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (BranchCoverage coverage : branchCoverages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(coverage.getPackageName());
            row.createCell(1).setCellValue(coverage.getClassName());
            row.createCell(2).setCellValue(coverage.getMethodName());
            row.createCell(3).setCellValue(coverage.getBranchNumber());
            row.createCell(4).setCellValue(coverage.isCovered());
            row.createCell(5).setCellValue(coverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createSummarySheet(Workbook workbook, CoverageSummary summary) {
        Sheet sheet = workbook.createSheet("Summary");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Metric", "Value", "Target", "Status", "Percentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (CoverageMetric metric : summary.getMetrics()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(metric.getName());
            row.createCell(1).setCellValue(metric.getValue());
            row.createCell(2).setCellValue(metric.getTarget());
            row.createCell(3).setCellValue(metric.getStatus());
            row.createCell(4).setCellValue(metric.getPercentage());
            
            // Apply appropriate style based on status
            Cell statusCell = row.getCell(3);
            if ("PASS".equals(metric.getStatus())) {
                statusCell.setCellStyle(createPassStyle(workbook));
            } else if ("FAIL".equals(metric.getStatus())) {
                statusCell.setCellStyle(createFailStyle(workbook));
            } else {
                statusCell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createCoverageStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createPassStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createFailStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    // Inner classes for data structures
    public static class CoverageReportData {
        private List<CoverageMetric> metrics;
        private List<PackageCoverage> packageCoverages;
        private List<ClassCoverage> classCoverages;
        private List<MethodCoverage> methodCoverages;
        private List<LineCoverage> lineCoverages;
        private List<BranchCoverage> branchCoverages;
        private CoverageSummary summary;
        
        // Getters and setters
        public List<CoverageMetric> getMetrics() { return metrics; }
        public void setMetrics(List<CoverageMetric> metrics) { this.metrics = metrics; }
        
        public List<PackageCoverage> getPackageCoverages() { return packageCoverages; }
        public void setPackageCoverages(List<PackageCoverage> packageCoverages) { this.packageCoverages = packageCoverages; }
        
        public List<ClassCoverage> getClassCoverages() { return classCoverages; }
        public void setClassCoverages(List<ClassCoverage> classCoverages) { this.classCoverages = classCoverages; }
        
        public List<MethodCoverage> getMethodCoverages() { return methodCoverages; }
        public void setMethodCoverages(List<MethodCoverage> methodCoverages) { this.methodCoverages = methodCoverages; }
        
        public List<LineCoverage> getLineCoverages() { return lineCoverages; }
        public void setLineCoverages(List<LineCoverage> lineCoverages) { this.lineCoverages = lineCoverages; }
        
        public List<BranchCoverage> getBranchCoverages() { return branchCoverages; }
        public void setBranchCoverages(List<BranchCoverage> branchCoverages) { this.branchCoverages = branchCoverages; }
        
        public CoverageSummary getSummary() { return summary; }
        public void setSummary(CoverageSummary summary) { this.summary = summary; }
    }
    
    public static class CoverageMetric {
        private String name;
        private double value;
        private double target;
        private String status;
        private double percentage;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public double getTarget() { return target; }
        public void setTarget(double target) { this.target = target; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
    
    public static class PackageCoverage {
        private String packageName;
        private double lineCoverage;
        private double branchCoverage;
        private double methodCoverage;
        private int classCount;
        private String status;
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public double getLineCoverage() { return lineCoverage; }
        public void setLineCoverage(double lineCoverage) { this.lineCoverage = lineCoverage; }
        
        public double getBranchCoverage() { return branchCoverage; }
        public void setBranchCoverage(double branchCoverage) { this.branchCoverage = branchCoverage; }
        
        public double getMethodCoverage() { return methodCoverage; }
        public void setMethodCoverage(double methodCoverage) { this.methodCoverage = methodCoverage; }
        
        public int getClassCount() { return classCount; }
        public void setClassCount(int classCount) { this.classCount = classCount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class ClassCoverage {
        private String packageName;
        private String className;
        private double lineCoverage;
        private double branchCoverage;
        private double methodCoverage;
        private int lineCount;
        private int branchCount;
        private int methodCount;
        private String status;
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public double getLineCoverage() { return lineCoverage; }
        public void setLineCoverage(double lineCoverage) { this.lineCoverage = lineCoverage; }
        
        public double getBranchCoverage() { return branchCoverage; }
        public void setBranchCoverage(double branchCoverage) { this.branchCoverage = branchCoverage; }
        
        public double getMethodCoverage() { return methodCoverage; }
        public void setMethodCoverage(double methodCoverage) { this.methodCoverage = methodCoverage; }
        
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        
        public int getBranchCount() { return branchCount; }
        public void setBranchCount(int branchCount) { this.branchCount = branchCount; }
        
        public int getMethodCount() { return methodCount; }
        public void setMethodCount(int methodCount) { this.methodCount = methodCount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class MethodCoverage {
        private String packageName;
        private String className;
        private String methodName;
        private double lineCoverage;
        private double branchCoverage;
        private int lineCount;
        private int branchCount;
        private String status;
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public double getLineCoverage() { return lineCoverage; }
        public void setLineCoverage(double lineCoverage) { this.lineCoverage = lineCoverage; }
        
        public double getBranchCoverage() { return branchCoverage; }
        public void setBranchCoverage(double branchCoverage) { this.branchCoverage = branchCoverage; }
        
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
        
        public int getBranchCount() { return branchCount; }
        public void setBranchCount(int branchCount) { this.branchCount = branchCount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class LineCoverage {
        private String packageName;
        private String className;
        private String methodName;
        private int lineNumber;
        private boolean covered;
        private String status;
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public int getLineNumber() { return lineNumber; }
        public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
        
        public boolean isCovered() { return covered; }
        public void setCovered(boolean covered) { this.covered = covered; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class BranchCoverage {
        private String packageName;
        private String className;
        private String methodName;
        private int branchNumber;
        private boolean covered;
        private String status;
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public int getBranchNumber() { return branchNumber; }
        public void setBranchNumber(int branchNumber) { this.branchNumber = branchNumber; }
        
        public boolean isCovered() { return covered; }
        public void setCovered(boolean covered) { this.covered = covered; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class CoverageSummary {
        private List<CoverageMetric> metrics;
        
        public List<CoverageMetric> getMetrics() { return metrics; }
        public void setMetrics(List<CoverageMetric> metrics) { this.metrics = metrics; }
    }
}
