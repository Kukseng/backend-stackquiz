package kh.edu.cstad.stackquizapi.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
//import kh.edu.cstad.stackquizapi.service.QuizSessionServiceExtended;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
public class QuizSessionController {

    private final QuizSessionService quizSessionService;
//    private final QuizSessionServiceExtended quizSessionServiceExtended;

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SessionResponse createSession(@RequestBody SessionCreateRequest request,
                                         @AuthenticationPrincipal Jwt accessToken) {
        return quizSessionService.createSession(request, accessToken);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionCode}/start")
    public void startSession(@PathVariable String sessionCode) {
        quizSessionService.startSession(sessionCode);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionCode}/end")
    SessionResponse endSession(@PathVariable String sessionCode) {
        return quizSessionService.endSession(sessionCode);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionCode}/next-question")
    public Question toNextQuestion(@PathVariable String sessionCode) {
        return quizSessionService.advanceToNextQuestion(sessionCode);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{sessionCode}/join")
    Boolean canJoinSession(@PathVariable String sessionCode) {
        return quizSessionService.canJoinSession(sessionCode);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{quizCode}")
    public Optional<QuizSession> getQuizSessions(@PathVariable String quizCode){
        return quizSessionService.getSessionByCode(quizCode);
    }

    @Operation(summary = "Get all users (secured)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/me")
    public List<QuizSession> getCurrentUserQuizSessions(@AuthenticationPrincipal Jwt accessToken) {
        return quizSessionService.getCurrentUserQuizSession(accessToken);
    }
}