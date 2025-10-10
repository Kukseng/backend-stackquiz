package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.SessionTimingRequest;
import kh.edu.cstad.stackquizapi.dto.response.HostDashboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.SessionTimerMessage;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.HostDashboardService;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.RealTimeStatsService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HostDashboardServiceImpl implements HostDashboardService {

    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository; // Added this line
    private final LeaderboardService leaderboardService;
    private final RealTimeStatsService realTimeStatsService;
    private final WebSocketService webSocketService;
    private final ParticipantMapper participantMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private static final String SESSION_TIMER_PREFIX = "timer:session:";
    private static final String QUESTION_TIMER_PREFIX = "timer:question:";
    private static final String SCHEDULED_EVENTS_PREFIX = "scheduled:";

    @Override
    public HostDashboardResponse getHostDashboard(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            
            // Get real-time statistics
            LiveStatsMessage liveStats = realTimeStatsService.calculateLiveStats(sessionId);
            HostProgressMessage hostProgress = realTimeStatsService.calculateHostProgress(
                    sessionId, session.getCurrentQuestion() != null ? session.getCurrentQuestion() : 0);

            // Get current leaderboard
            LeaderboardResponse leaderboard = leaderboardService.getRealTimeLeaderboard(
                    new LeaderboardRequest(sessionId, 20, 0, false, null));

            // Get top performers
            List<ParticipantResponse> topPerformers = participants.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()))
                    .limit(5)
                    .map(participantMapper::toParticipantResponse)
                    .collect(Collectors.toList());

            // Get current question information
            String currentQuestionId = null;
            String currentQuestionText = null;
            Map<String, Integer> answerDistribution = new HashMap<>();
            
            if (session.getCurrentQuestion() != null && session.getCurrentQuestion() > 0) {
                // Get current question details
                List<Question> questions = questionRepository.findByQuizId(session.getQuiz().getId()).stream()
                        .sorted(Comparator.comparingInt(Question::getQuestionOrder))
                        .collect(Collectors.toList());
                
                if (session.getCurrentQuestion() <= questions.size()) {
                    Question currentQuestion = questions.get(session.getCurrentQuestion() - 1);
                    currentQuestionId = currentQuestion.getId();
                    currentQuestionText = currentQuestion.getText();
                    answerDistribution = getAnswerDistribution(sessionId, currentQuestionId);
                }
            }

            // Calculate session analytics
            Map<String, Object> sessionAnalytics = getSessionAnalytics(sessionId);
            Map<String, Object> performanceMetrics = calculatePerformanceMetrics(sessionId);

            // Determine control states
            boolean canStart = session.getStatus() == Status.WAITING;
            boolean canPause = session.getStatus() == Status.IN_PROGRESS;
            boolean canResume = session.getStatus() == Status.PAUSED;
            boolean canEnd = session.getStatus() == Status.IN_PROGRESS || session.getStatus() == Status.PAUSED;
            boolean canAdvanceQuestion = session.getStatus() == Status.IN_PROGRESS && 
                    session.getCurrentQuestion() < session.getTotalQuestions();

            // Get session timer
            SessionTimerMessage currentTimer = getSessionTimer(sessionId);

            return HostDashboardResponse.builder()
                    .sessionId(sessionId)
                    .sessionCode(session.getSessionCode())
                    .sessionName(session.getSessionName())
                    .sessionStatus(session.getStatus())
                    .createdAt(session.getCreatedAt())
                    .startTime(session.getStartTime())
                    .endTime(session.getEndTime())
                    .scheduledStartTime(session.getScheduledStartTime())
                    .scheduledEndTime(session.getScheduledEndTime())
                    .defaultQuestionTimeLimit(session.getDefaultQuestionTimeLimit())
                      .autoAdvanceQuestions(false) // Default value since field doesn't exist
                    .allowLateJoining(session.getAllowJoinInProgress())
                    .currentTimer(currentTimer)
                    .currentQuestion(session.getCurrentQuestion())
                    .totalQuestions(session.getTotalQuestions())
                    .totalParticipants(participants.size())
                    .activeParticipants((int) participants.stream().filter(p -> p.getIsConnected()).count())
                    .participantsAnswered(hostProgress.getParticipantsAnswered())
                    .participantsPending(participants.size() - hostProgress.getParticipantsAnswered())
                    .liveStats(liveStats)
                    .hostProgress(hostProgress)
                    .participantProgress(hostProgress.getParticipantProgress())
                    .currentQuestionId(currentQuestionId)
                    .currentQuestionText(currentQuestionText)
                    .answerDistribution(answerDistribution)
                    .currentLeaderboard(leaderboard)
                    .topPerformers(topPerformers)
                    .sessionAnalytics(sessionAnalytics)
                    .performanceMetrics(performanceMetrics)
                    .canStart(canStart)
                    .canPause(canPause)
                    .canResume(canResume)
                    .canEnd(canEnd)
                    .canAdvanceQuestion(canAdvanceQuestion)
                    .canGoBack(false) // Not implemented yet
                    .activeAlerts(getActiveAlerts(sessionId))
                    .recentNotifications(getRecentNotifications(sessionId))
                    .canExportData(true)
                    .reportUrl("/api/v1/sessions/" + sessionId + "/report")
                    .build();

        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                log.warn("Session or Quiz not found for dashboard: {}", sessionId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session or Quiz not found");
            } else {
                log.error("Unexpected error getting host dashboard for session {}: {}", sessionId, e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get host dashboard", e);
            }
        }
    }

    @Override
    public void updateSessionTiming(String sessionId, SessionTimingRequest request) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            // Update session timing settings
            if (request.scheduledStartTime() != null) {
                session.setScheduledStartTime(request.scheduledStartTime());
            }
            if (request.scheduledEndTime() != null) {
                session.setScheduledEndTime(request.scheduledEndTime());
            }
            if (request.defaultQuestionTimeLimit() != null) {
                session.setDefaultQuestionTimeLimit(request.defaultQuestionTimeLimit());
            }
            // autoAdvanceQuestions field doesn't exist in QuizSession
            // This would need to be added to the domain model
            if (request.allowLateJoining() != null) {
                session.setAllowJoinInProgress(request.allowLateJoining());
            }

            quizSessionRepository.save(session);

            // Schedule events if needed
            if (request.scheduledStartTime() != null && request.scheduledStartTime().isAfter(LocalDateTime.now())) {
                scheduleSessionStart(sessionId, request.scheduledStartTime());
            }
            if (request.scheduledEndTime() != null && request.scheduledEndTime().isAfter(LocalDateTime.now())) {
                scheduleSessionEnd(sessionId, request.scheduledEndTime());
            }

            // Broadcast timing update to host
            broadcastHostProgress(sessionId);

            log.info("Updated session timing for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error updating session timing for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to update session timing", e);
        }
    }

    @Override
    public void startSessionTimer(String sessionId) {
        try {
            String timerKey = SESSION_TIMER_PREFIX + sessionId;
            long startTime = System.currentTimeMillis();
            
            redisTemplate.opsForValue().set(timerKey + ":start", String.valueOf(startTime));
            redisTemplate.opsForValue().set(timerKey + ":status", "RUNNING");
            redisTemplate.expire(timerKey + ":start", Duration.ofHours(24));
            redisTemplate.expire(timerKey + ":status", Duration.ofHours(24));

            broadcastTimerUpdate(sessionId);

            log.debug("Started session timer for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error starting session timer for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void pauseSessionTimer(String sessionId) {
        try {
            String timerKey = SESSION_TIMER_PREFIX + sessionId;
            redisTemplate.opsForValue().set(timerKey + ":status", "PAUSED");
            redisTemplate.expire(timerKey + ":status", Duration.ofHours(24));

            broadcastTimerUpdate(sessionId);

            log.debug("Paused session timer for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error pausing session timer for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void resumeSessionTimer(String sessionId) {
        try {
            String timerKey = SESSION_TIMER_PREFIX + sessionId;
            redisTemplate.opsForValue().set(timerKey + ":status", "RUNNING");
            redisTemplate.expire(timerKey + ":status", Duration.ofHours(24));

            broadcastTimerUpdate(sessionId);

            log.debug("Resumed session timer for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error resuming session timer for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void endSessionTimer(String sessionId) {
        try {
            String timerKey = SESSION_TIMER_PREFIX + sessionId;
            redisTemplate.opsForValue().set(timerKey + ":status", "STOPPED");
            redisTemplate.expire(timerKey + ":status", Duration.ofHours(24));

            broadcastTimerUpdate(sessionId);

            log.debug("Ended session timer for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error ending session timer for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public SessionTimerMessage getSessionTimer(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            String timerKey = SESSION_TIMER_PREFIX + sessionId;
            String startTimeStr = redisTemplate.opsForValue().get(timerKey + ":start");
            String status = redisTemplate.opsForValue().get(timerKey + ":status");

            if (startTimeStr == null || status == null) {
                status = "STOPPED";
            }

            long startTime = startTimeStr != null ? Long.parseLong(startTimeStr) : System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            int elapsedSeconds = (int) ((currentTime - startTime) / 1000);

            // Calculate remaining time based on session settings
            int totalSeconds = 3600; // Default 1 hour
            if (session.getScheduledEndTime() != null && session.getStartTime() != null) {
                totalSeconds = (int) Duration.between(session.getStartTime(), session.getScheduledEndTime()).getSeconds();
            }

            int remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);

            return new SessionTimerMessage(
                    session.getSessionCode(),
                    "SYSTEM",
                    "SESSION",
                    status,
                    remainingSeconds,
                    totalSeconds,
                    session.getStartTime(),
                    session.getScheduledEndTime(),
                    session.getCurrentQuestion() != null ? session.getCurrentQuestion() : 0,
                    session.getTotalQuestions() != null ? session.getTotalQuestions() : 0,
                      false, // Default value since field doesn't exist
                    remainingSeconds <= 0 ? "END_SESSION" : "CONTINUE",
                    System.currentTimeMillis()
            );

        } catch (Exception e) {
            log.error("Error getting session timer for session {}: {}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void broadcastTimerUpdate(String sessionId) {
        try {
            SessionTimerMessage timer = getSessionTimer(sessionId);
            if (timer != null) {
                  // webSocketService.broadcastSessionStats(timer.getSessionCode(), timer);
                  // Method doesn't exist, would need to be implemented
            }

        } catch (Exception e) {
            log.error("Error broadcasting timer update for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void setQuestionTimeLimit(String sessionId, int timeLimit) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            // Store current question time limit in Redis
            String timerKey = QUESTION_TIMER_PREFIX + sessionId + ":" + session.getCurrentQuestion();
            redisTemplate.opsForValue().set(timerKey, String.valueOf(timeLimit));
            redisTemplate.expire(timerKey, Duration.ofHours(1));

            // Broadcast timer update
            broadcastTimerUpdate(sessionId);

            log.debug("Set question time limit to {} seconds for session {}", timeLimit, sessionId);

        } catch (Exception e) {
            log.error("Error setting question time limit for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId) {
        return realTimeStatsService.getParticipantProgress(sessionId);
    }

    @Override
    public Map<String, Object> getCurrentQuestionStats(String sessionId, String questionId) {
        try {
              List<ParticipantAnswer> answers = participantAnswerRepository.findByQuestionIdAndParticipantSessionId(questionId, sessionId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAnswers", answers.size());
            stats.put("correctAnswers", answers.stream().mapToInt(a -> a.getIsCorrect() ? 1 : 0).sum());
            stats.put("averageTime", answers.stream().mapToInt(ParticipantAnswer::getTimeTaken).average().orElse(0.0));
            stats.put("fastestTime", answers.stream().mapToInt(ParticipantAnswer::getTimeTaken).min().orElse(0));
            stats.put("slowestTime", answers.stream().mapToInt(ParticipantAnswer::getTimeTaken).max().orElse(0));
            
            return stats;

        } catch (Exception e) {
            log.error("Error getting current question stats for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public void forceAdvanceQuestion(String sessionId) {
        // This would integrate with QuizSessionService to advance question
        log.info("Force advance question requested for session {}", sessionId);
    }

    @Override
    public Map<String, Object> getSessionAnalytics(String sessionId) {
        try {
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(sessionId);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalParticipants", participants.size());
            analytics.put("totalAnswers", allAnswers.size());
            analytics.put("averageScore", participants.stream().mapToInt(Participant::getTotalScore).average().orElse(0.0));
            analytics.put("highestScore", participants.stream().mapToInt(Participant::getTotalScore).max().orElse(0));
            analytics.put("lowestScore", participants.stream().mapToInt(Participant::getTotalScore).min().orElse(0));
            analytics.put("completionRate", calculateCompletionRate(sessionId));
            analytics.put("engagementScore", calculateEngagementScore(sessionId));

            return analytics;

        } catch (Exception e) {
            log.error("Error getting session analytics for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public void scheduleSessionStart(String sessionId, LocalDateTime startTime) {
        try {
            long delay = Duration.between(LocalDateTime.now(), startTime).toMillis();
            if (delay > 0) {
                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    // Trigger session start
                    log.info("Auto-starting scheduled session {}", sessionId);
                    // This would integrate with QuizSessionService
                }, delay, TimeUnit.MILLISECONDS);

                scheduledTasks.put("start:" + sessionId, task);
                log.info("Scheduled session {} to start at {}", sessionId, startTime);
            }

        } catch (Exception e) {
            log.error("Error scheduling session start for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void scheduleSessionEnd(String sessionId, LocalDateTime endTime) {
        try {
            long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
            if (delay > 0) {
                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    // Trigger session end
                    log.info("Auto-ending scheduled session {}", sessionId);
                    // This would integrate with QuizSessionService
                }, delay, TimeUnit.MILLISECONDS);

                scheduledTasks.put("end:" + sessionId, task);
                log.info("Scheduled session {} to end at {}", sessionId, endTime);
            }

        } catch (Exception e) {
            log.error("Error scheduling session end for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void cancelScheduledEvents(String sessionId) {
        try {
            scheduledTasks.entrySet().removeIf(entry -> {
                if (entry.getKey().contains(sessionId)) {
                    entry.getValue().cancel(false);
                    return true;
                }
                return false;
            });

            log.info("Cancelled scheduled events for session {}", sessionId);

        } catch (Exception e) {
            log.error("Error cancelling scheduled events for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getSessionTiming(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            Map<String, Object> timing = new HashMap<>();
            timing.put("scheduledStartTime", session.getScheduledStartTime());
            timing.put("scheduledEndTime", session.getScheduledEndTime());
            timing.put("actualStartTime", session.getStartTime());
            timing.put("actualEndTime", session.getEndTime());
            timing.put("defaultQuestionTimeLimit", session.getDefaultQuestionTimeLimit());
              timing.put("autoAdvanceQuestions", false); // Default value since field doesn't exist

            return timing;

        } catch (Exception e) {
            log.error("Error getting session timing for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public void broadcastHostProgress(String sessionId) {
        try {
            HostProgressMessage progress = realTimeStatsService.calculateHostProgress(sessionId, 0);
            webSocketService.broadcastHostProgress(sessionId, progress);

        } catch (Exception e) {
            log.error("Error broadcasting host progress for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void sendHostNotification(String sessionId, String message, String type) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("message", message);
            notification.put("type", type);
            notification.put("timestamp", System.currentTimeMillis());

            webSocketService.sendToHost(sessionId, notification);

        } catch (Exception e) {
            log.error("Error sending host notification for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> getAnswerDistribution(String sessionId, String questionId) {
        return realTimeStatsService.getAnswerDistribution(sessionId, questionId);
    }

    @Override
    public Map<String, Object> exportSessionData(String sessionId) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Ensure the quiz object is initialized before accessing its questions
            session.setQuiz(quizRepository.findById(session.getQuiz().getId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found for session")));

            Map<String, Object> exportData = new HashMap<>();
            exportData.put("session", session);
            exportData.put("participants", participantRepository.findBySessionId(sessionId));
            exportData.put("answers", participantAnswerRepository.findByParticipantSessionId(sessionId));
            exportData.put("analytics", getSessionAnalytics(sessionId));
            exportData.put("exportedAt", LocalDateTime.now());

            return exportData;

        } catch (Exception e) {
            log.error("Error exporting session data for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    // Private helper methods

    private Map<String, Object> calculatePerformanceMetrics(String sessionId) {
        Map<String, Object> metrics = new HashMap<>();
        // Implementation would calculate various performance metrics
        metrics.put("responseRate", 0.85);
        metrics.put("averageEngagement", 0.78);
        metrics.put("difficultyBalance", 0.72);
        return metrics;
    }

    private List<String> getActiveAlerts(String sessionId) {
        List<String> alerts = new ArrayList<>();
        // Implementation would check for various alert conditions
        return alerts;
    }

    private List<String> getRecentNotifications(String sessionId) {
        List<String> notifications = new ArrayList<>();
        // Implementation would get recent notifications
        return notifications;
    }

    private double calculateCompletionRate(String sessionId) {
        // Implementation would calculate completion rate
        return 0.85;
    }

    private double calculateEngagementScore(String sessionId) {
        // Implementation would calculate engagement score
        return 0.78;
    }
}
