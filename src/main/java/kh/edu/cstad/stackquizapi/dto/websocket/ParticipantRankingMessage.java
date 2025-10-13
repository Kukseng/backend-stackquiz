package kh.edu.cstad.stackquizapi.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ParticipantRankingMessage extends WebSocketMessage {
    private String participantId;
    private String participantNickname;
    private Integer currentRank;
    private Integer previousRank;
    private Integer totalScore;
    private Integer totalParticipants;
    private Boolean isTopPerformer;
    private Integer pointsBehindLeader;
    private Integer pointsAheadOfNext;
    private String rankChange; // "UP", "DOWN", "SAME", "NEW"
    
    public ParticipantRankingMessage(String sessionId, String senderNickname, String participantId,
                                   String participantNickname, Integer currentRank, Integer previousRank,
                                   Integer totalScore, Integer totalParticipants, Boolean isTopPerformer,
                                   Integer pointsBehindLeader, Integer pointsAheadOfNext, String rankChange) {
        super("PARTICIPANT_RANKING", sessionId, senderNickname);
        this.participantId = participantId;
        this.participantNickname = participantNickname;
        this.currentRank = currentRank;
        this.previousRank = previousRank;
        this.totalScore = totalScore;
        this.totalParticipants = totalParticipants;
        this.isTopPerformer = isTopPerformer;
        this.pointsBehindLeader = pointsBehindLeader;
        this.pointsAheadOfNext = pointsAheadOfNext;
        this.rankChange = rankChange;
    }
}
