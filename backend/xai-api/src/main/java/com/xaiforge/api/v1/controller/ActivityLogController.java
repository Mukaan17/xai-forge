package com.xaiforge.api.v1.controller;

import com.xaiforge.domain.activity.entity.ActivityLog;
import com.xaiforge.domain.user.entity.User;
import com.xaiforge.infrastructure.persistence.activity.ActivityLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    
    private final ActivityLogRepository activityRepository;
    
    public ActivityLogController(ActivityLogRepository activityRepository) {
        this.activityRepository = activityRepository;
    }
    
    @GetMapping
    @Operation(summary = "Get activity log")
    public ResponseEntity<List<Map<String, Object>>> getActivityLog(
            @RequestParam(required = false) Integer days,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        List<ActivityLog> activities;
        if (days != null && days > 0) {
            LocalDateTime after = LocalDateTime.now().minusDays(days);
            Pageable pageable = PageRequest.of(0, 1000);
            activities = activityRepository.findByUserIdAndTimestampAfterOrderByTimestampDesc(user.getId(), after, pageable);
        } else {
            Pageable pageable = PageRequest.of(0, 1000); // Get up to 1000 records
            Page<ActivityLog> page = activityRepository.findByUserIdOrderByTimestampDesc(user.getId(), pageable);
            activities = page.getContent();
        }
        
        List<Map<String, Object>> result = activities.stream().map(activity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", activity.getId());
            map.put("eventType", activity.getEventType().name());
            map.put("details", activity.getDetails());
            map.put("ipAddress", activity.getIpAddress());
            map.put("userAgent", activity.getUserAgent());
            map.put("timestamp", activity.getTimestamp().toString());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
}
