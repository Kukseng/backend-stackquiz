package kh.edu.cstad.stackquizapi.dto.request;

import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;

import java.util.List;

public record CreateQuizRequest(

        String title,

        String description,

        String thumbnailUrl,

        String visibility,

        QuizDifficultyType difficulty,

        List<String> categoryIds

) {
}
