package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record FeedbackResponse(
        String id,
        String comment,
        Integer rating,
        String quizId,
        String sessionId,
        LocalDateTime createdAt
) {
}
