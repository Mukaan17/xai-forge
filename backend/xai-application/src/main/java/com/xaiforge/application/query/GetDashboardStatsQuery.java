package com.xaiforge.application.query;

/**
 * Query to get dashboard statistics for a user.
 */
public record GetDashboardStatsQuery(Long userId) implements Query<GetDashboardStatsQuery.DashboardStats> {
    
    public record DashboardStats(
        long totalDatasets,
        long totalModels,
        long totalPredictions,
        double averageAccuracy,
        java.util.List<RecentActivity> recentActivity,
        java.util.Map<String, Long> modelsByType,
        java.util.List<WeeklyUsage> weeklyUsage,
        java.util.Map<String, Long> datasetSizes
    ) {}
    
    public record RecentActivity(
        String type,
        String description,
        String timestamp
    ) {}
    
    public record WeeklyUsage(
        String day,
        long predictions
    ) {}
}

