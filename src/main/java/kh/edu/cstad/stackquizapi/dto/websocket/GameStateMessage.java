package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.util.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class GameStateMessage extends WebSocketMessage {
    private Status sessionStatus;
    private String action;
    private Integer currentQuestionIndex;
    private Integer totalQuestions;
    private Long remainingTime;
    private String hostMessage;

    public GameStateMessage(String sessionId, String senderNickname, Status sessionStatus, String action,
                            Integer currentQuestionIndex, Integer totalQuestions, Long remainingTime, String hostMessage) {
        super("GAME_STATE", sessionId, senderNickname);
        this.sessionStatus = sessionStatus;
        this.action = action;
        this.currentQuestionIndex = currentQuestionIndex;
        this.totalQuestions = totalQuestions;
        this.remainingTime = remainingTime;
        this.hostMessage = hostMessage;
    }
}
