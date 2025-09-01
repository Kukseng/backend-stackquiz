package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.AnswerSubmissionMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.HostCommandMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import kh.edu.cstad.stackquizapi.service.QuestionServiceExtended;
import kh.edu.cstad.stackquizapi.service.QuizSessionServiceExtended;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuizSessionServiceExtended quizSessionService;
    private final ParticipantService participantService;
    private final LeaderboardService leaderboardService;
    private final QuestionServiceExtended questionService;

    /**
     * Handle participant joining a quiz session
     */
    @MessageMapping("/session/{sessionId}/join")
    public void joinSession(@DestinationVariable String sessionId,
                            @Payload JoinSessionRequest request,
                            Principal principal) {
        try {
            log.info("Participant attempting to join session: {}", sessionId);

            ParticipantResponse participant = participantService.joinSession(request);

            List<ParticipantResponse> participants = participantService.getSessionParticipants(sessionId);

            ParticipantMessage participantMessage = new ParticipantMessage(
                    sessionId,
                    participant.id(),
                    participants,
                    participants.size(),
                    "PARTICIPANT_JOINED"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", participantMessage);

            GameStateMessage welcomeMessage = new GameStateMessage(
                    sessionId,
                    "SYSTEM",
                    null,
                    "PARTICIPANT_WELCOMED",
                    null,
                    null,
                    null,
                    "Welcome to the quiz session!"
            );

            messagingTemplate.convertAndSendToUser(
                    participant.id(),
                    "/topic/session/" + sessionId + "/game-state",
                    welcomeMessage
            );

        } catch (Exception e) {
            log.error("Error joining session {}: {}", sessionId, e.getMessage());
            sendErrorMessage(sessionId, principal.getName(), "Failed to join session: " + e.getMessage());
        }
    }

    /**
     * Handle host commands for session management
     */
    @MessageMapping("/session/{sessionId}/host-command")
    public void handleHostCommand(@DestinationVariable String sessionId,
                                  @Payload HostCommandMessage command,
                                  Principal principal) {
        try {
            log.info("Host command received for session {}: {}", sessionId, command.getCommand());

            switch (command.getCommand()) {
                case "START_SESSION":
                    startQuizSession(sessionId, command.getHostId());
                    break;
                case "NEXT_QUESTION":
                    nextQuestion(sessionId, command.getHostId());
                    break;
                case "PAUSE_SESSION":
                    pauseSession(sessionId, command.getHostId());
                    break;
                case "END_SESSION":
                    endSession(sessionId, command.getHostId());
                    break;
                case "SHOW_RESULTS":
                    showResults(sessionId, command.getHostId());
                    break;
                default:
                    log.warn("Unknown host command: {}", command.getCommand());
            }

        } catch (Exception e) {
            log.error("Error handling host command for session {}: {}", sessionId, e.getMessage());
            sendErrorMessage(sessionId, principal.getName(), "Failed to execute command: " + e.getMessage());
        }
    }

    /**
     * Handle answer submissions from participants
     */
    @MessageMapping("/session/{sessionId}/submit-answer")
    public void submitAnswer(@DestinationVariable String sessionId,
                             @Payload AnswerSubmissionMessage answerMessage,
                             Principal principal) {
        try {
            log.info("Answer submitted for session {} by participant {}", sessionId, answerMessage.getParticipantId());

            SubmitAnswerRequest submitRequest = new SubmitAnswerRequest(
                    answerMessage.getParticipantId(),
                    answerMessage.getQuestionId(),
                    answerMessage.getSelectedOptionId(),
                    null, // answerText
                    answerMessage.getResponseTime().intValue(),
                    sessionId
            );

            SubmitAnswerResponse response = participantService.submitAnswer(submitRequest);

            answerMessage.setIsCorrect(response.isCorrect());
            answerMessage.setPointsEarned(response.pointsEarned());

            messagingTemplate.convertAndSendToUser(
                    answerMessage.getParticipantId(),
                    "/topic/session/" + sessionId + "/answer-result",
                    answerMessage
            );

            updateLeaderboard(sessionId);

            List<ParticipantResponse> participants = participantService.getSessionParticipants(sessionId);
            ParticipantMessage participantUpdate = new ParticipantMessage(
                    sessionId,
                    "SYSTEM",
                    participants,
                    participants.size(),
                    "PARTICIPANT_ANSWERED"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/participants", participantUpdate);

        } catch (Exception e) {
            log.error("Error submitting answer for session {}: {}", sessionId, e.getMessage());
            sendErrorMessage(sessionId, principal.getName(), "Failed to submit answer: " + e.getMessage());
        }
    }

    /**
     * Handle subscription to session updates
     */
    @SubscribeMapping("/topic/session/{sessionId}/game-state")
    public GameStateMessage subscribeToGameState(@DestinationVariable String sessionId, Principal principal) {
        try {
            SessionResponse session = quizSessionService.getSessionById(sessionId);

            return new GameStateMessage(
                    sessionId,
                    "SYSTEM",
                    Status.valueOf(session.status()),
                    "SESSION_STATUS",
                    session.currentQuestion(),
                    session.totalQuestions(),
                    null,
                    "Connected to session"
            );

        } catch (Exception e) {
            log.error("Error subscribing to game state for session {}: {}", sessionId, e.getMessage());
            return new GameStateMessage(
                    sessionId,
                    "SYSTEM",
                    null,
                    "ERROR",
                    null,
                    null,
                    null,
                    "Failed to get session state"
            );
        }
    }

    private void startQuizSession(String sessionId, String hostId) {
        try {
            // Update session status to ACTIVE
            SessionResponse session = quizSessionService.startSession(sessionId);

            // Broadcast session started message
            GameStateMessage gameState = new GameStateMessage(
                    sessionId,
                    hostId,
                    Status.valueOf(session.status()),
                    "SESSION_STARTED",
                    0,
                    session.totalQuestions(),
                    null,
                    "Quiz session has started!"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/game-state", gameState);

            nextQuestion(sessionId, hostId);

        } catch (Exception e) {
            log.error("Error starting session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void nextQuestion(String sessionId, String hostId) {
        try {
            QuestionResponse question = questionService.getNextQuestionForSession(sessionId);

            if (question != null) {
                SessionResponse session = quizSessionService.getSessionById(sessionId);

                QuestionMessage questionMessage = new QuestionMessage(
                        sessionId,
                        hostId,
                        question,
                        session.currentQuestion() + 1,
                        session.totalQuestions(),
                        60, // Default 60 seconds per question
                        "START_QUESTION"
                );

                messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/questions", questionMessage);

                GameStateMessage gameState = new GameStateMessage(
                        sessionId,
                        hostId,
                        Status.valueOf(session.status()),
                        "QUESTION_STARTED",
                        session.currentQuestion(),
                        session.totalQuestions(),
                        60L,
                        "New question available"
                );

                messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/game-state", gameState);

            } else {
                endSession(sessionId, hostId);
            }

        } catch (Exception e) {
            log.error("Error getting next question for session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void pauseSession(String sessionId, String hostId) {
        try {
            SessionResponse session = quizSessionService.pauseSession(sessionId);

            GameStateMessage gameState = new GameStateMessage(
                    sessionId,
                    hostId,
                    Status.valueOf(session.status()),
                    "SESSION_PAUSED",
                    session.currentQuestion(),
                    session.totalQuestions(),
                    null,
                    "Session has been paused by the host"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/game-state", gameState);

        } catch (Exception e) {
            log.error("Error pausing session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void endSession(String sessionId, String hostId) {
        try {
            SessionResponse session = quizSessionService.endSession(sessionId);

            GameStateMessage gameState = new GameStateMessage(
                    sessionId,
                    hostId,
                    Status.valueOf(session.status()),
                    "SESSION_ENDED",
                    session.currentQuestion(),
                    session.totalQuestions(),
                    null,
                    "Quiz session has ended"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/game-state", gameState);

            showResults(sessionId, hostId);

        } catch (Exception e) {
            log.error("Error ending session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void showResults(String sessionId, String hostId) {
        try {
            updateLeaderboard(sessionId);

        } catch (Exception e) {
            log.error("Error showing results for session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void updateLeaderboard(String sessionId) {
        try {
            LeaderboardResponse leaderboard = leaderboardService.getRealTimeLeaderboard(
                    new kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest(sessionId, 50, 0, false, null)
            );

            LeaderboardMessage leaderboardMessage = new LeaderboardMessage(
                    sessionId,
                    "SYSTEM",
                    leaderboard,
                    "SCORE_UPDATE"
            );

            messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/leaderboard", leaderboardMessage);

        } catch (Exception e) {
            log.error("Error updating leaderboard for session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void sendErrorMessage(String sessionId, String userId, String errorMessage) {
        GameStateMessage errorMsg = new GameStateMessage(
                sessionId,
                "SYSTEM",
                null,
                "ERROR",
                null,
                null,
                null,
                errorMessage
        );

        messagingTemplate.convertAndSendToUser(userId, "/topic/session/" + sessionId + "/errors", errorMsg);
    }
}
