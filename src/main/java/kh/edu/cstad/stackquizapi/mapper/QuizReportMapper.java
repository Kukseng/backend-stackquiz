package kh.edu.cstad.stackquizapi.mapper;

import kh.edu.cstad.stackquizapi.domain.QuizReport;
import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizReportMapper {

    QuizReport fromQuizReportRequest(QuizReportRequest quizReportRequest);

}
