package kh.edu.cstad.stackquizapi.controller;


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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SessionResponse createSession(@RequestBody SessionCreateRequest request) {
        return quizSessionService.createSession(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/start")
    public SessionResponse startSession(@PathVariable String sessionId) {
        return quizSessionService.startSession(sessionId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/end")
    SessionResponse endSession(@PathVariable String sessionId) {
        return quizSessionService.endSession(sessionId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{sessionId}/next-question")
    public Question toNextQuestion(@PathVariable String sessionId) {
        return quizSessionService.advanceToNextQuestion(sessionId);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{sessionCode}/join")
    Boolean canJoinSession(@PathVariable String sessionCode) {
        return quizSessionService.canJoinSession(sessionCode);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{hostId}")
    public Optional<QuizSession> getQuizSessions(@PathVariable String hostId){
        return quizSessionService.getSessionByCode(hostId);
    }



}