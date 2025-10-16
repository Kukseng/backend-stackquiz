package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kh.edu.cstad.stackquizapi.util.QuestionType;

public record UpdateQuestionRequest(

        @Size(max = 65535, message = "Question text is too long")
        String text,

        QuestionType type,

        @Min(value = 1, message = "Question order must be positive")
        Integer questionOrder,

        @Min(value = 1, message = "Time limit must be at least 1 second")
        Integer timeLimit,

        @Min(value = 0, message = "Points cannot be negative")
        Integer points,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl

) {}
