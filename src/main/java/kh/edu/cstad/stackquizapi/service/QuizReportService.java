package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuizReportResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface QuizReportService {

    // Submit a report for a quiz
    QuizReportResponse submitReport(String quizId, QuizReportRequest request, Jwt accessToken);

    // Get reports submitted by the current user
    List<QuizReportResponse> getCurrentUserReports(Jwt accessToken);

    // Get all reports for a specific quiz (admin)
    List<QuizReportResponse> getReportsByQuiz(String quizId);
}


