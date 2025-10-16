package kh.edu.cstad.stackquizapi.mapper;

import java.sql.Timestamp;
import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.Option;
import kh.edu.cstad.stackquizapi.dto.request.AddOptionRequest;
import kh.edu.cstad.stackquizapi.dto.request.UpdateOptionRequest;
import kh.edu.cstad.stackquizapi.dto.response.OptionResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-16T16:34:38+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class OptionMapperImpl implements OptionMapper {

    @Override
    public Option fromAddOptionRequest(AddOptionRequest addOptionRequest) {
        if ( addOptionRequest == null ) {
            return null;
        }

        Option option = new Option();

        option.setOptionText( addOptionRequest.optionText() );
        option.setIsCorrected( addOptionRequest.isCorrected() );

        return option;
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
    public void toQuestionPartially(UpdateOptionRequest updateOptionRequest, Option option) {
        if ( updateOptionRequest == null ) {
            return;
        }

        if ( updateOptionRequest.optionText() != null ) {
            option.setOptionText( updateOptionRequest.optionText() );
        }
        if ( updateOptionRequest.optionOrder() != null ) {
            option.setOptionOrder( updateOptionRequest.optionOrder() );
        }
        if ( updateOptionRequest.isCorrected() != null ) {
            option.setIsCorrected( updateOptionRequest.isCorrected() );
        }
    }
}
