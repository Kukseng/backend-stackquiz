package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.HostCommandMessage;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing quiz sessions and their associated question flow.
 * <p>
 * Handles session lifecycle management, question progression, participant interactions,
 * and optional features such as late joiners.
 * </p>
 *
 * <p>Main features include:
 * <ul>
 *   <li>Create, start, pause, and end quiz sessions</li>
 *   <li>Advance questions and retrieve current question</li>
 *   <li>Manage participant joining, late joining, and answer submissions</li>
 *   <li>Retrieve active and user-specific sessions</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface QuizSessionService {

    // ==============================
    // Session management
    // ==============================

    /**
     * Create a new quiz session for a host.
     *
     * @param request the session creation details
     * @param accessToken the host's JWT
     * @return the created session response
     */
    SessionResponse createSession(SessionCreateRequest request, Jwt accessToken);

    /**
     * Start a session by its ID.
     *
     * @param sessionId the unique session identifier
     * @return the updated session response
     */
    SessionResponse startSession(String sessionId);

    /**
     * End a session by its ID.
     *
     * @param sessionId the unique session identifier
     * @return the updated session response
     */
    SessionResponse endSession(String sessionId);

    /**
     * Pause a session temporarily.
     *
     * @param sessionId the unique session identifier
     */
    void pauseSession(String sessionId);

    /**
     * Submit a participant's answer for the current question.
     *
     * @param sessionCode the session code
     * @param participantId the participant's ID
     * @param selectedOptionId the chosen option ID
     */
    void submitAnswer(String sessionCode, String participantId, String selectedOptionId);

    /**
     * Start a session with custom settings.
     *
     * @param sessionCode the session code
     * @param settings the session settings
     * @return the updated session response
     */
    SessionResponse startSessionWithSettings(String sessionCode, HostCommandMessage.SessionSettings settings);

    /**
     * Advance the session to the next question.
     *
     * @param sessionId the unique session identifier
     * @return the next question object
     */
    Question advanceToNextQuestion(String sessionId);

    /**
     * Retrieve the current active question in the session.
     *
     * @param sessionId the unique session identifier
     * @return the current question object
     */
    Question getCurrentQuestion(String sessionId);

    /**
     * Allow a participant to join a session with nickname, user ID, and optional avatar.
     *
     * @param sessionCode the session code
     * @param nickname the participant's nickname
     * @param userId the participant's user ID
     * @param avatarId optional avatar ID
     * @return the participant session response
     */
    SessionResponse joinSession(String sessionCode, String nickname, String userId, String avatarId);

    /**
     * Check if a participant can join a session.
     *
     * @param sessionCode the session code
     * @return {@code true} if joining is allowed, otherwise {@code false}
     */
    boolean canJoinSession(String sessionCode);

    /**
     * Send the next question to a specific participant.
     *
     * @param participantId the participant's ID
     * @param sessionId the session ID
     * @param questionNumber the question number to send
     */
    void sendNextQuestionToParticipant(String participantId, String sessionId, int questionNumber);

    /**
     * Enable or disable late joining for a session in progress.
     *
     * @param sessionId the session ID
     * @param allow {@code true} to allow joining in progress
     * @return the updated session response
     */
    SessionResponse setAllowJoinInProgress(String sessionId, boolean allow);

    /**
     * Convert a QuizSession entity to a SessionResponse DTO.
     *
     * @param quizSession the quiz session entity
     * @return the session response object
     */
    SessionResponse toSessionResponse(QuizSession quizSession);

    /**
     * Retrieve all active sessions.
     *
     * @return list of active quiz sessions
     */
    List<QuizSession> getActiveSession();

    /**
     * Retrieve all sessions created by a specific host.
     *
     * @param hostId the host user ID
     * @return list of quiz sessions for the host
     */
    List<QuizSession> getSessions(String hostId);

    /**
     * Retrieve a session by its unique code.
     *
     * @param sessionCode the session code
     * @return an optional containing the session if found
     */
    Optional<QuizSession> getSessionByCode(String sessionCode);

    /**
     * Retrieve all sessions associated with the current authenticated user.
     *
     * @param accessToken the user's JWT
     * @return list of user-specific quiz sessions
     */
    List<QuizSession> getCurrentUserQuizSession(Jwt accessToken);
}

