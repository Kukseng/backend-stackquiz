package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kh.edu.cstad.stackquizapi.util.QuestionType;

public record CreateQuestionRequest(

        @NotBlank(message = "Question text is required")
        String text,

        @NotNull(message = "Question type is required")
        QuestionType type,

        @NotNull(message = "Time limit is required")
        @Min(value = 1, message = "Time limit must be at least 1 second")
        Integer timeLimit,

        @NotNull(message = "Points is required")
        @Min(value = 0, message = "Points must be non-negative")
        Integer points,

        String imageUrl,

        @NotBlank(message = "Quiz ID is required")
        String quizId

) {
}