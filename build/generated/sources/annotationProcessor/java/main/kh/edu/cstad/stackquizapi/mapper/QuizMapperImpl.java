package kh.edu.cstad.stackquizapi.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
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
import kh.edu.cstad.stackquizapi.util.QuestionType;
import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.QuizStatus;
import kh.edu.cstad.stackquizapi.util.TimeLimitRangeInSecond;
import kh.edu.cstad.stackquizapi.util.VisibilityType;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T22:04:26+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class QuizMapperImpl implements QuizMapper {

    @Override
    public QuizResponse toQuizResponse(Quiz quiz) {
        if ( quiz == null ) {
            return null;
        }

        List<CategoryResponse> categories = null;
        String id = null;
        String title = null;
        String description = null;
        String thumbnailUrl = null;
        VisibilityType visibility = null;
        QuizStatus status = null;
        TimeLimitRangeInSecond questionTimeLimit = null;
        LocalDateTime createdAt = null;
        QuizDifficultyType difficulty = null;
        LocalDateTime updatedAt = null;
        List<QuestionResponse> questions = null;

        categories = quizCategoryListToCategoryResponseList( quiz.getQuizCategories() );
        id = quiz.getId();
        title = quiz.getTitle();
        description = quiz.getDescription();
        thumbnailUrl = quiz.getThumbnailUrl();
        if ( quiz.getVisibility() != null ) {
            visibility = Enum.valueOf( VisibilityType.class, quiz.getVisibility() );
        }
        status = quiz.getStatus();
        questionTimeLimit = quiz.getQuestionTimeLimit();
        createdAt = quiz.getCreatedAt();
        difficulty = quiz.getDifficulty();
        updatedAt = quiz.getUpdatedAt();
        questions = toQuestionResponses( quiz.getQuestions() );

        QuizResponse quizResponse = new QuizResponse( id, title, description, thumbnailUrl, categories, visibility, status, questionTimeLimit, createdAt, difficulty, updatedAt, questions );

        return quizResponse;
    }

    @Override
    public Quiz toQuizRequest(CreateQuizRequest createQuizRequest) {
        if ( createQuizRequest == null ) {
            return null;
        }

        Quiz quiz = new Quiz();

        quiz.setTitle( createQuizRequest.title() );
        quiz.setDescription( createQuizRequest.description() );
        quiz.setVisibility( createQuizRequest.visibility() );
        quiz.setQuestionTimeLimit( createQuizRequest.questionTimeLimit() );
        quiz.setDifficulty( createQuizRequest.difficulty() );
        quiz.setStatus( createQuizRequest.status() );

        return quiz;
    }

    @Override
    public void toQuizUpdateResponse(QuizUpdate quizUpdate, Quiz quiz) {
        if ( quizUpdate == null ) {
            return;
        }

        if ( quizUpdate.title() != null ) {
            quiz.setTitle( quizUpdate.title() );
        }
        if ( quizUpdate.description() != null ) {
            quiz.setDescription( quizUpdate.description() );
        }
        if ( quizUpdate.thumbnailUrl() != null ) {
            quiz.setThumbnailUrl( quizUpdate.thumbnailUrl() );
        }
        if ( quizUpdate.visibility() != null ) {
            quiz.setVisibility( quizUpdate.visibility() );
        }
    }

    @Override
    public QuestionResponse toQuestionResponse(Question question) {
        if ( question == null ) {
            return null;
        }

        String id = null;
        String text = null;
        QuestionType type = null;
        Integer questionOrder = null;
        Integer timeLimit = null;
        Integer points = null;
        String imageUrl = null;
        List<OptionResponse> options = null;

        id = question.getId();
        text = question.getText();
        type = question.getType();
        questionOrder = question.getQuestionOrder();
        timeLimit = question.getTimeLimit();
        points = question.getPoints();
        imageUrl = question.getImageUrl();
        options = toOptionResponses( question.getOptions() );

        QuestionResponse questionResponse = new QuestionResponse( id, text, type, questionOrder, timeLimit, points, imageUrl, options );

        return questionResponse;
    }

    @Override
    public OptionResponse toOptionResponse(Option option) {
        if ( option == null ) {
            return null;
        }

        String id = null;
        String optionText = null;
        Integer optionOrder = null;
        Timestamp createdAt = null;
        Boolean isCorrected = null;

        id = option.getId();
        optionText = option.getOptionText();
        optionOrder = option.getOptionOrder();
        createdAt = option.getCreatedAt();
        isCorrected = option.getIsCorrected();

        OptionResponse optionResponse = new OptionResponse( id, optionText, optionOrder, createdAt, isCorrected );

        return optionResponse;
    }

    @Override
    public List<QuestionResponse> toQuestionResponses(List<Question> questions) {
        if ( questions == null ) {
            return null;
        }

        List<QuestionResponse> list = new ArrayList<QuestionResponse>( questions.size() );
        for ( Question question : questions ) {
            list.add( toQuestionResponse( question ) );
        }

        return list;
    }

    @Override
    public List<OptionResponse> toOptionResponses(List<Option> options) {
        if ( options == null ) {
            return null;
        }

        List<OptionResponse> list = new ArrayList<OptionResponse>( options.size() );
        for ( Option option : options ) {
            list.add( toOptionResponse( option ) );
        }

        return list;
    }

    protected List<CategoryResponse> quizCategoryListToCategoryResponseList(List<QuizCategory> list) {
        if ( list == null ) {
            return null;
        }

        List<CategoryResponse> list1 = new ArrayList<CategoryResponse>( list.size() );
        for ( QuizCategory quizCategory : list ) {
            list1.add( map( quizCategory ) );
        }

        return list1;
    }
}
