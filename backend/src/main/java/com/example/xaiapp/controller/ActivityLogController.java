package com.example.xaiapp.controller;

import com.example.xaiapp.dto.response.ActivityLogDTO;
import com.example.xaiapp.entity.ActivityLog;
import com.example.xaiapp.security.CurrentUser;
import com.example.xaiapp.security.UserPrincipal;
import com.example.xaiapp.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for activity logs.
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "Activity Logs", description = "Activity log endpoints")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "List activity logs")
    public ResponseEntity<Page<ActivityLogDTO>> getActivityLogs(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ActivityLog> logs = activityLogService.getActivityLogs(currentUser.getId(), pageable);
        Page<ActivityLogDTO> dtoPage = logs.map(this::mapToActivityLogDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/export")
    @Operation(summary = "Export activity logs to CSV")
    public ResponseEntity<byte[]> exportActivityLogs(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        // Default to last 30 days if not specified
        if (start == null) {
            start = LocalDateTime.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        byte[] data = activityLogService.exportActivityLogsToCsv(currentUser.getId(), start, end);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "activity_logs.csv");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ActivityLogDTO mapToActivityLogDTO(ActivityLog log) {
        return ActivityLogDTO.builder()
            .id(log.getId())
            .action(log.getAction() != null ? log.getAction().name() : null)
            .resourceType(log.getResourceType())
            .resourceId(log.getResourceId())
            .resourceName(log.getResourceName())
            .description(log.getDescription())
            .success(log.getSuccess())
            .ipAddress(log.getIpAddress())
            .location(log.getLocation())
            .createdAt(log.getCreatedAt())
            .metadata(log.getMetadata())
            .build();
    }
}
