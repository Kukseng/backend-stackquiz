package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface QuizSessionService {

    SessionResponse createSession(SessionCreateRequest request, Jwt accessToken);

    void startSession(String sessionId);

    SessionResponse endSession(String sessionId);

    void pauseSession(String sessionId);

    void submitAnswer(String sessionCode, String participantId, String selectedOptionId);

    Question advanceToNextQuestion(String sessionId);

    Question getCurrentQuestion(String sessionId);

    SessionResponse joinSession(String sessionCode, String nickname, String userId, String avatarId);

    boolean canJoinSession(String sessionCode);

    List<QuizSession> getActiveSession();

    List<QuizSession> getSessions(String hostId);

    Optional<QuizSession> getSessionByCode(String sessionCode);

    List<QuizSession> getCurrentUserQuizSession(Jwt accessToken);

    void sendNextQuestionToParticipant(String participantId, String sessionId, int questionNumber);

    SessionResponse setAllowJoinInProgress(String sessionId, boolean allow);

}
