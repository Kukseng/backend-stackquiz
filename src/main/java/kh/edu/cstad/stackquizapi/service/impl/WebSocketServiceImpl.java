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

    }

    @Override
    public void sendToParticipant(String nickname, String sessionCode, WebSocketMessage msg) {
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
}
