package com.example.xaiapp.controller;

import com.example.xaiapp.dto.request.FullExportRequest;
import com.example.xaiapp.dto.response.ExportJobDTO;
import com.example.xaiapp.entity.ExportJob.ExportFormat;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.DataExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for data export.
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Data export endpoints")
public class ExportController {

    private final DataExportService dataExportService;

    @PostMapping("/full")
    @Operation(summary = "Request full data export")
    public ResponseEntity<ExportJobDTO> requestFullExport(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody FullExportRequest request) {
        Map<String, Object> jobMap = dataExportService.requestFullExport(
            currentUser.getId(), request.getIncludeItems(), request.getFormat());
        ExportJobDTO job = mapToExportJobDTO(jobMap);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{jobId}/status")
    @Operation(summary = "Get export job status")
    public ResponseEntity<ExportJobDTO> getExportStatus(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long jobId) {
        Map<String, Object> jobMap = dataExportService.getExportStatus(currentUser.getId(), jobId);
        ExportJobDTO job = mapToExportJobDTO(jobMap);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{jobId}/download")
    @Operation(summary = "Download completed export")
    public ResponseEntity<Resource> downloadExport(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long jobId) {
        Resource resource = dataExportService.downloadExport(currentUser.getId(), jobId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "xai-export.zip");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private ExportJobDTO mapToExportJobDTO(Map<String, Object> map) {
        return ExportJobDTO.builder()
            .id(getLong(map, "id"))
            .status((String) map.get("status"))
            .exportType((String) map.get("exportType"))
            .format((String) map.get("format"))
            .includeItems((java.util.Set<String>) map.get("includeItems"))
            .progress(getInteger(map, "progress"))
            .currentStep((String) map.get("currentStep"))
            .fileSizeBytes(getLong(map, "fileSizeBytes"))
            .errorMessage((String) map.get("errorMessage"))
            .createdAt((java.time.LocalDateTime) map.get("createdAt"))
            .startedAt((java.time.LocalDateTime) map.get("startedAt"))
            .completedAt((java.time.LocalDateTime) map.get("completedAt"))
            .expiresAt((java.time.LocalDateTime) map.get("expiresAt"))
            .downloadCount(getInteger(map, "downloadCount"))
            .build();
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
}
