package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HostProgressMessage extends WebSocketMessage {
    private Integer currentQuestion;
    private Integer totalQuestions;
    private Integer totalParticipants;
    private Integer participantsAnswered;
    private Integer participantsRemaining;
    private List<ParticipantProgress> participantProgress;
    private SessionStatistics statistics;
    
    public HostProgressMessage(String sessionId, String senderNickname, Integer currentQuestion,
                              Integer totalQuestions, Integer totalParticipants, Integer participantsAnswered,
                              Integer participantsRemaining, List<ParticipantProgress> participantProgress,
                              SessionStatistics statistics) {
        super("HOST_PROGRESS", sessionId, senderNickname);
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.totalParticipants = totalParticipants;
        this.participantsAnswered = participantsAnswered;
        this.participantsRemaining = participantsRemaining;
        this.participantProgress = participantProgress;
        this.statistics = statistics;
    }
    
    @Data
    @NoArgsConstructor
    public static class ParticipantProgress {
        private String participantId;
        private String nickname;
        private Integer currentQuestion;
        private Integer totalScore;
        private Integer rank;
        private Boolean isAnswering;
        private Boolean hasAnsweredCurrentQuestion;
        private Long lastActivityTime;
        
        public ParticipantProgress(String participantId, String nickname, Integer currentQuestion,
                                 Integer totalScore, Integer rank, Boolean isAnswering,
                                 Boolean hasAnsweredCurrentQuestion, Long lastActivityTime) {
            this.participantId = participantId;
            this.nickname = nickname;
            this.currentQuestion = currentQuestion;
            this.totalScore = totalScore;
            this.rank = rank;
            this.isAnswering = isAnswering;
            this.hasAnsweredCurrentQuestion = hasAnsweredCurrentQuestion;
            this.lastActivityTime = lastActivityTime;
        }
    }
    
    @Data
    @NoArgsConstructor
    public static class SessionStatistics {
        private Double averageScore;
        private Double averageResponseTime;
        private Integer totalAnswers;
        private Integer correctAnswers;
        private Double accuracyRate;
        private String topPerformer;
        
        public SessionStatistics(Double averageScore, Double averageResponseTime, Integer totalAnswers,
                               Integer correctAnswers, Double accuracyRate, String topPerformer) {
            this.averageScore = averageScore;
            this.averageResponseTime = averageResponseTime;
            this.totalAnswers = totalAnswers;
            this.correctAnswers = correctAnswers;
            this.accuracyRate = accuracyRate;
            this.topPerformer = topPerformer;
        }
    }
}
