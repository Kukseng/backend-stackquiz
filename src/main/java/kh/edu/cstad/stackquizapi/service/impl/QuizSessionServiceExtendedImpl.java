package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizSessionMapper;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.QuizSessionServiceExtended;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Primary
public class QuizSessionServiceExtendedImpl implements QuizSessionServiceExtended {

    private final QuizSessionServiceImpl baseService;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizSessionMapper quizSessionMapper;

    @Override
    public SessionResponse createSession(SessionCreateRequest request, Jwt accessToken) {
        return baseService.createSession(request, accessToken);
    }

    @Override
    public SessionResponse startSession(String sessionId) {
        return baseService.startSession(sessionId);
    }

    @Override
    public Question advanceToNextQuestion(String sessionId) {
        return baseService.advanceToNextQuestion(sessionId);
    }

    @Override
    public SessionResponse endSession(String sessionId) {
        return baseService.endSession(sessionId);
    }

    @Override
    public Question getCurrentQuestion(String sessionId) {
        return baseService.getCurrentQuestion(sessionId);
    }

    @Override
    public boolean canJoinSession(String sessionCode) {
        return baseService.canJoinSession(sessionCode);
    }

    @Override
    public List<QuizSession> getActiveSession() {
        return baseService.getActiveSession();
    }

    @Override
    public List<QuizSession> getSessions(String hostId) {
        return baseService.getSessions(hostId);
    }

    @Override
    public Optional<QuizSession> getSessionByCode(String sessionCode) {
        return baseService.getSessionByCode(sessionCode);
    }

    @Override
    public SessionResponse getSessionById(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        return quizSessionMapper.toSessionResponse(session);
    }

    @Override
    public SessionResponse pauseSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setStatus(Status.WAITING);
        session = quizSessionRepository.save(session);

        return quizSessionMapper.toSessionResponse(session);
    }

    @Override
    public SessionResponse resumeSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setStatus(Status.IN_PROGRESS);
        session = quizSessionRepository.save(session);

        return quizSessionMapper.toSessionResponse(session);
    }

    @Override
    public SessionResponse updateCurrentQuestion(String sessionId, Integer questionIndex) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setCurrentQuestion(questionIndex);
        session = quizSessionRepository.save(session);

        return quizSessionMapper.toSessionResponse(session);
    }

    @Override
    public boolean isSessionActive(String sessionId) {
        Optional<QuizSession> session = quizSessionRepository.findById(sessionId);
        return session.isPresent() && session.get().getStatus() == Status.IN_PROGRESS;
    }

    @Override
    public Integer getTotalQuestions(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        return session.getTotalQuestions();
    }
}


