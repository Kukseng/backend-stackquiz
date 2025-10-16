package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "participantAnswers", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    Question fromCreateQuestionRequest(CreateQuestionRequest request);

    QuestionResponse toQuestionResponse(Question question);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(new java.sql.Timestamp(System.currentTimeMillis()))")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "participantAnswers", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    void toQuestionPartially(
            UpdateQuestionRequest updateCustomerRequest,
            @MappingTarget Question question
    );
}
