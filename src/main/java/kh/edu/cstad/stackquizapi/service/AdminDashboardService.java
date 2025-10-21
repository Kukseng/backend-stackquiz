package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.AdminDashboardResponse;

/**
 * Service interface for Admin Dashboard operations
 */
public interface AdminDashboardService {
    
    /**
     * Get comprehensive admin dashboard statistics
     * @return AdminDashboardResponse with all statistics
     */
    AdminDashboardResponse getAdminDashboardStats();
    
    /**
     * Get statistics for a specific time period
     * @param days Number of days to look back
     * @return AdminDashboardResponse with filtered statistics
     */
    AdminDashboardResponse getAdminDashboardStatsByPeriod(int days);
}

