package kh.edu.cstad.stackquizapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kh.edu.cstad.stackquizapi.dto.request.QuizReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.QuizReportResponse;
import kh.edu.cstad.stackquizapi.service.QuizReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz-reports")
public class QuizReportController {

    private final QuizReportService quizReportService;

    @Operation(summary = "Submit a report for a quiz",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{quizId}")
    public QuizReportResponse submitReport(@PathVariable String quizId,
                                           @Valid @RequestBody QuizReportRequest reportRequest,
                                           @AuthenticationPrincipal Jwt accessToken) {
        return quizReportService.submitReport(quizId, reportRequest, accessToken);
    }

    @Operation(summary = "Get all reports submitted by the authenticated user",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public List<QuizReportResponse> getCurrentUserReports(@AuthenticationPrincipal Jwt accessToken) {
        return quizReportService.getCurrentUserReports(accessToken);
    }

    @Operation(summary = "Get all reports for a specific quiz",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/quizzes/{quizId}")
    public List<QuizReportResponse> getReportsByQuiz(@PathVariable String quizId) {
        return quizReportService.getReportsByQuiz(quizId);
    }

}

