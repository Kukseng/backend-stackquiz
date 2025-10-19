package kh.edu.cstad.stackquizapi.dto.request;

import jakarta.validation.constraints.*;
import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.QuizStatus;
import kh.edu.cstad.stackquizapi.util.TimeLimitRangeInSecond;
import kh.edu.cstad.stackquizapi.util.VisibilityType;

import java.util.List;

public record CreateQuizRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Description must not be empty")
        String description,

        @NotNull(message = "Visibility must be provided")
        VisibilityType visibility,

        @NotNull(message = "Status is required")
        QuizStatus status,

        @NotNull(message = "Question time limit must be provided")
        TimeLimitRangeInSecond questionTimeLimit,

        @NotNull(message = "Difficulty must be provided")
        QuizDifficultyType difficulty,

        @NotEmpty(message = "Category IDs must not be empty")
        List<@NotBlank(message = "Category ID cannot be blank") String> categoryIds

) {
}
