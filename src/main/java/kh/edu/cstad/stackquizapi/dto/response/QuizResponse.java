package kh.edu.cstad.stackquizapi.dto.response;

import jakarta.validation.constraints.NotBlank;
import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.VisibilityType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(

        String id,

        String title,

        String description,

        String thumbnailUrl,

        List<CategoryResponse> categories,

        VisibilityType visibility,

        LocalDateTime createdAt,

        QuizDifficultyType difficulty,

        LocalDateTime updatedAt,
        List<QuestionResponse> questions


) {
}
