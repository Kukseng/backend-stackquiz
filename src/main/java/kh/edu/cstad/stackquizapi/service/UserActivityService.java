package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.UserActivityResponse;

/**
 * Service interface for tracking and analyzing user activity.
 * <p>
 * Provides methods to retrieve comprehensive activity statistics for a user,
 * including overall activity and time-range specific analytics.
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface UserActivityService {

    /**
     * Retrieve comprehensive activity statistics for a specific user.
     * <p>
     * Includes metrics such as quizzes participated, scores, session attendance,
     * and other relevant engagement data.
     * </p>
     *
     * @param userId the unique ID of the user
     * @return a UserActivityResponse containing all user activity statistics
     */
    UserActivityResponse getUserActivity(String userId);

    /**
     * Retrieve user activity statistics filtered by a specific time range.
     * <p>
     * Supported time ranges: "7days", "30days", "90days", "1year", "all".
     * Useful for tracking recent activity or trends over a defined period.
     * </p>
     *
     * @param userId the unique ID of the user
     * @param timeRange the time range to filter activity data
     * @return a UserActivityResponse with filtered activity statistics
     */
    UserActivityResponse getUserActivityByTimeRange(String userId, String timeRange);
}

