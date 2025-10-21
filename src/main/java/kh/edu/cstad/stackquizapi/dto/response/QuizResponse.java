package kh.edu.cstad.stackquizapi.dto.response;

import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.QuizStatus;
import kh.edu.cstad.stackquizapi.util.TimeLimitRangeInSecond;
import kh.edu.cstad.stackquizapi.util.VisibilityType;
import java.time.LocalDateTime;
import java.util.List;

public record QuizResponse(

        String id,

        String title,

        String description,

        String thumbnailUrl,

        List<CategoryResponse> categories,

        VisibilityType visibility,

        QuizStatus status,

        TimeLimitRangeInSecond questionTimeLimit,

        LocalDateTime createdAt,

        QuizDifficultyType difficulty,

        LocalDateTime updatedAt,

        List<QuestionResponse> questions,

        // Analytics fields
        Integer totalSessionsHosted,
        Integer totalParticipants,
        String participantsDisplay,
        String sessionsDisplay

) {
}
