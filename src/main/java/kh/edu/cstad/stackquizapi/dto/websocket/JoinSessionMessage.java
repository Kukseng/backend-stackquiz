package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket payload for join events.
 * participantId is optional — if provided, server will NOT create a new Participant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionMessage {

    private String nickname;

    private Long avatarId;

}
