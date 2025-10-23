package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    @Mapping(target = "sessionCode", source = "session.sessionCode")
    @Mapping(target = "sessionName", source = "session.sessionName")
    ParticipantResponse toParticipantResponse(Participant participant);

    Participant toParticipant(JoinSessionRequest joinSessionRequest);

}
