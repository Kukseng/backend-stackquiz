package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SessionTimerMessage extends WebSocketMessage {
    
    private String timerType; // SESSION, QUESTION, BREAK
    private String timerStatus; // RUNNING, PAUSED, STOPPED, EXPIRED
    private int remainingSeconds;
    private int totalSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int currentQuestion;
    private int totalQuestions;
    private boolean isAutoAdvance;
    private String nextAction; // NEXT_QUESTION, END_SESSION, etc.
    private long timerTimestamp;

    public SessionTimerMessage(String sessionCode, String sender, String timerType, 
                              String timerStatus, int remainingSeconds, int totalSeconds,
                              LocalDateTime startTime, LocalDateTime endTime,
                              int currentQuestion, int totalQuestions, boolean isAutoAdvance,
                              String nextAction, long timerTimestamp) {
        super("SESSION_TIMER", sessionCode, sender);
        this.timerType = timerType;
        this.timerStatus = timerStatus;
        this.remainingSeconds = remainingSeconds;
        this.totalSeconds = totalSeconds;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentQuestion = currentQuestion;
        this.totalQuestions = totalQuestions;
        this.isAutoAdvance = isAutoAdvance;
        this.nextAction = nextAction;
        this.timerTimestamp = timerTimestamp;
    }
}
