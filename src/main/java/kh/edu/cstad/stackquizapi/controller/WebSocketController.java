package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.domain.QuizEvent;
import kh.edu.cstad.stackquizapi.dto.websocket.HostCommandMessage;
import kh.edu.cstad.stackquizapi.service.QuestionService;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller for handling realtime quiz events
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionService questionService; // service that manages quiz questions
    private final QuizSessionService quizSessionService;
    /**
     * Handles messages sent to /app/events
     * Broadcasts them to /topic/events (all clients)
     */
    @MessageMapping("/events")
    public void handleGlobalEvent(@Payload QuizEvent event) {
        messagingTemplate.convertAndSend("/topic/events", event);
    }

    @MessageMapping("/session/{sessionId}/host-command")
    public void handleHostCommand(
            @DestinationVariable String sessionId,
            @Payload HostCommandMessage commandMsg
    ) {
        log.info("Received host command: {} for session {}", commandMsg.getCommand(), sessionId);
        switch (commandMsg.getCommand()) {
            case "START_SESSION" -> quizSessionService.startSession(sessionId);
            case "NEXT_QUESTION" -> quizSessionService.advanceToNextQuestion(sessionId);
            case "PAUSE_SESSION" -> quizSessionService.pauseSession(sessionId);
            case "END_SESSION" -> quizSessionService.endSession(sessionId);
            // Add other cases as needed
            default -> log.warn("Unknown command: {}", commandMsg.getCommand());
        }
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