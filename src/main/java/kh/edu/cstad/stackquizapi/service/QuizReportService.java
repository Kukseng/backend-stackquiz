package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuizReportResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface QuizReportService {

    QuizReportResponse submitReport(String quizId, QuizReportRequest request, Jwt accessToken);

    List<QuizReportResponse> getCurrentUserReports(Jwt accessToken);

    List<QuizReportResponse> getReportsByQuiz(String quizId);
}


