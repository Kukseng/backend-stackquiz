package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ScoreUpdateMessage extends WebSocketMessage {
    private String participantId;
    private String participantNickname;
    private Integer previousScore;
    private Integer newScore;
    private Integer pointsEarned;
    private Integer currentRank;
    private Integer previousRank;
    private Boolean isCorrect;
    private String questionId;
    
    public ScoreUpdateMessage(String sessionId, String senderNickname, String participantId,
                             String participantNickname, Integer previousScore, Integer newScore,
                             Integer pointsEarned, Integer currentRank, Integer previousRank,
                             Boolean isCorrect, String questionId) {
        super("SCORE_UPDATE", sessionId, senderNickname);
        this.participantId = participantId;
        this.participantNickname = participantNickname;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.pointsEarned = pointsEarned;
        this.currentRank = currentRank;
        this.previousRank = previousRank;
        this.isCorrect = isCorrect;
        this.questionId = questionId;
    }
}
