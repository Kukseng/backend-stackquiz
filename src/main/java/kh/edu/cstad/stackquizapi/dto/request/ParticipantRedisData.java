package kh.edu.cstad.stackquizapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRedisData {

    private String participantId;

    private String nickname;

    private int score;

    private long lastUpdated;

}

