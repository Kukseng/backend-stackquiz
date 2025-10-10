package kh.edu.cstad.stackquizapi.controller;


import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.websocket.AnswerSubmissionMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.HostCommandMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.JoinSessionMessage;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final QuizSessionService quizSessionService;
    private final ParticipantService participantService;
    // Host commands
    @MessageMapping("/session/{sessionId}/host-command")
    public void handleHostCommand(
            @DestinationVariable String sessionId,
            @Payload HostCommandMessage commandMsg
    ) {
        log.info("Host command: {} for session {}", commandMsg.getCommand(), sessionId);

        switch (commandMsg.getCommand()) {
            case START_SESSION -> {
                // ✅ FIXED: Pass settings to startSession
                if (commandMsg.getSettings() != null) {
                    quizSessionService.startSessionWithSettings(sessionId, commandMsg.getSettings());
                } else {
                    quizSessionService.startSession(sessionId);
                }
            }
            case NEXT_QUESTION -> quizSessionService.advanceToNextQuestion(sessionId);
            case PAUSE_SESSION -> quizSessionService.pauseSession(sessionId);
            case END_SESSION -> quizSessionService.endSession(sessionId);
            default -> log.warn("Unknown host command: {}", commandMsg.getCommand());
        }
    }
    // Participant joins session
    @MessageMapping("/session/{sessionId}/join")
    public void handleParticipantJoin(
            @DestinationVariable String sessionId,
            @Payload JoinSessionMessage joinMsg
    ) {
        log.info("Participant '{}' joining session {} (avatar: {})",
                joinMsg.getNickname(), sessionId, joinMsg.getAvatarId());

        JoinSessionRequest request = new JoinSessionRequest(
                sessionId,
                joinMsg.getNickname(),
                joinMsg.getAvatarId()
        );

        participantService.joinSession(request);
    }




    // Participant submits an answer
    @MessageMapping("/session/{sessionId}/answer")
    public void handleAnswerSubmission(
            @DestinationVariable String sessionId,
            @Payload AnswerSubmissionMessage answerDto
    ) {
        log.info("Answer received from {} in session {}: {}",
                answerDto.getParticipantId(), sessionId, answerDto.getSelectedOptionId());

        SubmitAnswerRequest request = new SubmitAnswerRequest(
                answerDto.getParticipantId(),      // participantId
                answerDto.getQuestionId(),         // questionId
                answerDto.getSelectedOptionId(),   // optionId
                null,                              // answerText (MCQ only)
                Math.toIntExact(answerDto.getResponseTime()),       // timeTaken (Long)
                sessionId                          // sessionId
        );

        participantService.submitAnswer(request);
    }


}
