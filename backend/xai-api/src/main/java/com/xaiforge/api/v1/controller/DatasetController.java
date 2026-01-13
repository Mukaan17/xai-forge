package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.DatasetApplicationService;
import com.xaiforge.common.dto.DatasetDto;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/datasets")
@Tag(name = "Datasets", description = "Dataset management operations")
public class DatasetController {
    
    private final DatasetApplicationService datasetService;
    
    public DatasetController(DatasetApplicationService datasetService) {
        this.datasetService = datasetService;
    }
    
    @PostMapping("/upload")
    @Operation(summary = "Upload a CSV dataset")
    public ResponseEntity<DatasetDto> uploadDataset(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        DatasetDto dataset = datasetService.uploadDataset(file, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dataset);
    }
    
    @GetMapping
    @Operation(summary = "List user datasets")
    public ResponseEntity<List<DatasetDto>> getUserDatasets(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<DatasetDto> datasets = datasetService.listUserDatasets(user.getId());
        return ResponseEntity.ok(datasets);
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
    public ResponseEntity<?> previewDataset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int rows,
            Authentication authentication) {
        // TODO: Implement preview logic
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete dataset")
    public ResponseEntity<Void> deleteDataset(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        datasetService.deleteDataset(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

