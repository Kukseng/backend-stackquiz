package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import kh.edu.cstad.stackquizapi.util.QuizMode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SessionCreateRequest(

        @NotBlank(message = "Quiz ID is required")
        String quizId,

        String sessionName,
        QuizMode mode,
        // Kahoot-style quiz settings
        LocalDateTime scheduledStartTime,
        LocalDateTime scheduledEndTime,

        @Min(value = 1, message = "Max attempts must be at least 1")
        Integer maxAttempts,

        Boolean allowJoinInProgress,
        Boolean shuffleQuestions,
        Boolean showCorrectAnswers,
        Boolean allowReview,

        // Time settings
        Integer defaultQuestionTimeLimit,
        Integer sessionTimeLimit, // Total session time limit in minutes

        // Participation settings
        Integer maxParticipants,
        Boolean requireNickname,
        Boolean allowAnonymous,

        // Display settings
        Boolean showLeaderboard,
        Boolean showProgress,
        Boolean playSound,

        // Advanced settings
        String description,
        String[] tags,
        Boolean isPublic,
        String password // For private sessions
) {
}