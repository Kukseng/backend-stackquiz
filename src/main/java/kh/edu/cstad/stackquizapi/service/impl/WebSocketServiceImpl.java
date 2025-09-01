package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.WebSocketMessage;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CONNECTION_KEY_PREFIX = "ws:connections:session:";
    private static final String PARTICIPANT_KEY_PREFIX = "ws:participant:";

    @Override
    public void broadcastToSession(String sessionId, WebSocketMessage message) {
        try {
            String topic = "/topic/session/" + sessionId + "/broadcast";
            messagingTemplate.convertAndSend(topic, message);
            log.debug("Broadcasted message to session {}: {}", sessionId, message.getMessageType());
        } catch (Exception e) {
            log.error("Error broadcasting to session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void sendToParticipant(String participantId, String sessionId, WebSocketMessage message) {
        try {
            String destination = "/topic/session/" + sessionId + "/participant";
            messagingTemplate.convertAndSendToUser(participantId, destination, message);
            log.debug("Sent message to participant {} in session {}: {}", participantId, sessionId, message.getMessageType());
        } catch (Exception e) {
            log.error("Error sending to participant {} in session {}: {}", participantId, sessionId, e.getMessage());
        }
    }

    @Override
    public void sendToHost(String hostId, String sessionId, WebSocketMessage message) {
        try {
            String destination = "/topic/session/" + sessionId + "/host";
            messagingTemplate.convertAndSendToUser(hostId, destination, message);
            log.debug("Sent message to host {} in session {}: {}", hostId, sessionId, message.getMessageType());
        } catch (Exception e) {
            log.error("Error sending to host {} in session {}: {}", hostId, sessionId, e.getMessage());
        }
    }

    @Override
    public void broadcastQuestion(String sessionId, QuestionMessage questionMessage) {
        try {
            String topic = "/topic/session/" + sessionId + "/questions";
            messagingTemplate.convertAndSend(topic, questionMessage);
            log.info("Broadcasted question to session {}: Question {}/{}",
                    sessionId, questionMessage.getQuestionNumber(), questionMessage.getTotalQuestions());
        } catch (Exception e) {
            log.error("Error broadcasting question to session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void broadcastLeaderboard(String sessionId, LeaderboardMessage leaderboardMessage) {
        try {
            String topic = "/topic/session/" + sessionId + "/leaderboard";
            messagingTemplate.convertAndSend(topic, leaderboardMessage);
            log.debug("Broadcasted leaderboard update to session {}: {}", sessionId, leaderboardMessage.getUpdateType());
        } catch (Exception e) {
            log.error("Error broadcasting leaderboard to session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void broadcastParticipantUpdate(String sessionId, ParticipantMessage participantMessage) {
        try {
            String topic = "/topic/session/" + sessionId + "/participants";
            messagingTemplate.convertAndSend(topic, participantMessage);
            log.debug("Broadcasted participant update to session {}: {} participants, action: {}",
                    sessionId, participantMessage.getTotalParticipants(), participantMessage.getAction());
        } catch (Exception e) {
            log.error("Error broadcasting participant update to session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void broadcastGameState(String sessionId, GameStateMessage gameStateMessage) {
        try {
            String topic = "/topic/session/" + sessionId + "/game-state";
            messagingTemplate.convertAndSend(topic, gameStateMessage);
            log.info("Broadcasted game state to session {}: {} - {}",
                    sessionId, gameStateMessage.getAction(), gameStateMessage.getHostMessage());
        } catch (Exception e) {
            log.error("Error broadcasting game state to session {}: {}", sessionId, e.getMessage());
        }
    }

    @Override
    public void handleParticipantConnect(String sessionId, String participantId) {
        try {
            String connectionKey = CONNECTION_KEY_PREFIX + sessionId;
            String participantKey = PARTICIPANT_KEY_PREFIX + participantId;

            // Add participant to session connections set
            redisTemplate.opsForSet().add(connectionKey, participantId);

            // Set participant connection status with expiration
            redisTemplate.opsForValue().set(participantKey, sessionId, 30, TimeUnit.MINUTES);

            log.info("Participant {} connected to session {}", participantId, sessionId);

        } catch (Exception e) {
            log.error("Error handling participant connect for {} in session {}: {}", participantId, sessionId, e.getMessage());
        }
    }

    @Override
    public void handleParticipantDisconnect(String sessionId, String participantId) {
        try {
            String connectionKey = CONNECTION_KEY_PREFIX + sessionId;
            String participantKey = PARTICIPANT_KEY_PREFIX + participantId;

            // Remove participant from session connections set
            redisTemplate.opsForSet().remove(connectionKey, participantId);

            // Remove participant connection status
            redisTemplate.delete(participantKey);

            log.info("Participant {} disconnected from session {}", participantId, sessionId);

        } catch (Exception e) {
            log.error("Error handling participant disconnect for {} in session {}: {}", participantId, sessionId, e.getMessage());
        }
    }

    @Override
    public int getActiveConnectionCount(String sessionId) {
        try {
            String connectionKey = CONNECTION_KEY_PREFIX + sessionId;
            Set<String> connections = redisTemplate.opsForSet().members(connectionKey);
            return connections != null ? connections.size() : 0;
        } catch (Exception e) {
            log.error("Error getting active connection count for session {}: {}", sessionId, e.getMessage());
            return 0;
        }
    }
}

