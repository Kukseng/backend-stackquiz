package kh.edu.cstad.stackquizapi.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T22:04:26+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class ParticipantMapperImpl implements ParticipantMapper {

    @Override
    public ParticipantResponse toParticipantResponse(Participant participant) {
        if ( participant == null ) {
            return null;
        }

        String sessionCode = null;
        String sessionName = null;
        String id = null;
        String nickname = null;
        Integer totalScore = null;
        LocalDateTime joinedAt = null;

        sessionCode = participantSessionSessionCode( participant );
        sessionName = participantSessionSessionName( participant );
        id = participant.getId();
        nickname = participant.getNickname();
        totalScore = participant.getTotalScore();
        joinedAt = participant.getJoinedAt();

        ParticipantResponse participantResponse = new ParticipantResponse( id, nickname, sessionCode, sessionName, totalScore, joinedAt );

        return participantResponse;
    }

    @Override
    public Participant toParticipant(JoinSessionRequest joinSessionRequest) {
        if ( joinSessionRequest == null ) {
            return null;
        }

        Participant participant = new Participant();

        participant.setNickname( joinSessionRequest.nickname() );

        return participant;
    }

    private String participantSessionSessionCode(Participant participant) {
        QuizSession session = participant.getSession();
        if ( session == null ) {
            return null;
        }
        return session.getSessionCode();
    }

    private String participantSessionSessionName(Participant participant) {
        QuizSession session = participant.getSession();
        if ( session == null ) {
            return null;
        }
        return session.getSessionName();
    }
}
