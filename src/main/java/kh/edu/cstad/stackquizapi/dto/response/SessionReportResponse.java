//package kh.edu.cstad.stackquizapi.dto.response;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import lombok.Builder;
//
//import java.time.LocalDateTime;
//
//@Builder
//public record SessionReportResponse(
//
//        String reportId,
//
//        String sessionId,
//
//        String sessionName,
//
//        String hostName,
//
//        LocalDateTime generatedAt,
//
//        JsonNode sessionOverview,
//
//        JsonNode questionBreakdown,
//
//        JsonNode participantDetails,
//
//        JsonNode finalRankings
//
//) {}

package kh.edu.cstad.stackquizapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReportResponse {

    // Session Overview
    private String sessionId;
    private String sessionCode;
    private String sessionName;
    private String hostName;
    private String quizTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private String status;

    // Session Statistics
    private SessionStatistics statistics;

    // Question Analysis
    private List<QuestionAnalysis> questionAnalysis;

    // Participant Reports
    private List<ParticipantReport> participantReports;

    // Performance Insights
    private PerformanceInsights insights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStatistics {
        private Integer totalParticipants;
        private Integer completedParticipants;
        private Integer totalQuestions;
        private Double averageScore;
        private Double averageAccuracy;
        private Double averageResponseTime;
        private Integer totalAnswers;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Integer unansweredQuestions;
        private Double completionRate;
        private Double engagementRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnalysis {
        private String questionId;
        private String questionText;
        private Integer questionNumber;
        private String questionType;
        private Integer timeLimit;

        // Question Statistics
        private Integer totalResponses;
        private Integer correctResponses;
        private Integer incorrectResponses;
        private Integer noResponses;
        private Double accuracyRate;
        private Double averageResponseTime;
        private String difficulty; // "Easy", "Medium", "Hard" based on accuracy

        // Answer Distribution
        private List<OptionAnalysis> optionAnalysis;
        private String correctOptionId;
        private String explanation;

        // Performance Metrics
        private Double discriminationIndex; // How well question separates high/low performers
        private List<String> commonMistakes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionAnalysis {
        private String optionId;
        private String optionText;
        private Boolean isCorrect;
        private Integer responseCount;
        private Double responsePercentage;
        private Double averageResponseTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantReport {
        private String participantId;
        private String nickname;
        private String avatarId;
        private LocalDateTime joinTime;
        private LocalDateTime lastActivity;

        // Overall Performance
        private Integer totalScore;
        private Integer finalRank;
        private Double accuracyRate;
        private Double averageResponseTime;
        private Integer questionsAnswered;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Integer skippedQuestions;
        private String completionStatus; // "COMPLETED", "PARTIAL", "ABANDONED"

        // Detailed Answers
        private List<ParticipantAnswer> answers;

        // Performance Analysis
        private PerformanceMetrics performance;

        // Streaks and Patterns
        private Integer longestCorrectStreak;
        private Integer longestIncorrectStreak;
        private List<String> strongTopics;
        private List<String> weakTopics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantAnswer {
        private String questionId;
        private Integer questionNumber;
        private String questionText;
        private String selectedOptionId;
        private String selectedOptionText;
        private String correctOptionId;
        private String correctOptionText;
        private Boolean isCorrect;
        private Integer pointsEarned;
        private Integer maxPoints;
        private Double responseTime;
        private LocalDateTime answeredAt;
        private String answerStatus; // "CORRECT", "INCORRECT", "SKIPPED", "TIME_UP"
        private String explanation;

        // Answer Quality Metrics
        private String responseSpeed; // "VERY_FAST", "FAST", "NORMAL", "SLOW", "VERY_SLOW"
        private Boolean wasGuessed; // Based on response time analysis
        private Integer attemptNumber; // If multiple attempts allowed
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private String performanceLevel; // "EXCELLENT", "GOOD", "AVERAGE", "BELOW_AVERAGE", "POOR"
        private Double consistencyScore; // How consistent the participant's performance was
        private Double improvementTrend; // Whether performance improved over time
        private Double speedAccuracyBalance; // Balance between speed and accuracy
        private List<String> strengths;
        private List<String> areasForImprovement;
        private String recommendedActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceInsights {
        // Session-wide insights
        private List<String> topPerformers; // Top 3 participants
        private List<String> mostImprovedParticipants;
        private List<String> strugglingParticipants;

        // Question insights
        private QuestionAnalysis easiestQuestion;
        private QuestionAnalysis hardestQuestion;
        private QuestionAnalysis mostSkippedQuestion;
        private QuestionAnalysis fastestAnsweredQuestion;

        // Timing insights
        private String peakParticipationTime;
        private String averageSessionDuration;
        private Double dropoffRate;

        // Recommendations
        private List<String> hostRecommendations;
        private List<String> contentRecommendations;
        private Map<String, String> participantFeedback; // participantId -> feedback
    }
}