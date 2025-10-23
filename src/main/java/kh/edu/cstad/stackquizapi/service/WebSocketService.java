package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.*;

/**
 * Service interface for managing WebSocket communications in quiz sessions.
 * <p>
 * Supports real-time interaction including broadcasting messages to all participants,
 * sending targeted messages to individual participants or hosts, handling participant
 * connections, and providing Kahoot-style score, ranking, and progress updates.
 * </p>
 *
 * @author Phou Kukseng, Pech Rattanakmony
 * @since 1.0
 */
public interface WebSocketService {

    /**
     * Broadcast the current game state to all participants in a session.
     *
     * @param sessionCode the code of the session
     * @param message the game state message to broadcast
     */
    void broadcastGameState(String sessionCode, GameStateMessage message);

    /**
     * Broadcast a question to all participants in a session.
     *
     * @param sessionCode the code of the session
     * @param message the question message to broadcast
     */
    void broadcastQuestion(String sessionCode, QuestionMessage message);

    /**
     * Broadcast the current leaderboard to all participants.
     *
     * @param sessionCode the code of the session
     * @param message the leaderboard message to broadcast
     */
    void broadcastLeaderboard(String sessionCode, LeaderboardMessage message);

    /**
     * Broadcast updates regarding participants (join, leave, progress) to all participants.
     *
     * @param sessionCode the code of the session
     * @param message the participant update message
     */
    void broadcastParticipantUpdate(String sessionCode, ParticipantMessage message);

    /**
     * Broadcast the results of participant answers to all participants.
     *
     * @param sessionCode the code of the session
     * @param message the answer submission message
     */
    void broadcastAnswerResult(String sessionCode, AnswerSubmissionMessage message);

    /**
     * Send a WebSocket message to a specific participant identified by nickname.
     *
     * @param nickname the participant's nickname
     * @param sessionCode the code of the session
     * @param message the WebSocket message
     */
    void sendToParticipantByNickname(String nickname, String sessionCode, WebSocketMessage message);

    /**
     * Send a WebSocket message to the host of a session.
     *
     * @param hostNickname the host's nickname
     * @param sessionCode the code of the session
     * @param message the WebSocket message
     */
    void sendToHost(String hostNickname, String sessionCode, WebSocketMessage message);

    /**
     * Send an error message to a specific participant.
     *
     * @param nickname the participant's nickname
     * @param sessionCode the code of the session
     * @param errorMessage the error message content
     */
    void sendErrorToParticipant(String nickname, String sessionCode, String errorMessage);

    /**
     * Handle participant connection to a session.
     *
     * @param sessionCode the code of the session
     * @param nickname the participant's nickname
     */
    void handleParticipantConnect(String sessionCode, String nickname);

    /**
     * Handle participant disconnection from a session.
     *
     * @param sessionCode the code of the session
     * @param nickname the participant's nickname
     */
    void handleParticipantDisconnect(String sessionCode, String nickname);

    /**
     * Send a message to a specific participant by ID.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the message object to send
     */
    default void sendToParticipant(String sessionCode, String participantId, Object message) {}

    /**
     * Send a question to a specific participant.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the question message
     */
    default void sendQuestionToParticipant(String sessionCode, String participantId, QuestionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Send answer feedback to a specific participant.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the answer submission message
     */
    default void sendFeedbackToParticipant(String sessionCode, String participantId, AnswerSubmissionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Send a session completion message to a specific participant.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the game state message
     */
    default void sendCompletionToParticipant(String sessionCode, String participantId, GameStateMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Notify the host of a participant's progress.
     *
     * @param sessionCode the code of the session
     * @param message the participant progress message
     */
    default void notifyHostParticipantProgress(String sessionCode, ParticipantProgressMessage message) {}

    /**
     * Send score updates to a specific participant in real time.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the score update message
     */
    default void sendScoreUpdateToParticipant(String sessionCode, String participantId, ScoreUpdateMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Send answer feedback messages to a specific participant.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the answer feedback message
     */
    default void sendAnswerFeedbackToParticipant(String sessionCode, String participantId, AnswerFeedbackMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Send ranking updates to a specific participant.
     *
     * @param sessionCode the code of the session
     * @param participantId the participant's unique ID
     * @param message the participant ranking message
     */
    default void sendRankingUpdateToParticipant(String sessionCode, String participantId, ParticipantRankingMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    /**
     * Broadcast host progress updates to all hosts or observers.
     *
     * @param sessionCode the code of the session
     * @param message the host progress message
     */
    default void broadcastHostProgress(String sessionCode, HostProgressMessage message) {}

    /**
     * Broadcast live session statistics to all participants.
     *
     * @param sessionCode the code of the session
     * @param message the live statistics message
     */
    default void broadcastLiveStats(String sessionCode, LiveStatsMessage message) {}

    /**
     * Send real-time updates to the host.
     *
     * @param sessionCode the code of the session
     * @param message the message object to send
     */
    default void sendToHost(String sessionCode, Object message) {}
}

