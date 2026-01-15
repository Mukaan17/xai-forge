package com.xaiforge.api.v1.controller;

import com.xaiforge.application.service.GlobalSearchService;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Global search operations")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    
    private final GlobalSearchService searchService;
    
    @GetMapping
    @Operation(summary = "Global search across datasets, models, and predictions")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            Map<String, Object> results = searchService.search(user.getId(), q, limit);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error performing search: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
