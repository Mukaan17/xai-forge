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
 * Utility class for generating Excel test reports
 * Creates comprehensive test reports with multiple sheets for different test types
 */
@Component
public class ExcelTestReporter {
    
    private static final String REPORT_FILENAME = "test-report-%s.xlsx";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Generate comprehensive test report
     */
    public void generateTestReport(TestReportData reportData) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String filename = String.format(REPORT_FILENAME, timestamp);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            createSummarySheet(workbook, reportData);
            
            // Create unit tests sheet
            createUnitTestsSheet(workbook, reportData.getUnitTests());
            
            // Create integration tests sheet
            createIntegrationTestsSheet(workbook, reportData.getIntegrationTests());
            
            // Create API tests sheet
            createApiTestsSheet(workbook, reportData.getApiTests());
            
            // Create E2E tests sheet
            createE2eTestsSheet(workbook, reportData.getE2eTests());
            
            // Create coverage sheet
            createCoverageSheet(workbook, reportData.getCoverageData());
            
            // Create performance sheet
            createPerformanceSheet(workbook, reportData.getPerformanceData());
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                workbook.write(fileOut);
            }
        }
        
        System.out.println("Test report generated: " + filename);
    }
    
    private void createSummarySheet(Workbook workbook, TestReportData reportData) {
        Sheet sheet = workbook.createSheet("Summary");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Test Type", "Total Tests", "Passed", "Failed", "Skipped", "Success Rate", "Duration (ms)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (TestTypeSummary summary : reportData.getTestTypeSummaries()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(summary.getTestType());
            row.createCell(1).setCellValue(summary.getTotalTests());
            row.createCell(2).setCellValue(summary.getPassed());
            row.createCell(3).setCellValue(summary.getFailed());
            row.createCell(4).setCellValue(summary.getSkipped());
            row.createCell(5).setCellValue(summary.getSuccessRate());
            row.createCell(6).setCellValue(summary.getDuration());
            
            // Apply data style to all cells
            for (int i = 0; i < 7; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createUnitTestsSheet(Workbook workbook, List<TestResult> unitTests) {
        Sheet sheet = workbook.createSheet("Unit Tests");
        createTestResultsSheet(workbook, sheet, unitTests);
    }
    
    private void createIntegrationTestsSheet(Workbook workbook, List<TestResult> integrationTests) {
        Sheet sheet = workbook.createSheet("Integration Tests");
        createTestResultsSheet(workbook, sheet, integrationTests);
    }
    
    private void createApiTestsSheet(Workbook workbook, List<TestResult> apiTests) {
        Sheet sheet = workbook.createSheet("API Tests");
        createTestResultsSheet(workbook, sheet, apiTests);
    }
    
    private void createE2eTestsSheet(Workbook workbook, List<TestResult> e2eTests) {
        Sheet sheet = workbook.createSheet("E2E Tests");
        createTestResultsSheet(workbook, sheet, e2eTests);
    }
    
    private void createTestResultsSheet(Workbook workbook, Sheet sheet, List<TestResult> testResults) {
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle passStyle = createPassStyle(workbook);
        CellStyle failStyle = createFailStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Test Name", "Class", "Method", "Status", "Duration (ms)", "Error Message", "Timestamp"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (TestResult result : testResults) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(result.getTestName());
            row.createCell(1).setCellValue(result.getClassName());
            row.createCell(2).setCellValue(result.getMethodName());
            row.createCell(3).setCellValue(result.getStatus());
            row.createCell(4).setCellValue(result.getDuration());
            row.createCell(5).setCellValue(result.getErrorMessage());
            row.createCell(6).setCellValue(result.getTimestamp());
            
            // Apply appropriate style based on status
            Cell statusCell = row.getCell(3);
            if ("PASSED".equals(result.getStatus())) {
                statusCell.setCellStyle(passStyle);
            } else if ("FAILED".equals(result.getStatus())) {
                statusCell.setCellStyle(failStyle);
            } else {
                statusCell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createCoverageSheet(Workbook workbook, CoverageData coverageData) {
        Sheet sheet = workbook.createSheet("Coverage");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle coverageStyle = createCoverageStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Package", "Class", "Line Coverage %", "Branch Coverage %", "Method Coverage %", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (ClassCoverage classCoverage : coverageData.getClassCoverages()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(classCoverage.getPackageName());
            row.createCell(1).setCellValue(classCoverage.getClassName());
            row.createCell(2).setCellValue(classCoverage.getLineCoverage());
            row.createCell(3).setCellValue(classCoverage.getBranchCoverage());
            row.createCell(4).setCellValue(classCoverage.getMethodCoverage());
            row.createCell(5).setCellValue(classCoverage.getStatus());
            
            // Apply coverage style
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(coverageStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createPerformanceSheet(Workbook workbook, PerformanceData performanceData) {
        Sheet sheet = workbook.createSheet("Performance");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Test Type", "Average Duration (ms)", "Min Duration (ms)", "Max Duration (ms)", "95th Percentile (ms)", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (PerformanceMetric metric : performanceData.getMetrics()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(metric.getTestType());
            row.createCell(1).setCellValue(metric.getAverageDuration());
            row.createCell(2).setCellValue(metric.getMinDuration());
            row.createCell(3).setCellValue(metric.getMaxDuration());
            row.createCell(4).setCellValue(metric.getPercentile95());
            row.createCell(5).setCellValue(metric.getStatus());
            
            // Apply data style
            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
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
    
    // Inner classes for data structures
    public static class TestReportData {
        private List<TestTypeSummary> testTypeSummaries;
        private List<TestResult> unitTests;
        private List<TestResult> integrationTests;
        private List<TestResult> apiTests;
        private List<TestResult> e2eTests;
        private CoverageData coverageData;
        private PerformanceData performanceData;
        
        // Getters and setters
        public List<TestTypeSummary> getTestTypeSummaries() { return testTypeSummaries; }
        public void setTestTypeSummaries(List<TestTypeSummary> testTypeSummaries) { this.testTypeSummaries = testTypeSummaries; }
        
        public List<TestResult> getUnitTests() { return unitTests; }
        public void setUnitTests(List<TestResult> unitTests) { this.unitTests = unitTests; }
        
        public List<TestResult> getIntegrationTests() { return integrationTests; }
        public void setIntegrationTests(List<TestResult> integrationTests) { this.integrationTests = integrationTests; }
        
        public List<TestResult> getApiTests() { return apiTests; }
        public void setApiTests(List<TestResult> apiTests) { this.apiTests = apiTests; }
        
        public List<TestResult> getE2eTests() { return e2eTests; }
        public void setE2eTests(List<TestResult> e2eTests) { this.e2eTests = e2eTests; }
        
        public CoverageData getCoverageData() { return coverageData; }
        public void setCoverageData(CoverageData coverageData) { this.coverageData = coverageData; }
        
        public PerformanceData getPerformanceData() { return performanceData; }
        public void setPerformanceData(PerformanceData performanceData) { this.performanceData = performanceData; }
    }
    
    public static class TestTypeSummary {
        private String testType;
        private int totalTests;
        private int passed;
        private int failed;
        private int skipped;
        private double successRate;
        private long duration;
        
        // Getters and setters
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
        
        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
        
        public int getPassed() { return passed; }
        public void setPassed(int passed) { this.passed = passed; }
        
        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }
        
        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
    }
    
    public static class TestResult {
        private String testName;
        private String className;
        private String methodName;
        private String status;
        private long duration;
        private String errorMessage;
        private String timestamp;
        
        // Getters and setters
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class CoverageData {
        private List<ClassCoverage> classCoverages;
        
        public List<ClassCoverage> getClassCoverages() { return classCoverages; }
        public void setClassCoverages(List<ClassCoverage> classCoverages) { this.classCoverages = classCoverages; }
    }
    
    public static class ClassCoverage {
        private String packageName;
        private String className;
        private double lineCoverage;
        private double branchCoverage;
        private double methodCoverage;
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
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class PerformanceData {
        private List<PerformanceMetric> metrics;
        
        public List<PerformanceMetric> getMetrics() { return metrics; }
        public void setMetrics(List<PerformanceMetric> metrics) { this.metrics = metrics; }
    }
    
    public static class PerformanceMetric {
        private String testType;
        private double averageDuration;
        private double minDuration;
        private double maxDuration;
        private double percentile95;
        private String status;
        
        // Getters and setters
        public String getTestType() { return testType; }
        public void setTestType(String testType) { this.testType = testType; }
        
        public double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(double averageDuration) { this.averageDuration = averageDuration; }
        
        public double getMinDuration() { return minDuration; }
        public void setMinDuration(double minDuration) { this.minDuration = minDuration; }
        
        public double getMaxDuration() { return maxDuration; }
        public void setMaxDuration(double maxDuration) { this.maxDuration = maxDuration; }
        
        public double getPercentile95() { return percentile95; }
        public void setPercentile95(double percentile95) { this.percentile95 = percentile95; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
