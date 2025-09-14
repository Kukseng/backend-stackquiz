package kh.edu.cstad.stackquizapi.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ParticipantAnswerResponse(

        String answerId,

        String participantId,

        String participantNickname,

        String questionId,

        String questionText,

        String optionId,

        String optionText,

        String answerText,

        Boolean isCorrect,

        Integer timeTaken,

        Integer pointsEarned,

        LocalDateTime answeredAt,

        String sessionId

) {}