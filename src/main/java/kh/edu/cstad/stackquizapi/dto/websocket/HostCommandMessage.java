
package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HostCommandMessage extends WebSocketMessage {
    private String command;
    private String hostNickname;
    private Object commandData;

    public HostCommandMessage(String sessionId, String senderNickname, String command, String hostNickname, Object commandData) {
        super("HOST_COMMAND", sessionId, senderNickname);
        this.command = command;
        this.hostNickname = hostNickname;
        this.commandData = commandData;
    }
}

