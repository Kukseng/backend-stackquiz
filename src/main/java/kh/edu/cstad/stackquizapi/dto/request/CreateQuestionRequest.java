package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.util.QuestionType;

import java.util.List;
import java.util.Set;

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

        String quizId

) {
}