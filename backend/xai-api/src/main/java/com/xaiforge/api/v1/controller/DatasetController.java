package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.DatasetApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.DatasetDto;
import com.xaiforge.common.dto.DatasetPreviewDto;
import com.xaiforge.common.dto.DatasetStatisticsDto;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/datasets")
@Tag(name = "Datasets", description = "Dataset management operations")
public class DatasetController {
    
    private final DatasetApplicationService datasetService;
    
    public DatasetController(DatasetApplicationService datasetService) {
        this.datasetService = datasetService;
    }
    
    @PostMapping("/upload")
    @Operation(
        summary = "Upload a dataset file",
        description = """
            Upload a CSV or Excel (.xlsx, .xls) dataset file. The file will be:
            - Validated for format and content
            - Parsed to extract headers and row count
            - Stored securely and associated with your account
            - Made available for model training
            
            **Supported Formats:**
            - CSV (.csv)
            - Excel (.xlsx, .xls)
            
            **File Requirements:**
            - Maximum file size: 100MB
            - Must contain headers in the first row
            - Must have at least one data row
            
            **Response:**
            Returns dataset metadata including ID, filename, headers, and row count.
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Dataset uploaded successfully",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DatasetDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                        {
                          "id": 1,
                          "fileName": "customer-churn.csv",
                          "uploadDate": "2025-01-15T10:30:00",
                          "headers": ["age", "tenure", "monthly_charges", "churn"],
                          "rowCount": 1000,
                          "ownerId": 1
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file format or validation failed"
        )
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @LogActivity(
        eventType = "DATASET_UPLOADED",
        description = "Dataset uploaded: #{#result.fileName}",
        resourceType = "DATASET",
        resourceId = "#{#result.id}",
        resourceName = "#{#result.fileName}"
    )
    public ResponseEntity<DatasetDto> uploadDataset(
            @Parameter(
                description = "Dataset file (CSV or Excel)",
                required = true
            )
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        DatasetDto dataset = datasetService.uploadDataset(file, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dataset);
    }
    
    @GetMapping
    @Operation(summary = "List user datasets with search and filtering")
    public ResponseEntity<Map<String, Object>> getUserDatasets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<DatasetDto> datasets = datasetService.searchUserDatasets(user.getId(), search, dateFrom, dateTo, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", datasets.getContent());
        response.put("currentPage", datasets.getNumber());
        response.put("totalItems", datasets.getTotalElements());
        response.put("totalPages", datasets.getTotalPages());
        response.put("hasNext", datasets.hasNext());
        response.put("hasPrevious", datasets.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get dataset by ID")
    public ResponseEntity<DatasetDto> getDataset(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return datasetService.getDataset(id, user.getId())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview dataset rows")
    public ResponseEntity<DatasetPreviewDto> previewDataset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        DatasetPreviewDto preview = datasetService.previewDataset(id, user.getId(), rows, offset);
        return ResponseEntity.ok(preview);
    }
    
    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get dataset statistics and column analysis")
    public ResponseEntity<DatasetStatisticsDto> getDatasetStatistics(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        DatasetStatisticsDto statistics = datasetService.getDatasetStatistics(id, user.getId());
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/{id}/export")
    @Operation(summary = "Export dataset as CSV file")
    public ResponseEntity<Resource> exportDataset(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        Resource resource = datasetService.exportDataset(id, user.getId());
        
        DatasetDto dataset = datasetService.getDataset(id, user.getId())
            .orElseThrow(() -> new com.xaiforge.common.exception.DatasetNotFoundException(id));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", dataset.fileName());
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete dataset")
    @LogActivity(
        eventType = "DATASET_DELETED",
        description = "Dataset deleted: #{#id}",
        resourceType = "DATASET",
        resourceId = "#{#id}"
    )
    public ResponseEntity<Void> deleteDataset(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        datasetService.deleteDataset(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

