package kh.edu.cstad.stackquizapi.dto.response;

import java.time.LocalDateTime;

public record ParticipantResponse(

        String id,

        String nickname,

        String sessionCode,

        String sessionName,

        Integer totalScore,

        LocalDateTime joinedAt

) {
}
