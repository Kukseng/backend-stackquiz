package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.HostCommandMessage;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface QuizSessionService {

    // ✅ Session management
    SessionResponse createSession(SessionCreateRequest request, Jwt accessToken);
    SessionResponse startSession(String sessionId);
    SessionResponse endSession(String sessionId);
    void pauseSession(String sessionId);
    void submitAnswer(String sessionCode, String participantId, String selectedOptionId);
    SessionResponse startSessionWithSettings(String sessionCode, HostCommandMessage.SessionSettings settings);
    // ✅ Question flow
    Question advanceToNextQuestion(String sessionId);
    Question getCurrentQuestion(String sessionId);

    // ✅ Participant management
//    SessionResponse joinSession(String sessionCode, String nickname, String userId);
//    void submitAnswer(String sessionId, String participantId, String selectedOptionId);
    SessionResponse joinSession(String sessionCode, String nickname, String userId, String avatarId);
    // ✅ Session queries
    boolean canJoinSession(String sessionCode);
    List<QuizSession> getActiveSession();
    List<QuizSession> getSessions(String hostId);
    Optional<QuizSession> getSessionByCode(String sessionCode);
    List<QuizSession> getCurrentUserQuizSession(Jwt accessToken);
    void sendNextQuestionToParticipant(String participantId, String sessionId, int questionNumber);
    // ✅ Optional: allow late joiners

    SessionResponse setAllowJoinInProgress(String sessionId, boolean allow);
    SessionResponse toSessionResponse(QuizSession quizSession);
}
