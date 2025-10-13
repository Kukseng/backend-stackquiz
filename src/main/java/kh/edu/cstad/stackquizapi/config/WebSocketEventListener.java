package kh.edu.cstad.stackquizapi.config;

import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketService webSocketService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket connection established: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String participantId = (String) sessionAttributes.get("participantId");
            String quizSessionId = (String) sessionAttributes.get("quizSessionId");

            if (participantId != null && quizSessionId != null) {
                webSocketService.handleParticipantDisconnect(quizSessionId, participantId);
                log.info("Participant {} disconnected from quiz session {}", participantId, quizSessionId);
            }
        }

        log.info("WebSocket connection closed: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();

        log.debug("WebSocket subscription to {}: {}", destination, sessionId);

        if (destination != null && destination.contains("/session/")) {
            String[] parts = destination.split("/");
            if (parts.length >= 4 && "session".equals(parts[2])) {
                String quizSessionId = parts[3];

                Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
                if (sessionAttributes != null) {
                    sessionAttributes.put("quizSessionId", quizSessionId);
                }

                log.info("Client subscribed to quiz session {}", quizSessionId);
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = headerAccessor.getSubscriptionId();
        String sessionId = headerAccessor.getSessionId();

        log.debug("WebSocket unsubscription {}: {}", subscriptionId, sessionId);
    }
}

