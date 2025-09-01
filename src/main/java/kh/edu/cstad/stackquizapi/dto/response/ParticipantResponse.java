package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record ParticipantResponse(

        String participantId,

        String nickname,

        String sessionCode,

        String sessionName,

        Integer totalScore,

        LocalDateTime joinedAt,

        Integer currentPosition

) {
}
