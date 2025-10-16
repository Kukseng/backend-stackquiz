package kh.edu.cstad.stackquizapi.mapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateQuestionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.util.QuestionType;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-16T16:34:38+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class QuestionMapperImpl implements QuestionMapper {

    @Override
    public Question fromCreateQuestionRequest(CreateQuestionRequest request) {
        if ( request == null ) {
            return null;
        }

        Question question = new Question();

        question.setText( request.text() );
        question.setType( request.type() );
        question.setImageUrl( request.imageUrl() );

        return question;
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
        options = optionListToOptionResponseList( question.getOptions() );

        QuestionResponse questionResponse = new QuestionResponse( id, text, type, questionOrder, timeLimit, points, imageUrl, options );

        return questionResponse;
    }

    @Override
    public void toQuestionPartially(UpdateQuestionRequest updateCustomerRequest, Question question) {
        if ( updateCustomerRequest == null ) {
            return;
        }

        if ( updateCustomerRequest.text() != null ) {
            question.setText( updateCustomerRequest.text() );
        }
        if ( updateCustomerRequest.type() != null ) {
            question.setType( updateCustomerRequest.type() );
        }
        if ( updateCustomerRequest.questionOrder() != null ) {
            question.setQuestionOrder( updateCustomerRequest.questionOrder() );
        }
        if ( updateCustomerRequest.timeLimit() != null ) {
            question.setTimeLimit( updateCustomerRequest.timeLimit() );
        }
        if ( updateCustomerRequest.points() != null ) {
            question.setPoints( updateCustomerRequest.points() );
        }
        if ( updateCustomerRequest.imageUrl() != null ) {
            question.setImageUrl( updateCustomerRequest.imageUrl() );
        }

        question.setCreatedAt( new java.sql.Timestamp(System.currentTimeMillis()) );
    }

    protected OptionResponse optionToOptionResponse(Option option) {
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

    protected List<OptionResponse> optionListToOptionResponseList(List<Option> list) {
        if ( list == null ) {
            return null;
        }

        List<OptionResponse> list1 = new ArrayList<OptionResponse>( list.size() );
        for ( Option option : list ) {
            list1.add( optionToOptionResponse( option ) );
        }

        return list1;
    }
}
