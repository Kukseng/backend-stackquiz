package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnswerFeedbackMessage extends WebSocketMessage {
    private String participantId;
    private String questionId;
    private String selectedOptionId;
    private String correctOptionId;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private Integer timeTaken;
    private Integer newTotalScore;
    private Integer currentRank;
    private String explanation;
    
    public AnswerFeedbackMessage(String sessionId, String senderNickname, String participantId,
                                String questionId, String selectedOptionId, String correctOptionId,
                                Boolean isCorrect, Integer pointsEarned, Integer timeTaken,
                                Integer newTotalScore, Integer currentRank, String explanation) {
        super("ANSWER_FEEDBACK", sessionId, senderNickname);
        this.participantId = participantId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionId = correctOptionId;
        this.isCorrect = isCorrect;
        this.pointsEarned = pointsEarned;
        this.timeTaken = timeTaken;
        this.newTotalScore = newTotalScore;
        this.currentRank = currentRank;
        this.explanation = explanation;
    }
}
