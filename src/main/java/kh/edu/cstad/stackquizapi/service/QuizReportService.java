package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuizReportResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Service for managing quiz reports submitted by participants.
 * Handles creating and retrieving reports for specific quizzes or users.
 *
 * @author Pech Rattanakmony
 * @since 1.0
 */
public interface QuizReportService {

    /**
     * Submit a report for a specific quiz.
     *
     * @param quizId      the ID of the quiz being reported
     * @param request     the report details
     * @param accessToken the user's JWT
     * @return the submitted report response
     */
    QuizReportResponse submitReport(String quizId, QuizReportRequest request, Jwt accessToken);

    /**
     * Get all reports submitted by the current user.
     *
     * @param accessToken the user's JWT
     * @return list of report responses
     */
    List<QuizReportResponse> getCurrentUserReports(Jwt accessToken);

    /**
     * Get all reports for a specific quiz.
     *
     * @param quizId the ID of the quiz
     * @return list of report responses
     */
    List<QuizReportResponse> getReportsByQuiz(String quizId);
}



