package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.UserActivityResponse;

/**
 * Service for user activity and analytics
 */
public interface UserActivityService {
    
    /**
     * Get comprehensive activity statistics for a user
     * @param userId The user ID
     * @return UserActivityResponse with all statistics
     */
    UserActivityResponse getUserActivity(String userId);
    
    /**
     * Get activity statistics for a specific time range
     * @param userId The user ID
     * @param timeRange Time range filter (7days, 30days, 90days, 1year, all)
     * @return UserActivityResponse with filtered statistics
     */
    UserActivityResponse getUserActivityByTimeRange(String userId, String timeRange);
}

