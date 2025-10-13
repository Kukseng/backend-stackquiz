package kh.edu.cstad.stackquizapi.mapper;

import javax.annotation.processing.Generated;
import kh.edu.cstad.stackquizapi.domain.QuizReport;
import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-12T22:04:26+0700",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class QuizReportMapperImpl implements QuizReportMapper {

    @Override
    public QuizReport fromQuizReportRequest(QuizReportRequest quizReportRequest) {
        if ( quizReportRequest == null ) {
            return null;
        }

        QuizReport quizReport = new QuizReport();

        quizReport.setReason( quizReportRequest.reason() );
        quizReport.setDescription( quizReportRequest.description() );

        return quizReport;
    }
}
