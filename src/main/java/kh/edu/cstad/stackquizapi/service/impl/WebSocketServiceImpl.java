package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.dto.websocket.*;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastGameState(String sessionCode, GameStateMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode + "/game-state", msg);
        log.debug("Broadcasted game state to session {}: {}", sessionCode, msg.getAction());
    }

    @Override
    public void broadcastQuestion(String sessionCode, QuestionMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode + "/questions", msg);
        log.info("Broadcasted question to session {}: Question {}/{}",
                sessionCode, msg.getQuestionNumber(), msg.getTotalQuestions());
    }

    @Override
    public void broadcastLeaderboard(String sessionCode, LeaderboardMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode + "/leaderboard", msg);
        log.debug("Broadcasted leaderboard to session {}: {}", sessionCode, msg.getUpdateType());
    }

    @Override
    public void broadcastParticipantUpdate(String sessionCode, ParticipantMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode + "/participants", msg);
        log.debug("Broadcasted participant update to session {}: {} participants, action: {}",
                sessionCode, msg.getTotalParticipants(), msg.getAction());
    }

    @Override
    public void broadcastAnswerResult(String sessionCode, AnswerSubmissionMessage msg) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionCode + "/answers", msg);
        log.debug("Broadcasted answer result to session {}", sessionCode);
    }

    @Override
    public void handleParticipantDisconnect(String sessionCode, String nickname) {
        log.info("Participant {} disconnected from session {}", nickname, sessionCode);
    }

    @Override
    public void sendToParticipantByNickname(String nickname, String sessionCode, WebSocketMessage msg) {
        messagingTemplate.convertAndSendToUser(nickname, "/topic/session/" + sessionCode + "/participant", msg);
        log.debug("Sent message to participant {} in session {}", nickname, sessionCode);
    }

    @Override
    public void sendToHost(String hostNickname, String sessionCode, WebSocketMessage msg) {
        messagingTemplate.convertAndSendToUser(hostNickname, "/topic/session/" + sessionCode + "/host", msg);
        log.debug("Sent message to host {} in session {}", hostNickname, sessionCode);
    }

    @Override
    public void sendErrorToParticipant(String nickname, String sessionCode, String errorMessage) {
        GameStateMessage errorMsg = new GameStateMessage(
                sessionCode, "SYSTEM", null, "ERROR", null, null, null, errorMessage
        );
        sendToParticipant(nickname, sessionCode, errorMsg);
    }

    @Override
    public void handleParticipantConnect(String sessionCode, String nickname) {
        log.info("Participant {} connected to session {}", nickname, sessionCode);
    }

    // =================== ENHANCED KAHOOT-STYLE METHODS ===================

    /**
     * Send message to specific participant by participant ID (for individual progression)
     *  Use convertAndSendToUser to send to participant's personal queue
     */
    @Override
    public void sendToParticipant(String sessionCode, String participantId, Object message) {
        // FIXED: Include session code in the destination path to match frontend subscription
        // Frontend subscribes to: /user/queue/session/{sessionCode}/question
        messagingTemplate.convertAndSendToUser(
                participantId,                                    // User identifier (participant ID)
                "/queue/session/" + sessionCode + "/question",   // Destination queue with session code
                message
        );
        log.debug("Sent message to participant {} in session {}: {}", participantId, sessionCode, message.getClass().getSimpleName());
    }

    /**
     * Send question to specific participant (individual progression)
     */
    @Override
    public void sendQuestionToParticipant(String sessionCode, String participantId, QuestionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
        log.info("Sent question {} to participant {} in session {}",
                message.getQuestionNumber(), participantId, sessionCode);
    }

    /**
     * Send answer feedback to specific participant
     */
    @Override
    public void sendFeedbackToParticipant(String sessionCode, String participantId, AnswerSubmissionMessage message) {
        sendToParticipant(sessionCode, participantId, message);
        log.debug("Sent answer feedback to participant {} in session {}", participantId, sessionCode);
    }

    /**
     * Send completion message to specific participant
     */
    @Override
    public void sendCompletionToParticipant(String sessionCode, String participantId, GameStateMessage message) {
        sendToParticipant(sessionCode, participantId, message);
        log.info("Sent completion message to participant {} in session {}", participantId, sessionCode);
    }

    /**
     * Notify host of participant progress updates
     */
    @Override
    public void notifyHostParticipantProgress(String sessionCode, ParticipantProgressMessage message) {
        String topic = "/topic/session/" + sessionCode + "/host";
        messagingTemplate.convertAndSend(topic, message);
        log.debug("Notified host of participant progress in session {}: {} on question {}",
                sessionCode, message.participantNickname(), message.currentQuestion());
    }

    // =================== NEW REAL-TIME METHODS ===================

    /**
     * Send score update to specific participant
     */
    @Override
    public void sendScoreUpdateToParticipant(String sessionCode, String participantId, ScoreUpdateMessage message) {
        messagingTemplate.convertAndSendToUser(
                participantId,
                "/queue/session/" + sessionCode + "/score",
                message
        );
        log.debug("Sent score update to participant {} in session {}: {} points",
                participantId, sessionCode, message.getPointsEarned());
    }

    /**
     * Send answer feedback to specific participant
     */
    @Override
    public void sendAnswerFeedbackToParticipant(String sessionCode, String participantId, AnswerFeedbackMessage message) {
        messagingTemplate.convertAndSendToUser(
                participantId,
                "/queue/session/" + sessionCode + "/feedback",
                message
        );
        log.debug("Sent answer feedback to participant {} in session {}: {} ({})",
                participantId, sessionCode, message.getIsCorrect() ? "CORRECT" : "INCORRECT", message.getPointsEarned());
    }

    /**
     * Send ranking update to specific participant
     */
    @Override
    public void sendRankingUpdateToParticipant(String sessionCode, String participantId, ParticipantRankingMessage message) {
        messagingTemplate.convertAndSendToUser(
                participantId,
                "/queue/session/" + sessionCode + "/ranking",
                message
        );
        log.debug("Sent ranking update to participant {} in session {}: rank {} ({})",
                participantId, sessionCode, message.getCurrentRank(), message.getRankChange());
    }

    /**
     * Broadcast host progress updates
     */
    @Override
    public void broadcastHostProgress(String sessionCode, HostProgressMessage message) {
        String topic = "/topic/session/" + sessionCode + "/host-progress";
        messagingTemplate.convertAndSend(topic, message);
        log.debug("Broadcasted host progress to session {}: {}/{} participants answered",
                sessionCode, message.getParticipantsAnswered(), message.getTotalParticipants());
    }

    /**
     * Broadcast live session statistics
     */
    @Override
    public void broadcastLiveStats(String sessionCode, LiveStatsMessage message) {
        String topic = "/topic/session/" + sessionCode + "/live-stats";
        messagingTemplate.convertAndSend(topic, message);
        log.debug("Broadcasted live stats to session {}: {} active participants",
                sessionCode, message.getActiveParticipants());
    }

    /**
     * Send message directly to host
     */
    @Override
    public void sendToHost(String sessionCode, Object message) {
        String topic = "/topic/session/" + sessionCode + "/host";
        messagingTemplate.convertAndSend(topic, message);
        log.debug("Sent message to host in session {}: {}", sessionCode, message.getClass().getSimpleName());
    }

    /**
     * Broadcast session statistics to all participants
     */
    public void broadcastSessionStats(String sessionCode, Object statsMessage) {
        String topic = "/topic/session/" + sessionCode + "/stats";
        messagingTemplate.convertAndSend(topic, statsMessage);
        log.debug("Broadcasted session stats to session {}", sessionCode);
    }

    /**
     * Send real-time progress update to all participants
     */
    public void broadcastProgressUpdate(String sessionCode, Object progressMessage) {
        String topic = "/topic/session/" + sessionCode + "/progress";
        messagingTemplate.convertAndSend(topic, progressMessage);
        log.debug("Broadcasted progress update to session {}", sessionCode);
    }
}
