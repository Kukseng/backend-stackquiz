package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record QuizAnalyticsResponse(
        String quizId,

        // Main statistics
        Integer totalSessionsHosted,      // "100 times hosted"
        Integer totalParticipants,        // "Played by 5K students"
        Integer totalCompletions,

        // Additional metrics
        Double averageParticipantsPerSession,
        Integer peakParticipants,
        Long totalQuestionsAnswered,
        Long totalCorrectAnswers,
        Double overallAccuracyRate,

        // Timestamps
        LocalDateTime firstPlayedAt,
        LocalDateTime lastPlayedAt,
        LocalDateTime updatedAt,

        // Formatted strings for display
        String participantsDisplay,       // "5.0K participants"
        String sessionsDisplay,           // "100 sessions"
        String accuracyDisplay            // "85.5% accuracy"
) {

    public static QuizAnalyticsResponse from(
            String quizId,
            Integer totalSessionsHosted,
            Integer totalParticipants,
            Integer totalCompletions,
            Double averageParticipantsPerSession,
            Integer peakParticipants,
            Long totalQuestionsAnswered,
            Long totalCorrectAnswers,
            Double overallAccuracyRate,
            LocalDateTime firstPlayedAt,
            LocalDateTime lastPlayedAt,
            LocalDateTime updatedAt
    ) {
        return new QuizAnalyticsResponse(
                quizId,
                totalSessionsHosted,
                totalParticipants,
                totalCompletions,
                averageParticipantsPerSession,
                peakParticipants,
                totalQuestionsAnswered,
                totalCorrectAnswers,
                overallAccuracyRate,
                firstPlayedAt,
                lastPlayedAt,
                updatedAt,
                formatNumber(totalParticipants) + " participants",
                formatNumber(totalSessionsHosted) + " sessions",
                String.format("%.1f%% accuracy", overallAccuracyRate)
        );
    }

    /**
     * Format large numbers with K, M suffixes
     * 1000 → "1.0K"
     * 5000 → "5.0K"
     * 1000000 → "1.0M"
     */
    private static String formatNumber(Integer number) {
        if (number == null || number == 0) {
            return "0";
        }

        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        } else {
            return number.toString();
        }
    }
}

