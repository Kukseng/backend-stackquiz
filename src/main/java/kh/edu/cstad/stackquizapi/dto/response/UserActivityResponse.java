package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for user activity and analytics
 * Contains all data needed for dashboard graphs and statistics
 */
@Builder
public record UserActivityResponse(
        // User info
        String userId,
        String username,
        
        // Overall statistics
        Integer totalQuizzesCreated,
        Integer totalSessionsStarted,
        Integer totalParticipants,
        Integer totalQuestionsCreated,
        Integer activeSessionsCount,
        Integer completedSessionsCount,
        
        // Engagement metrics
        Double averageParticipantsPerSession,
        Double averageQuestionsPerQuiz,
        Double sessionCompletionRate,
        Integer totalAnswersReceived,
        
        // Time-based statistics
        Integer quizzesCreatedThisWeek,
        Integer quizzesCreatedThisMonth,
        Integer sessionsStartedThisWeek,
        Integer sessionsStartedThisMonth,
        
        // Most popular quiz
        MostPopularQuiz mostPopularQuiz,
        
        // Recent activity
        List<RecentActivity> recentActivities,
        
        // Time series data for graphs
        List<TimeSeriesData> quizCreationTimeSeries,
        List<TimeSeriesData> sessionActivityTimeSeries,
        List<TimeSeriesData> participantGrowthTimeSeries,
        
        // Category breakdown
        Map<String, Integer> quizzesByCategory,
        Map<String, Integer> quizzesByDifficulty,
        
        // Session statistics
        Map<String, Integer> sessionsByMode, // SYNC vs ASYNC
        Map<String, Integer> sessionsByStatus, // WAITING, IN_PROGRESS, COMPLETED, ENDED
        
        // Peak activity times
        Map<String, Integer> activityByDayOfWeek,
        Map<String, Integer> activityByHourOfDay,
        
        // Timestamps
        LocalDateTime firstQuizCreatedAt,
        LocalDateTime lastActivityAt,
        Long memberSince // Days since first quiz
) {
    /**
     * Most popular quiz information
     */
    @Builder
    public record MostPopularQuiz(
            String quizId,
            String title,
            Integer totalSessions,
            Integer totalParticipants,
            Double averageScore
    ) {}
    
    /**
     * Recent activity item
     */
    @Builder
    public record RecentActivity(
            String activityType, // QUIZ_CREATED, SESSION_STARTED, SESSION_ENDED
            String description,
            LocalDateTime timestamp,
            Map<String, Object> metadata
    ) {}
    
    /**
     * Time series data point for graphs
     */
    @Builder
    public record TimeSeriesData(
            String date, // ISO date string (YYYY-MM-DD)
            Integer count,
            String label // Human-readable label
    ) {}
}

