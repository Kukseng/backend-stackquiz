package kh.edu.cstad.stackquizapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Admin Dashboard Statistics
 * Provides overview of all sessions and participants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    // Session Statistics
    private Long totalSessions;
    private Long activeSessions;
    private Long completedSessions;
    private Long scheduledSessions;

    // Participant Statistics
    private Long totalParticipants;
    private Long activeParticipants;
    private Long totalUniqueParticipants;

    // Quiz Statistics
    private Long totalQuizzes;
    private Long totalQuestions;
    private Long totalAnswers;

    // Engagement Metrics
    private Double averageParticipantsPerSession;
    private Double averageSessionDuration; // in minutes
    private Double overallAccuracyRate; // percentage

    // Time-based Statistics
    private Long sessionsToday;
    private Long sessionsThisWeek;
    private Long sessionsThisMonth;
    private Long participantsToday;
    private Long participantsThisWeek;
    private Long participantsThisMonth;

    // Popular Quizzes (Top 5)
    private java.util.List<PopularQuizStats> topQuizzes;

    // Recent Activity
    private LocalDateTime lastSessionCreated;
    private LocalDateTime lastSessionCompleted;
    private String mostActiveHost;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularQuizStats {
        private String quizId;
        private String quizTitle;
        private Long timesPlayed;
        private Long totalParticipants;
        private Double averageAccuracy;
    }
}

