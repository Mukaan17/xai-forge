package com.example.xaiapp.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.xaiapp.dto.ApiResponse;
import com.example.xaiapp.dto.DatasetDto;
import com.example.xaiapp.entity.User;
import com.example.xaiapp.service.DatasetService;

@RestController
@RequestMapping("/api/datasets")
@CrossOrigin(origins = "*")
public class DatasetController {
    
    private final DatasetService datasetService;
    
    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDataset(@RequestParam("file") MultipartFile file,
                                         Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            DatasetDto dataset = datasetService.storeFile(file, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Dataset uploaded successfully", dataset));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to upload dataset: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<DatasetDto>> getUserDatasets(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<DatasetDto> datasets = datasetService.listUserDatasets(user.getId());
        return ResponseEntity.ok(datasets);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDataset(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            return datasetService.getDataset(id, user.getId())
                .map(dataset -> ResponseEntity.ok(dataset))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve dataset: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDataset(@PathVariable Long id, 
                                                   Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            datasetService.deleteDataset(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Dataset deleted successfully"));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete dataset: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
