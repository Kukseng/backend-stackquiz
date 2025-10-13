package kh.edu.cstad.stackquizapi.dto.websocket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AnswerSubmissionMessage extends WebSocketMessage {

    @NotNull
    private String participantId; // Backend uses this for unique identification

    private String participantNickname; // Optional, for display in frontend

    @NotNull
    private String questionId;

    @NotNull
    private String selectedOptionId;

    @NotNull
    @Min(value = 0, message = "Response time must be non-negative")
    private Long responseTime;

    private Boolean isCorrect;
    private Integer pointsEarned;

    public AnswerSubmissionMessage(String sessionId, String senderNickname, String participantId,
                                   String participantNickname, String questionId,
                                   String selectedOptionId, Long responseTime) {
        super("ANSWER_SUBMISSION", sessionId, senderNickname);
        this.participantId = participantId;
        this.participantNickname = participantNickname;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.responseTime = responseTime;
    }

    public AnswerSubmissionMessage(String sessionId, String senderNickname, String participantId,
                                   String participantNickname, String questionId,
                                   String selectedOptionId, Long responseTime,
                                   Boolean isCorrect, Integer pointsEarned) {
        super("ANSWER_SUBMISSION", sessionId, senderNickname);
        this.participantId = participantId;
        this.participantNickname = participantNickname;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.responseTime = responseTime;
        this.isCorrect = isCorrect;
        this.pointsEarned = pointsEarned;
    }
}
