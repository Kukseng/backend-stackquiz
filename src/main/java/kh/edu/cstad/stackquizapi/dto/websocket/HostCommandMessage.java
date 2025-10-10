package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.util.HostCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostCommandMessage {
    private HostCommand command;
    private String sessionId;

    // ✅ ADDED: Session settings for START_SESSION command
    private SessionSettings settings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSettings {
        private LocalDateTime scheduledStartTime;
        private LocalDateTime scheduledEndTime;
        private String mode;  // "SYNC" or "ASYNC"
        private Integer maxAttempts;
        private Boolean allowJoinInProgress;
        private Boolean shuffleQuestions;
        private Boolean showCorrectAnswers;
        private Integer defaultQuestionTimeLimit;
        private Integer maxParticipants;
    }
}