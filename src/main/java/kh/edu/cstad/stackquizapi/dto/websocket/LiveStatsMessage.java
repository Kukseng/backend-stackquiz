package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LiveStatsMessage extends WebSocketMessage {
    private Integer totalParticipants;
    private Integer activeParticipants;
    private Integer currentQuestion;
    private Integer totalQuestions;
    private Map<String, Integer> answerDistribution; // optionId -> count
    private Double averageResponseTime;
    private Double accuracyRate;
    private Integer fastestResponseTime;
    private Integer slowestResponseTime;
    private String leadingParticipant;
    private Integer highestScore;
    
    public LiveStatsMessage(String sessionId, String senderNickname, Integer totalParticipants,
                           Integer activeParticipants, Integer currentQuestion, Integer totalQuestions,
                           Map<String, Integer> answerDistribution, Double averageResponseTime,
                           Double accuracyRate, Integer fastestResponseTime, Integer slowestResponseTime,
                           String leadingParticipant, Integer highestScore) {
        super("LIVE_STATS", sessionId, senderNickname);
        this.totalParticipants = totalParticipants;
        this.activeParticipants = activeParticipants;
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.answerDistribution = answerDistribution;
        this.averageResponseTime = averageResponseTime;
        this.accuracyRate = accuracyRate;
        this.fastestResponseTime = fastestResponseTime;
        this.slowestResponseTime = slowestResponseTime;
        this.leadingParticipant = leadingParticipant;
        this.highestScore = highestScore;
    }
}
