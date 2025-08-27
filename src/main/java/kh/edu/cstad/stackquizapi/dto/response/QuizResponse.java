package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.VisibilityType;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public record QuizResponse(

        String id,

        String title,

        String description,

        String thumbnailUrl,

        VisibilityType visibility,

        LocalDateTime createdAt,

        QuizDifficultyType difficulty,

        LocalDateTime updatedAt


) {
}
