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
    private String participantNickname;
    private String questionId;
    private String selectedOptionId;
    private Long responseTime;
    private Boolean isCorrect;
    private Integer pointsEarned;

    public AnswerSubmissionMessage(String sessionId, String senderNickname, String participantNickname,
                                   String questionId, String selectedOptionId, Long responseTime) {
        super("ANSWER_SUBMISSION", sessionId, senderNickname);
        this.participantNickname = participantNickname;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.responseTime = responseTime;
    }

    public AnswerSubmissionMessage(String sessionCode, String system, String nickname, String id, String selectedOptionId, @NotNull(message = "Time taken is required") @Min(value = 0, message = "Time taken must be non-negative") Integer integer) {
    }
}
