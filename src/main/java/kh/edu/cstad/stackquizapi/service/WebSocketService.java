package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.*;

/**
 * Service for managing WebSocket communication in quiz sessions.
 *
 * @author Pech Rattanakmony
 */
public interface WebSocketService {
    // Core broadcasting methods

    void broadcastGameState(String sessionCode, GameStateMessage msg);
    void broadcastQuestion(String sessionCode, QuestionMessage msg);
    void broadcastLeaderboard(String sessionCode, LeaderboardMessage msg);
    void broadcastParticipantUpdate(String sessionCode, ParticipantMessage msg);
    void broadcastAnswerResult(String sessionCode, AnswerSubmissionMessage msg);
    void handleParticipantDisconnect(String sessionCode, String nickname);
    void sendToParticipant(String nickname, String sessionCode, WebSocketMessage msg);
    void sendToHost(String hostNickname, String sessionCode, WebSocketMessage msg);
    void sendErrorToParticipant(String nickname, String sessionCode, String errorMessage);
    // Direct messaging


    //    // Connection management
    void handleParticipantConnect(String sessionCode, String nickname);
//    void handleParticipantDisconnect(String sessionCode, String nickname);
//
//    // Session lifecycle events
//    void startSessionWithCountdown(String sessionCode, String hostNickname, int totalQuestions);
//    void broadcastSessionLobby(String sessionCode, String hostNickname);
//    void broadcastCountdown(String sessionCode, String hostNickname, int seconds);
//    void endSession(String sessionCode, String hostNickname);
//
//    // Utility methods
//    int getActiveConnectionCount(String sessionCode);
//    void cleanupSession(String sessionCode);
}
