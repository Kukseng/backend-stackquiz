package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * @author Kukseng
 */
@Mapper(componentModel = "spring")
public interface QuizMapper {

    QuizResponse toQuizResponse(Quiz quiz);

    Quiz toQuizRequest(CreateQuizRequest createQuizRequest);

    QuestionResponse toQuestionResponse(Question question);

    OptionResponse toOptionResponse(Option option);

    List<QuestionResponse> toQuestionResponses(List<Question> questions);

    List<OptionResponse> toOptionResponses(List<Option> options);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toQuizUpdateResponse(
            QuizUpdate quizUpdate,
            @MappingTarget Quiz quiz
    );

}

