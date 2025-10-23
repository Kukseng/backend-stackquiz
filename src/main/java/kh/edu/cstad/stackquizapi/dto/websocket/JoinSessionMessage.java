package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinSessionMessage {
//    private String participantId; // optional: id returned by REST join
    private String nickname;
    private Long avatarId;     // optional for guests; use Integer to accept null
}
