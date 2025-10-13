package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizCategory;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdate;
import kh.edu.cstad.stackquizapi.dto.response.CategoryResponse;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizMapper {

    @Mapping(target = "categories", source = "quizCategories") // map quizCategories → categories
    QuizResponse toQuizResponse(Quiz quiz);

    Quiz toQuizRequest(CreateQuizRequest createQuizRequest);

    // custom mapping: QuizCategory → CategoryResponse
    default CategoryResponse map(QuizCategory quizCategory) {
        if (quizCategory == null || quizCategory.getCategory() == null) {
            return null;
        }
        return new CategoryResponse(
                quizCategory.getCategory().getId(),
                quizCategory.getCategory().getName()
        );
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toQuizUpdateResponse(
            QuizUpdate quizUpdate,
            @MappingTarget Quiz quiz
    );

    // if needed
    QuestionResponse toQuestionResponse(Question question);
    OptionResponse toOptionResponse(Option option);
    List<QuestionResponse> toQuestionResponses(List<Question> questions);
    List<OptionResponse> toOptionResponses(List<Option> options);
}
