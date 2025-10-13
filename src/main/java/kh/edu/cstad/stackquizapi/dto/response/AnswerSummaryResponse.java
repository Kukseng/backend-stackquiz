package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AnswerSummaryResponse(

        String participantId,

        String participantNickname,

        String sessionId,

        Integer totalAnswered,

        Integer correctAnswers,

        Integer incorrectAnswers,

        Double accuracyRate,

        Integer totalScore,

        Double averageResponseTime,

        LocalDateTime lastAnswered

) {}