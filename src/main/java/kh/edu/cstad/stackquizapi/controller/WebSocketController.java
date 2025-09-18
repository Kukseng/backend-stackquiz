package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.domain.QuizEvent;
import kh.edu.cstad.stackquizapi.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller for handling realtime quiz events
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionService questionService; // service that manages quiz questions

    /**
     * Handles messages sent to /app/events
     * Broadcasts them to /topic/events (all clients)
     */
    @MessageMapping("/events")
    public void handleGlobalEvent(@Payload QuizEvent event) {
        messagingTemplate.convertAndSend("/topic/events", event);
    }

    /**
     * Send event to all users in a specific lab (room)
     * Clients subscribe to /topic/session/{sessionId}
     */
    public void sendToSession(String sessionId, QuizEvent event) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, event);
    }

    /**
     * Utility: Send a private message to a specific participant
     */
    public void sendToParticipant(String participantId, QuizEvent event) {
        messagingTemplate.convertAndSendToUser(participantId, "/queue/private", event);
    }
}