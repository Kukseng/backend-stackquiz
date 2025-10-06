package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Builder;

@Builder
public record ParticipantProgressMessage(

        String sessionCode,

        String participantId,

        String participantNickname,

        Integer currentQuestion,

        Integer totalQuestions,

        Integer totalScore,

        Boolean isCompleted,

        String action, // "QUESTION_STARTED", "QUESTION_ANSWERED", "PARTICIPANT_COMPLETED"

        Long timestamp

) {
    public ParticipantProgressMessage {

        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}
