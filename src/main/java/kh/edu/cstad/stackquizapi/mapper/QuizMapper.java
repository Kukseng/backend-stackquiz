package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel ="spring")
public interface QuizMapper {

        /***
     * Response Quiz Mapper
     * Authors Kukseng
     * @param quiz
     * @return
     */
    QuizResponse toQuizResponse(Quiz quiz);

    Quiz toQuizRequest(CreateQuizRequest createQuizRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toQuizUpdateResponse(
            QuizUpdate quizUpdate,
            @MappingTarget Quiz quiz
    );
}

