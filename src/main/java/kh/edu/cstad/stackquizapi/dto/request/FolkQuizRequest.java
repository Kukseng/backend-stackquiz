package kh.edu.cstad.stackquizapi.dto.request;

import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.TimeLimitRangeInSecond;
import kh.edu.cstad.stackquizapi.util.VisibilityType;

public record FolkQuizRequest(

        String title,

        String description,

        String thumbnailUrl,

        VisibilityType visibility,

        TimeLimitRangeInSecond questionTimeLimit,

        QuizDifficultyType difficulty

) {
}
