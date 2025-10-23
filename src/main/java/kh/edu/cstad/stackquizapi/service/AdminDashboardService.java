package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.AdminDashboardResponse;

/**
 * Service interface for managing and retrieving statistics for the Admin Dashboard.
 * <p>
 * Provides methods to fetch overall and time-specific statistics for administrative monitoring,
 * including user activity, session counts, and other key metrics.
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface AdminDashboardService {

    /**
     * Retrieve comprehensive statistics for the admin dashboard.
     * <p>
     * Includes overall metrics across users, quizzes, sessions, and other relevant data.
     * </p>
     *
     * @return an AdminDashboardResponse containing all aggregated statistics
     */
    AdminDashboardResponse getAdminDashboardStats();

    /**
     * Retrieve dashboard statistics filtered by a specific time period.
     * <p>
     * Useful for monitoring trends, growth, or activity over the last {@code days} days.
     * </p>
     *
     * @param days the number of past days to include in the statistics
     * @return an AdminDashboardResponse containing time-filtered metrics
     */
    AdminDashboardResponse getAdminDashboardStatsByPeriod(int days);
}


