package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ParticipantMessage extends WebSocketMessage {
    private List<ParticipantResponse> participants;
    private Integer totalParticipants;
    private String action;

    public ParticipantMessage(String sessionId, String senderNickname, List<ParticipantResponse> participants,
                              Integer totalParticipants, String action) {
        super("PARTICIPANT", sessionId, senderNickname);
        this.participants = participants;
        this.totalParticipants = totalParticipants;
        this.action = action;
    }
}

