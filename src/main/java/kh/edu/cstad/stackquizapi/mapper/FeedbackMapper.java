package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Feedback;
import kh.edu.cstad.stackquizapi.dto.response.FeedbackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "quizId", source = "quiz.id")        // map Feedback.quiz.id → DTO.quizId
    @Mapping(target = "sessionId", source = "session.id") // map Feedback.session.id → DTO.sessionId
    FeedbackResponse toDto(Feedback feedback);        // Convert Feedback entity into FeedbackResponse
}
