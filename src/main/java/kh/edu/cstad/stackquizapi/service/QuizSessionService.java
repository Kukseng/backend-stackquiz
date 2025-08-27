package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;

import java.util.List;
import java.util.Optional;

public interface QuizSessionService {

    SessionResponse createSession(SessionCreateRequest request);

    SessionResponse startSession(String sessionId);

    Question advanceToNextQuestion(String sessionId);

    SessionResponse endSession(String sessionId);

    Question getCurrentQuestion(String sessionId);

    boolean canJoinSession(String sessionCode);

    List<QuizSession> getActiveSession();

    List<QuizSession> getSessions(String hostId);

    Optional<QuizSession> getSessionByCode(String sessionCode);

}