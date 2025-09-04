package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

@Builder
public record QuestionStatisticsResponse(

        String questionId,

        String questionText,

        String sessionId,

        Integer totalResponses,

        Integer correctAnswers,

        Integer incorrectAnswers,

        Double accuracyRate,

        Double averageResponseTime,

        Integer maxPoints,

        Double averagePointsEarned

) {}