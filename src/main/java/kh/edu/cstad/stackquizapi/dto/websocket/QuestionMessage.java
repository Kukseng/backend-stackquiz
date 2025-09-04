package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class QuestionMessage extends WebSocketMessage {
    private QuestionResponse question;
    private Integer questionNumber;
    private Integer totalQuestions;
    private Integer timeLimit;
    private String action;

    public QuestionMessage(String sessionId, String senderId, QuestionResponse question,
                           Integer questionNumber, Integer totalQuestions, Integer timeLimit, String action) {
        super("QUESTION", sessionId, senderId);
        this.question = question;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
        this.timeLimit = timeLimit;
        this.action = action;
    }
}
