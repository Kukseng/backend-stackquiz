package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.domain.Participant;

import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing participant-related operations within a quiz session.
 * <p>
 * Provides methods for joining and leaving sessions, submitting answers,
 * retrieving participant information, and validating session access rules.
 * </p>
 *
 * <p>Main features include:
 * <ul>
 *   <li>Joining a session (guest or authenticated user)</li>
 *   <li>Submitting answers to active sessions</li>
 *   <li>Retrieving and managing participant data</li>
 *   <li>Nickname and session validation checks</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface ParticipantService {

    /**
     * Allows a participant to join a session using a session code.
     *
     * @param request the request containing session code and participant details
     * @return the response containing participant information after joining
     */
    ParticipantResponse joinSession(JoinSessionRequest request);

    /**
     * Allows an authenticated user to join a session using a session code.
     * <p>
     * This method uses the provided JWT to associate the participant with a user account.
     * </p>
     *
     * @param accessToken the JWT representing the authenticated user
     * @param request     the request containing session code and participant details
     * @return the response containing participant information after joining
     */
    ParticipantResponse joinSessionAsAuthenticatedUser(Jwt accessToken, JoinSessionRequest request);

    /**
     * Submits an answer for a participant within a session.
     *
     * @param request the request containing participant ID, question ID, and answer details
     * @return the response containing answer submission results
     */
    SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request);

    /**
     * Retrieves all participants in a specific session.
     *
     * @param sessionId the unique identifier of the session
     * @return a list of responses representing participants in the session
     */
    List<ParticipantResponse> getSessionParticipants(String sessionId);

    /**
     * Removes a participant from the session.
     *
     * @param participantId the unique identifier of the participant to remove
     */
    void leaveSession(String participantId);

    /**
     * Retrieves a participant by their unique identifier.
     *
     * @param participantId the unique identifier of the participant
     * @return an optional containing the participant if found, otherwise empty
     */
    Optional<Participant> getParticipantById(String participantId);

    /**
     * Checks if a participant can join a session based on the session code.
     * <p>
     * This may validate session state (e.g., active, locked, or full).
     * </p>
     *
     * @param sessionCode the unique code representing the session
     * @return {@code true} if the participant can join, otherwise {@code false}
     */
    boolean canJoinSession(String sessionCode);

    /**
     * Checks whether a nickname is available for use in a specific session.
     *
     * @param sessionId the unique identifier of the session
     * @param nickname  the nickname to validate
     * @return {@code true} if the nickname is available, otherwise {@code false}
     */
    boolean isNicknameAvailable(String sessionId, String nickname);
}
