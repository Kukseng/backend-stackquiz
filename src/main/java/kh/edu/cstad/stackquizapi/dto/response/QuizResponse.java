package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.VisibilityType;

import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(

        String id,

        String title,

        String description,

        String thumbnailUrl,

        VisibilityType visibility,

        LocalDateTime createdAt,

        QuizDifficultyType difficulty,

        LocalDateTime updatedAt,

        List<Question> questions

) {
}
