package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class WebSocketMessage {

    private String messageType;

    private String sessionId;

    private LocalDateTime timestamp;

    private String senderId;

    public WebSocketMessage(String messageType, String sessionId, String senderId) {
        this.messageType = messageType;
        this.sessionId = sessionId;
        this.senderId = senderId;
        this.timestamp = LocalDateTime.now();
    }
}
