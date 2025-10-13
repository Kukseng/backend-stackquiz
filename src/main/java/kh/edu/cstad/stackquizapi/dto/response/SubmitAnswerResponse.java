package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SubmitAnswerResponse(

        String answerId,

        String participantId,

        String questionId,

        String selectedAnswerId,

        boolean isCorrect,

        int pointsEarned,

        int timeTaken,

        LocalDateTime answeredAt,

        int newTotalScore

) {
}
