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
        Integer currentRank,
        Boolean isCompleted,
        String action,
        Long timestamp
) {
    public ParticipantProgressMessage {
        // Auto-fill timestamp if not provided
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
    }
}
