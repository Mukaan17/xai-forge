/**
 * @Author: Mukhil Sundararaj
 * @Date:   2026-01-09 18:09:54
 * @Last Modified by:   Mukhil Sundararaj
 * @Last Modified time: 2026-01-12 21:50:41
 */
package com.xaiforge.api.v1.controller;

import com.xaiforge.application.query.GetDashboardStatsQuery;
import com.xaiforge.application.service.DashboardStatsService;
import com.xaiforge.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Dashboard statistics")
public class DashboardController {
    
    private final DashboardStatsService dashboardStatsService;
    
    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<GetDashboardStatsQuery.DashboardStats> getStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GetDashboardStatsQuery.DashboardStats stats = dashboardStatsService.getStats(user.getId());
        return ResponseEntity.ok(stats);
    }
}

