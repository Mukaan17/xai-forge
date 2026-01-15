package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.ActivityLogQueryService;
import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/activity")
@Tag(name = "Activity Log", description = "Activity log operations")
public class ActivityLogController {
    
    private final ActivityLogQueryService activityLogQueryService;
    
    public ActivityLogController(ActivityLogQueryService activityLogQueryService) {
        this.activityLogQueryService = activityLogQueryService;
    }
    
    @GetMapping
    @Operation(summary = "Get activity log with search and filters")
    public ResponseEntity<Map<String, Object>> getActivityLog(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        // Convert days to dateFrom if provided
        if (days != null && days > 0 && dateFrom == null) {
            dateFrom = LocalDateTime.now().minusDays(days);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> activityPage = activityLogQueryService.searchActivityLogs(
            user.getId(),
            eventType,
            search,
            dateFrom,
            dateTo,
            ipAddress,
            pageable
        );
        
        List<Map<String, Object>> content = activityPage.getContent().stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", activity.getId());
            map.put("eventType", activity.getEventType().name());
            map.put("details", activity.getDetails());
            map.put("ipAddress", activity.getIpAddress());
            map.put("userAgent", activity.getUserAgent());
            map.put("timestamp", activity.getTimestamp().toString());
            return map;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", activityPage.getTotalElements());
        response.put("totalPages", activityPage.getTotalPages());
        response.put("page", activityPage.getNumber());
        response.put("size", activityPage.getSize());
        response.put("hasNext", activityPage.hasNext());
        response.put("hasPrevious", activityPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
}
