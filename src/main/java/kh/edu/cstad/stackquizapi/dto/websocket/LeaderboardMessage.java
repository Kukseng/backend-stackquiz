package kh.edu.cstad.stackquizapi.dto.websocket;

import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LeaderboardMessage extends WebSocketMessage {
    private LeaderboardResponse leaderboard;
    private String updateType;

    public LeaderboardMessage(String sessionId, String senderNickname, LeaderboardResponse leaderboard, String updateType) {
        super("LEADERBOARD", sessionId, senderNickname);
        this.leaderboard = leaderboard;
        this.updateType = updateType;
    }
}
