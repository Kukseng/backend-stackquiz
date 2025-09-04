package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HostCommandMessage extends WebSocketMessage {
    private String command;
    private String hostId;
    private Object commandData;

    public HostCommandMessage(String sessionId, String senderId, String command, String hostId, Object commandData) {
        super("HOST_COMMAND", sessionId, senderId);
        this.command = command;
        this.hostId = hostId;
        this.commandData = commandData;
    }
}
