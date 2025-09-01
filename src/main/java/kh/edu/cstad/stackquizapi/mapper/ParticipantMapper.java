package kh.edu.cstad.stackquizapi.mapper;


import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    ParticipantResponse toParticipantResponse(Participant participant);

    Participant toParticipant(JoinSessionRequest joinSessionRequest);

}
