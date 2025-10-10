package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * WebSocket message for broadcasting session statistics to all participants
 * Provides real-time session overview information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SessionStatsMessage extends WebSocketMessage {
    
    private int totalParticipants;
    private int activeParticipants;
    private int currentQuestion;
    private int totalQuestions;
    private double averageScore;
    private int highestScore;
    private String topPerformer;
    private long statsTimestamp;

    public SessionStatsMessage(String sessionCode, String sender, int totalParticipants, 
                              int activeParticipants, int currentQuestion, int totalQuestions,
                              double averageScore, int highestScore, String topPerformer, 
                              long statsTimestamp) {
        super("SESSION_STATS", sessionCode, sender);
        this.totalParticipants = totalParticipants;
        this.activeParticipants = activeParticipants;
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.topPerformer = topPerformer;
        this.statsTimestamp = statsTimestamp;
    }
}
