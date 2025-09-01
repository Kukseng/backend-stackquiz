package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.WebSocketMessage;

/**
 * Service for managing WebSocket communication in quiz sessions.
 *
 * @author Pech Rattanakmony
 */
public interface WebSocketService {

    /** Broadcast a message to all participants in a session. */
    void broadcastToSession(String sessionId, WebSocketMessage message);

    /** Send a message to a specific participant. */
    void sendToParticipant(String participantId, String sessionId, WebSocketMessage message);

    /** Send a message to the session host. */
    void sendToHost(String hostId, String sessionId, WebSocketMessage message);

    /** Broadcast a question to all participants. */
    void broadcastQuestion(String sessionId, QuestionMessage questionMessage);

    /** Broadcast leaderboard updates to all participants. */
    void broadcastLeaderboard(String sessionId, LeaderboardMessage leaderboardMessage);

    /** Broadcast participant status updates to all participants. */
    void broadcastParticipantUpdate(String sessionId, ParticipantMessage participantMessage);

    /** Broadcast game state changes to all participants. */
    void broadcastGameState(String sessionId, GameStateMessage gameStateMessage);

    /** Handle participant connection event. */
    void handleParticipantConnect(String sessionId, String participantId);

    /** Handle participant disconnection event. */
    void handleParticipantDisconnect(String sessionId, String participantId);

    /** Get active connection count for a session. */
    int getActiveConnectionCount(String sessionId);
}
