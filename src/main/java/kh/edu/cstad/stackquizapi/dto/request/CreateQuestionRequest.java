package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kh.edu.cstad.stackquizapi.util.QuestionType;

public record CreateQuestionRequest(

        @NotBlank(message = "Question text is required")
        String text,

        @NotNull(message = "Question type is required")
        QuestionType type,

        @NotBlank(message = "Quiz ID is required")
        String quizId

) {
}