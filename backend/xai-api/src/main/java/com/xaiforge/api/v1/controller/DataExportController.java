package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.DataExportApplicationService;
import com.xaiforge.common.annotation.LogActivity;
import com.xaiforge.common.dto.ExportJobDto;
import com.xaiforge.common.dto.ExportRequest;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/export")
@Tag(name = "Data Export", description = "GDPR-compliant data export operations")
@RequiredArgsConstructor
@Slf4j
public class DataExportController {
    
    private final DataExportApplicationService exportService;
    
    @PostMapping("/request")
    @Operation(summary = "Request a data export")
    @LogActivity(
        eventType = "DATA_EXPORT_REQUESTED",
        description = "Data export requested",
        resourceType = "EXPORT"
    )
    public ResponseEntity<ExportJobDto> requestExport(
            @Valid @RequestBody ExportRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ExportJobDto job = exportService.requestExport(user.getId(), request);
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs")
    @Operation(summary = "Get all export jobs for the current user")
    public ResponseEntity<List<ExportJobDto>> getExportJobs(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ExportJobDto> jobs = exportService.getUserExports(user.getId());
        return ResponseEntity.ok(jobs);
    }
    
    @GetMapping("/jobs/{jobId}/status")
    @Operation(summary = "Get export job status")
    public ResponseEntity<ExportJobDto> getExportStatus(
            @PathVariable Long jobId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ExportJobDto job = exportService.getExportStatus(user.getId(), jobId);
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs/{jobId}/download")
    @Operation(summary = "Download completed export")
    @LogActivity(
        eventType = "DATA_EXPORT_DOWNLOADED",
        description = "Data export downloaded: #{#jobId}",
        resourceType = "EXPORT",
        resourceId = "#{#jobId}"
    )
    public ResponseEntity<Resource> downloadExport(
            @PathVariable Long jobId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Resource resource = exportService.downloadExport(user.getId(), jobId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "xai-export-" + jobId + ".zip");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }
}
