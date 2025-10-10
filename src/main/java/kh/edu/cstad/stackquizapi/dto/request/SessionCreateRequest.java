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

        LocalDateTime scheduledStartTime,
        LocalDateTime scheduledEndTime,

        @Min(value = 1, message = "Max attempts must be at least 1")
        Integer maxAttempts,

        Boolean allowJoinInProgress,
        Boolean shuffleQuestions,
        Boolean showCorrectAnswers,
        Boolean allowReview,


        Integer defaultQuestionTimeLimit,
        Integer sessionTimeLimit,


        Integer maxParticipants,
        Boolean requireNickname,
        Boolean allowAnonymous,


        Boolean showLeaderboard,
        Boolean showProgress,
        Boolean playSound,



        Boolean isPublic

) {
}