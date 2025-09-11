package kh.edu.cstad.stackquizapi.dto.response;

public record FeedbackSummaryResponse(
        String sessionId,
        int totalFeedback
) {
}
