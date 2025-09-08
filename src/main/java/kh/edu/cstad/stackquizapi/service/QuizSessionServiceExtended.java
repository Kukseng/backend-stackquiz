package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Extended quiz session service providing additional functionality
 * for managing and interacting with quiz sessions.
 *
 * This interface builds on {@link QuizSessionService} with methods
 * for controlling session state, retrieving details, and updating
 * participant progress.
 *
 * @author Pech Rattanakmony
 */
public interface QuizSessionServiceExtended extends QuizSessionService {

    /**
     * Retrieves the quiz session details by its unique ID.
     *
     * @param sessionId the unique identifier of the session
     * @return a {@link SessionResponse} containing session details
     */
    SessionResponse getSessionById(String sessionId);

    /**
     * Pauses an active quiz session, preventing further progress
     * until it is resumed.
     *
     * @param sessionId the unique identifier of the session
     * @return a {@link SessionResponse} reflecting the paused session state
     */
    SessionResponse pauseSession(String sessionId);

    /**
     * Resumes a previously paused quiz session, allowing participants
     * to continue where they left off.
     *
     * @param sessionId the unique identifier of the session
     * @return a {@link SessionResponse} reflecting the resumed session state
     */
    SessionResponse resumeSession(String sessionId);

    /**
     * Updates the current question index for the session.
     * Useful for tracking participant progress or skipping questions.
     *
     * @param sessionId the unique identifier of the session
     * @param questionIndex the index of the current question (0-based or 1-based depending on implementation)
     * @return a {@link SessionResponse} with the updated question state
     */
    SessionResponse updateCurrentQuestion(String sessionId, Integer questionIndex);

    /**
     * Checks whether the session is currently active and accepting participants.
     *
     * @param sessionId the unique identifier of the session
     * @return {@code true} if the session is active, {@code false} otherwise
     */
    boolean isSessionActive(String sessionId);

    List<QuizSession> getCurrentUserQuizSession(Jwt accessToken);
    /**
     * Retrieves the total number of questions associated with the session.
     *
     * @param sessionId the unique identifier of the session
     * @return the total count of questions in the session
     */
    Integer getTotalQuestions(String sessionId);
}
