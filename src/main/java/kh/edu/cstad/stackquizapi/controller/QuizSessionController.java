package kh.edu.cstad.stackquizapi.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
public class QuizSessionController {

    private final QuizSessionService quizSessionService;

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SessionResponse createSession(@RequestBody SessionCreateRequest request) {
        return quizSessionService.createSession(request);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/start")
    public SessionResponse startSession(@PathVariable String sessionId) {
        return quizSessionService.startSession(sessionId);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/end")
    SessionResponse endSession(@PathVariable String sessionId) {
        return quizSessionService.endSession(sessionId);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/next-question")
    public Question toNextQuestion(@PathVariable String sessionId) {
        return quizSessionService.advanceToNextQuestion(sessionId);
    }


    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{sessionCode}/join")
    Boolean canJoinSession(@PathVariable String sessionCode) {
        return quizSessionService.canJoinSession(sessionCode);
    }

    // get current user for profile
    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizCode}")
    public Optional<QuizSession> getQuizSessions(@PathVariable String quizCode){
        return quizSessionService.getSessionByCode(quizCode);
    }

}