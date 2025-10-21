package kh.edu.cstad.stackquizapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Kahoot-style question analytics for SYNC mode
 * Shows statistics after each question before advancing to the next
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnalyticsResponse {
    
    // Session info
    private String sessionCode;
    private Integer currentQuestionNumber;
    private Integer totalQuestions;
    
    // Question info
    private String questionId;
    private String questionText;
    private String correctOptionId;
    
    // Participation statistics
    private Integer totalParticipants;
    private Integer participantsAnswered;
    private Integer participantsNotAnswered;
    private Double participationRate; // percentage
    
    // Answer statistics
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Double accuracyRate; // percentage
    
    // Answer distribution by option
    private Map<String, OptionStats> optionStatistics;
    
    // Top 3 leaderboard
    private List<LeaderboardEntry> top3;
    
    // Timing statistics
    private Double averageResponseTime; // in seconds
    private Double fastestResponseTime; // in seconds
    
    /**
     * Statistics for each answer option
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionStats {
        private String optionId;
        private String optionText;
        private Boolean isCorrect;
        private Integer count;
        private Double percentage;
    }
    
    /**
     * Leaderboard entry for top 3
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private String participantId;
        private String nickname;
        private String avatarId;
        private Integer totalScore;
        private Integer correctAnswers;
        private Integer streak;
    }
}

