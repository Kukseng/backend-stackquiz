package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizSessionMapper {

    @Mapping(source = "quiz.title", target = "quizTitle")
    SessionResponse toSessionResponse(QuizSession quizSession);

    QuizSession toSessionRequest(SessionCreateRequest sessionCreateRequest);

}
