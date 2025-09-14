package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.domain.QuizEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles messages from clients sent to /app/events
     * Broadcasts to all subscribers of /topic/events
     */
    @MessageMapping("/events")
    @SendTo("/topic/events")
    public QuizEvent handleGlobalEvent(@Payload QuizEvent event) {
        return event;
    }

    /**
     * Send event to all users in a specific lab (room)
     * Clients subscribe to /topic/session/{sessionId}
     */
    public void sendToSession(String sessionId, QuizEvent event) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, event);
    }


    /**
     * Send a private event to a specific user
     * Clients subscribe to /participant/queue/private
     */
    public void sendToParticipant(String nickname, QuizEvent event) {
        messagingTemplate.convertAndSendToUser(nickname, "/queue/private", event);
    }

}
