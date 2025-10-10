package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.websocket.*;

public interface WebSocketService {

    // Broadcast methods (to all participants in session)
    void broadcastGameState(String sessionCode, GameStateMessage message);
    void broadcastQuestion(String sessionCode, QuestionMessage message);
    void broadcastLeaderboard(String sessionCode, LeaderboardMessage message);
    void broadcastParticipantUpdate(String sessionCode, ParticipantMessage message);
    void broadcastAnswerResult(String sessionCode, AnswerSubmissionMessage message);

    // Individual messaging methods
    void sendToParticipantByNickname(String nickname, String sessionCode, WebSocketMessage message);
    void sendToHost(String hostNickname, String sessionCode, WebSocketMessage message);
    void sendErrorToParticipant(String nickname, String sessionCode, String errorMessage);

    // Connection handling
    void handleParticipantConnect(String sessionCode, String nickname);
    void handleParticipantDisconnect(String sessionCode, String nickname);

    // *** ENHANCED METHODS FOR KAHOOT-STYLE INDIVIDUAL PROGRESSION ***

    // Send messages to specific participant by ID (for individual progression)
    default void sendToParticipant(String sessionCode, String participantId, Object message) {
        // Default implementation - override in implementation class
    }

    // Send question to specific participant
    default void sendQuestionToParticipant(String sessionCode, String participantId, QuestionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Send answer feedback to specific participant
    default void sendFeedbackToParticipant(String sessionCode, String participantId, AnswerSubmissionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Send completion message to specific participant
    default void sendCompletionToParticipant(String sessionCode, String participantId, GameStateMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Notify host of participant progress
    default void notifyHostParticipantProgress(String sessionCode, ParticipantProgressMessage message) {
        // Default implementation - override in implementation class
    }

    // *** NEW REAL-TIME METHODS FOR KAHOOT-STYLE FUNCTIONALITY ***

    // Send score update to specific participant
    default void sendScoreUpdateToParticipant(String sessionCode, String participantId, ScoreUpdateMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Send answer feedback to specific participant
    default void sendAnswerFeedbackToParticipant(String sessionCode, String participantId, AnswerFeedbackMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Send ranking update to specific participant
    default void sendRankingUpdateToParticipant(String sessionCode, String participantId, ParticipantRankingMessage message) {
        sendToParticipant(sessionCode, participantId, message);
    }

    // Broadcast host progress updates
    default void broadcastHostProgress(String sessionCode, HostProgressMessage message) {
        // Default implementation - override in implementation class
    }

    // Broadcast live session statistics
    default void broadcastLiveStats(String sessionCode, LiveStatsMessage message) {
        // Default implementation - override in implementation class
    }

    // Send real-time updates to host
    default void sendToHost(String sessionCode, Object message) {
        // Default implementation - override in implementation class
    }
}
