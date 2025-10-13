package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public abstract class WebSocketMessage {
    private String messageType;
    private String sessionId;
    private String senderNickname; // Change senderId to senderNickname for clarity
    private LocalDateTime timestamp;

    protected WebSocketMessage(String messageType, String sessionId, String senderNickname) {
        this.messageType = messageType;
        this.sessionId = sessionId;
        this.senderNickname = senderNickname;
        this.timestamp = LocalDateTime.now();
    }
}
