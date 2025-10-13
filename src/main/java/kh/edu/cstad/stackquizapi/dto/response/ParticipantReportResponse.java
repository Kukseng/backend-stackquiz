package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record ParticipantReportResponse(
        String participantId,
        String nickname,
        Integer totalScore,
        Double accuracy,
        Integer correctAnswers,
        Integer incorrectAnswers,
        Integer unattemptedQuestions
) {
}

