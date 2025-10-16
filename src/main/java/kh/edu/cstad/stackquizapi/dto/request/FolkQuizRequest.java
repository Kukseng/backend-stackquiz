package kh.edu.cstad.stackquizapi.dto.request;

import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.TimeLimitRangeInSecond;

public record FolkQuizRequest(

        String title,

        String description,

        String thumbnailUrl,

        String visibility,

        TimeLimitRangeInSecond questionTimeLimit,

        QuizDifficultyType difficulty

) {
}
