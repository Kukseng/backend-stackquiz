package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnswerSubmissionMessage extends WebSocketMessage {
    private String participantId;
    private String questionId;
    private String selectedOptionId;
    private Long responseTime;
    private Boolean isCorrect;
    private Integer pointsEarned;

    public AnswerSubmissionMessage(String sessionId, String senderId, String participantId,
                                   String questionId, String selectedOptionId, Long responseTime) {
        super("ANSWER_SUBMISSION", sessionId, senderId);
        this.participantId = participantId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.responseTime = responseTime;
    }
}
