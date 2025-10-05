package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.util.HostCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class HostCommandMessage extends WebSocketMessage {


    private HostCommand command;

    private String hostNickname;

    private Object commandData;

    public HostCommandMessage(String sessionId,
                              String senderNickname,
                              HostCommand command,
                              String hostNickname,
                              Object commandData) {
        super("HOST_COMMAND", sessionId, senderNickname);
        this.command = command;
        this.hostNickname = hostNickname;
        this.commandData = commandData;
    }

}