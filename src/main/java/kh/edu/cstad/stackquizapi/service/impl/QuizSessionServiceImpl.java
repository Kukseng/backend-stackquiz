package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.ws.rs.NotFoundException;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.*;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.mapper.QuizSessionMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import kh.edu.cstad.stackquizapi.util.QuizMode;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl implements QuizSessionService, DisposableBean {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final QuizSessionMapper quizSessionMapper;
    private final ParticipantMapper participantMapper;
    private final LeaderboardService leaderboardService;
    private final WebSocketService webSocketService;
    private final AvatarRepository avatarRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    @Value("${quiz.default.time-limit:30}")
    private int defaultTimeLimit;

    private static final String PARTICIPANT_PROGRESS_PREFIX = "participant:progress:";
    private static final String SESSION_QUESTIONS_PREFIX = "session:questions:";
    private static final String QUESTION_START_TIME_PREFIX = "question:start:";
    private static final String SESSION_ACTIVE_PREFIX = "session:active:";

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }

    // =================== SESSION CREATION ===================
    @Override
    @Transactional
    public SessionResponse createSession(SessionCreateRequest request, Jwt accessToken) {
        String hostId = accessToken.getSubject();
        User user = userRepository.findById(hostId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        QuizSession quizSession = quizSessionMapper.toSessionRequest(request);
        quizSession.setHost(user);
        quizSession.setHostName(user.getUsername());
        quizSession.setQuiz(quiz);
        quizSession.setSessionName(request.sessionName() != null ? request.sessionName() : quiz.getTitle());
        quizSession.setCurrentQuestion(0);
        quizSession.setCreatedAt(LocalDateTime.now());
        quizSession.setTotalParticipants(0);
        quizSession.setTotalQuestions(quiz.getQuestions().size());
        quizSession.setStatus(Status.WAITING);
        quizSession.setSessionCode(generateUniqueSessionCode());

        quizSession.setMode(request.mode() != null ? request.mode() : QuizMode.ASYNC);

        QuizSession savedSession = quizSessionRepository.save(quizSession);

        cacheSessionQuestions(savedSession);

        leaderboardService.initializeSessionLeaderboard(savedSession.getId());

        log.info("Created quiz session {} (Code: {}) mode={}", savedSession.getId(), savedSession.getSessionCode(), savedSession.getMode());

        webSocketService.broadcastGameState(
                savedSession.getSessionCode(),
                new GameStateMessage(
                        savedSession.getSessionCode(),
                        savedSession.getHostName(),
                        Status.WAITING,
                        "SESSION_LOBBY",
                        0,
                        savedSession.getTotalQuestions(),
                        null,
                        "Lobby opened! Waiting for participants..."
                )
        );

        return quizSessionMapper.toSessionResponse(savedSession);
    }

    // ===================== replace in QuizSessionServiceImpl =====================

    private void cacheSessionQuestions(QuizSession session) {
        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getQuestionOrder))
                .toList();

        String questionsKey = SESSION_QUESTIONS_PREFIX + session.getId();

        redisTemplate.delete(questionsKey);
        for (Question q : questions) {

            redisTemplate.opsForList().rightPush(questionsKey, q.getId());
        }
        redisTemplate.expire(questionsKey, java.time.Duration.ofHours(24));

        log.info("Cached {} questions for session {}", questions.size(), session.getId());
    }

    @Override
    public void sendNextQuestionToParticipant(String participantId, String sessionId, int questionNumber) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Session not found with code: " + sessionId));
            Quiz quiz = quizRepository.findByIdWithQuestions(session.getQuiz().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Quiz not found with id: " + session.getQuiz().getId()));
            List<Question> questions = quiz.getQuestions().stream()
                    .sorted(Comparator.comparingInt(Question::getQuestionOrder))
                    .toList();

            // Completed all questions
            if (questionNumber < 1 || questionNumber > questions.size()) {
                log.info("Participant {} completed quiz in session {}", participantId, session.getSessionCode());

                // mark progress as completed so completion checker picks it up
                String progressKey = PARTICIPANT_PROGRESS_PREFIX + participantId;
                redisTemplate.opsForValue().set(progressKey, String.valueOf(questions.size() + 1));
                redisTemplate.expire(progressKey, java.time.Duration.ofHours(24));

                GameStateMessage completionMsg = new GameStateMessage(
                        session.getSessionCode(),
                        session.getHostName(),
                        Status.IN_PROGRESS,
                        "PARTICIPANT_COMPLETED",
                        session.getTotalQuestions(),
                        session.getTotalQuestions(),
                        null,
                        "You’ve completed the quiz!"
                );

                webSocketService.sendCompletionToParticipant(session.getSessionCode(), participantId, completionMsg);
                // schedule completion check if necessary
                scheduler.schedule(() -> checkSessionCompletion(session), 3, TimeUnit.SECONDS);
                return;
            }

            Question question = questions.get(questionNumber - 1);

            QuestionMessage qMsg = new QuestionMessage(
                    session.getSessionCode(),
                    session.getHostName(),
                    question,
                    questionNumber,
                    session.getTotalQuestions(),
                    question.getTimeLimit() != null ? question.getTimeLimit().intValue() : defaultTimeLimit,
                    "NEXT_QUESTION"
            );

            // Send ONLY to this participant
            webSocketService.sendQuestionToParticipant(session.getSessionCode(), participantId, qMsg);

            // Store participant progress in Redis (use the shared prefix constant)
            String progressKey = PARTICIPANT_PROGRESS_PREFIX + participantId;
            redisTemplate.opsForValue().set(progressKey, String.valueOf(questionNumber));
            redisTemplate.expire(progressKey, java.time.Duration.ofHours(24));


            String startTimeKey = QUESTION_START_TIME_PREFIX + participantId + ":" + question.getId();
            redisTemplate.opsForValue().set(startTimeKey, String.valueOf(System.currentTimeMillis()));
            redisTemplate.expire(startTimeKey, java.time.Duration.ofHours(1));

            // ✅ FIX: Schedule timeout handler to auto-submit if time expires
            int timeLimit = question.getTimeLimit() != null ? question.getTimeLimit().intValue() : defaultTimeLimit;
            String sessionCodeForTimeout = session.getSessionCode();
            scheduler.schedule(() -> handleQuestionTimeout(participantId, question.getId(), sessionCodeForTimeout, questionNumber),
                    timeLimit + 2, TimeUnit.SECONDS); // Add 2 seconds buffer for network delay

            log.info("Sent question {} (id={}) to participant {} in session {} with {} seconds timeout",
                    questionNumber, question.getId(), participantId, session.getSessionCode(), timeLimit);

            // Optionally notify host about participant progress (keeps host UI in sync)
            try {
                ParticipantProgressMessage progressMsg = ParticipantProgressMessage.builder()
                        .sessionCode(session.getSessionCode())
                        .participantId(participantId)
                        .participantNickname(participantRepository.findById(participantId).map(Participant::getNickname).orElse("UNKNOWN"))
                        .currentQuestion(questionNumber)
                        .totalQuestions(session.getTotalQuestions())
                        .totalScore(participantRepository.findById(participantId).map(Participant::getTotalScore).orElse(0))
                        .isCompleted(false)
                        .action("QUESTION_STARTED")
                        .timestamp(System.currentTimeMillis())
                        .build();
                webSocketService.notifyHostParticipantProgress(session.getSessionCode(), progressMsg);
            } catch (Exception e) {
                log.debug("Failed to notify host of participant progress for {}: {}", participantId, e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error sending question to participant {} in session {}: {}", participantId, sessionId, e.getMessage(), e);
        }
    }

    private void sendParticipantCompletion(Participant participant, QuizSession session) {
        log.info("Participant {} completed all questions in session {}",
                participant.getNickname(), session.getSessionCode());

        // mark participant progress as completed
        String progressKey = PARTICIPANT_PROGRESS_PREFIX + participant.getId();
        redisTemplate.opsForValue().set(progressKey, String.valueOf(session.getTotalQuestions() + 1));
        redisTemplate.expire(progressKey, java.time.Duration.ofHours(24));

        // Send completion message to participant
        GameStateMessage completionMsg = new GameStateMessage(
                session.getSessionCode(),
                "SYSTEM",
                Status.IN_PROGRESS,
                "PARTICIPANT_COMPLETED",
                session.getTotalQuestions(),
                session.getTotalQuestions(),
                null,
                "Congratulations! You've completed all questions. Waiting for other participants..."
        );

        webSocketService.sendCompletionToParticipant(session.getSessionCode(), participant.getId(), completionMsg);

        // Schedule completion check
        scheduler.schedule(() -> checkSessionCompletion(session), 5, TimeUnit.SECONDS);
    }

    private int calculateParticipantTimeTaken(String participantId, String questionId) {
        try {

            String startTimeKey = QUESTION_START_TIME_PREFIX + participantId + ":" + questionId;
            String startTimeStr = redisTemplate.opsForValue().get(startTimeKey);

            if (startTimeStr != null) {
                long startTime = Long.parseLong(startTimeStr);
                long currentTime = System.currentTimeMillis();
                int seconds = (int) ((currentTime - startTime) / 1000);
                // remove start time after reading (optional)
                redisTemplate.delete(startTimeKey);
                return Math.max(0, seconds);
            }
        } catch (Exception e) {
            log.error("Error calculating time taken for participant {} question {}: {}", participantId, questionId, e.getMessage());
        }

        return defaultTimeLimit;
    }
//

    @Override
    public SessionResponse startSessionWithSettings(String sessionCode, HostCommandMessage.SessionSettings settings) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new NotFoundException("Session not found for code: " + sessionCode));

        if (session.getStatus() != Status.WAITING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in waiting status.");


        if (settings != null) {
            if (settings.getScheduledStartTime() != null) {
                ZonedDateTime scheduledStart = settings.getScheduledStartTime();
                ZonedDateTime now = ZonedDateTime.now();
                
                // Convert to server timezone for comparison and storage
                ZonedDateTime scheduledStartInServerZone = scheduledStart.withZoneSameInstant(now.getZone());
                
                // ✅ FIX: Validate scheduled time with proper timezone handling
                if (scheduledStartInServerZone.isBefore(now)) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        String.format("Scheduled start time cannot be in the past. Provided: %s, Current: %s", 
                            scheduledStartInServerZone, now)
                    );
                }
                
                if (scheduledStartInServerZone.isBefore(now.plusSeconds(10))) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "Scheduled start time must be at least 10 seconds in the future"
                    );
                }
                
                // Store as LocalDateTime in server timezone
                session.setScheduledStartTime(scheduledStartInServerZone.toLocalDateTime());
                log.info("✅ Session {} scheduled to start at {} (current time: {})", 
                    sessionCode, scheduledStartInServerZone, now);
            }
            if (settings.getScheduledEndTime() != null) {
                ZonedDateTime scheduledEnd = settings.getScheduledEndTime();
                ZonedDateTime now = ZonedDateTime.now();
                
                // Convert to server timezone for comparison and storage
                ZonedDateTime scheduledEndInServerZone = scheduledEnd.withZoneSameInstant(now.getZone());
                LocalDateTime scheduledStart = session.getScheduledStartTime();
                
                // ✅ FIX: Validate scheduled end time with proper timezone handling
                if (scheduledEndInServerZone.isBefore(now)) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        String.format("Scheduled end time cannot be in the past. Provided: %s, Current: %s", 
                            scheduledEndInServerZone, now)
                    );
                }
                
                // Validate end time is after start time
                if (scheduledStart != null && scheduledEndInServerZone.toLocalDateTime().isBefore(scheduledStart)) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        String.format("Scheduled end time (%s) must be after start time (%s)", 
                            scheduledEndInServerZone.toLocalDateTime(), scheduledStart)
                    );
                }
                
                // Ensure reasonable duration (at least 1 minute)
                if (scheduledStart != null) {
                    long durationMinutes = java.time.Duration.between(scheduledStart, scheduledEndInServerZone.toLocalDateTime()).toMinutes();
                    if (durationMinutes < 1) {
                        throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, 
                            "Session duration must be at least 1 minute"
                        );
                    }
                }
                
                // Store as LocalDateTime in server timezone
                session.setScheduledEndTime(scheduledEndInServerZone.toLocalDateTime());
                log.info("✅ Session {} scheduled to end at {} (current time: {})", 
                    sessionCode, scheduledEndInServerZone, now);
            }
            if (settings.getMode() != null) {
                session.setMode(QuizMode.valueOf(settings.getMode()));
            }
            if (settings.getMaxAttempts() != null) {
                session.setMaxAttempts(settings.getMaxAttempts());
            }
            if (settings.getAllowJoinInProgress() != null) {
                session.setAllowJoinInProgress(settings.getAllowJoinInProgress());
            }
            if (settings.getShuffleQuestions() != null) {
                session.setShuffleQuestions(settings.getShuffleQuestions());
            }
            if (settings.getShowCorrectAnswers() != null) {
                session.setShowCorrectAnswers(settings.getShowCorrectAnswers());
            }
            if (settings.getDefaultQuestionTimeLimit() != null) {
                session.setDefaultQuestionTimeLimit(settings.getDefaultQuestionTimeLimit());
            }
            if (settings.getMaxParticipants() != null) {
                session.setMaxParticipants(settings.getMaxParticipants());
            }

            quizSessionRepository.save(session);
        }

        // ✅ FIXED: Check if we should start now or schedule for later
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = session.getScheduledStartTime();

        log.info("🕐 Session {} - Current time: {}, Scheduled start: {}", 
            sessionCode, now, scheduledStart);

        if (scheduledStart != null && scheduledStart.isAfter(now)) {
            // Schedule start for later
            long delaySeconds = java.time.Duration.between(now, scheduledStart).getSeconds();

            log.info("⏰ Session {} scheduled to start at {} (in {} seconds)",
                    sessionCode, scheduledStart, delaySeconds);

            // Broadcast scheduled message
            webSocketService.broadcastGameState(
                    session.getSessionCode(),
                    new GameStateMessage(
                            session.getSessionCode(),
                            session.getHostName(),
                            Status.WAITING,
                            "SESSION_SCHEDULED",
                            0,
                            session.getTotalQuestions(),
                            null,
                            "Quiz scheduled to start at " + scheduledStart
                    )
            );

            // Schedule the actual start
            scheduler.schedule(() -> {
                try {
                    actuallyStartSession(session);
                } catch (Exception e) {
                    log.error("Error starting scheduled session {}", sessionCode, e);
                }
            }, delaySeconds, TimeUnit.SECONDS);

            return quizSessionMapper.toSessionResponse(session);
        } else {
            // Start immediately
            log.info("⚡ Starting session {} immediately (no valid scheduled time)", sessionCode);
            return actuallyStartSession(session);
        }
    }

    private SessionResponse actuallyStartSession(QuizSession session) {
        session.setStatus(Status.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        session.setCurrentQuestion(0);
        quizSessionRepository.save(session);

        // Mark active
        String activeKey = SESSION_ACTIVE_PREFIX + session.getId();
        redisTemplate.opsForValue().set(activeKey, "true");
        redisTemplate.expire(activeKey, java.time.Duration.ofHours(24));

        log.info("Started session {} (Code: {}) in {} mode",
                session.getId(), session.getSessionCode(), session.getMode());

        webSocketService.broadcastGameState(
                session.getSessionCode(),
                new GameStateMessage(
                        session.getSessionCode(),
                        session.getHostName(),
                        Status.IN_PROGRESS,
                        "SESSION_STARTED",
                        0,
                        session.getTotalQuestions(),
                        null,
                        "Quiz started! Get ready!"
                )
        );

        // ✅ ADDED: Schedule auto-end if scheduled end time is set
        if (session.getScheduledEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime scheduledEnd = session.getScheduledEndTime();

            log.info("🕐 Session {} - Current time: {}, Scheduled end: {}", 
                session.getSessionCode(), now, scheduledEnd);

            if (scheduledEnd.isAfter(now)) {
                long delaySeconds = java.time.Duration.between(now, scheduledEnd).getSeconds();
                long durationMinutes = delaySeconds / 60;

                log.info("⏰ Session {} scheduled to auto-end at {} (in {} minutes / {} seconds)",
                        session.getSessionCode(), scheduledEnd, durationMinutes, delaySeconds);

                // Broadcast scheduled end time to participants
                webSocketService.broadcastGameState(
                        session.getSessionCode(),
                        new GameStateMessage(
                                session.getSessionCode(),
                                session.getHostName(),
                                Status.IN_PROGRESS,
                                "SESSION_END_SCHEDULED",
                                session.getCurrentQuestion(),
                                session.getTotalQuestions(),
                                null,
                                String.format("Session will automatically end at %s", scheduledEnd)
                        )
                );

                scheduler.schedule(() -> {
                    try {
                        log.info("⏱️ Auto-ending session {} at scheduled time {}", 
                            session.getSessionCode(), scheduledEnd);
                        endSession(session.getSessionCode());
                    } catch (Exception e) {
                        log.error("❌ Error ending scheduled session {}", session.getSessionCode(), e);
                    }
                }, delaySeconds, TimeUnit.SECONDS);
            } else {
                log.warn("⚠️ Scheduled end time {} is in the past, session will not auto-end", scheduledEnd);
            }
        } else {
            log.info("ℹ️ Session {} has no scheduled end time - will run until manually ended", 
                session.getSessionCode());
        }

        // ✅ FIX: Send first question to all participants regardless of mode
        scheduler.schedule(() -> {
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(session.getId());
            if (session.getMode() == QuizMode.SYNC) {
                log.info("SYNC: Sending first question to {} participants in session {}", 
                    participants.size(), session.getSessionCode());
                // In SYNC mode, advance to question 1 and broadcast to all
                session.setCurrentQuestion(1);
                quizSessionRepository.save(session);
                
                // Broadcast the first question to all participants
                for (Participant participant : participants) {
                    sendNextQuestionToParticipant(participant.getId(), session.getSessionCode(), 1);
                }
            } else {
                log.info("ASYNC: Sending first question to {} participants in session {}",
                    participants.size(), session.getSessionCode());
                // In ASYNC mode, each participant gets their own question flow
                for (Participant participant : participants) {
                    sendNextQuestionToParticipant(participant.getId(), session.getSessionCode(), 1);
                }
            }
        }, 2, TimeUnit.SECONDS);

        return quizSessionMapper.toSessionResponse(session);
    }

    // =================== START SESSION ===================
    @Override
    @Transactional
    public SessionResponse startSession(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new NotFoundException("Session not found for code: " + sessionCode));

        if (session.getStatus() != Status.WAITING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in waiting status.");

        session.setStatus(Status.IN_PROGRESS);
        session.setStartTime(LocalDateTime.now());
        // Always reset question pointer at start
        session.setCurrentQuestion(0);
        quizSessionRepository.save(session);

        // Mark active
        String activeKey = SESSION_ACTIVE_PREFIX + session.getId();
        redisTemplate.opsForValue().set(activeKey, "true");
        redisTemplate.expire(activeKey, java.time.Duration.ofHours(24));

        log.info("Started session {} (Code: {}) in {} mode", session.getId(), sessionCode, session.getMode());

        webSocketService.broadcastGameState(
                session.getSessionCode(),
                new GameStateMessage(
                        session.getSessionCode(),
                        session.getHostName(),
                        Status.IN_PROGRESS,
                        "SESSION_STARTED",
                        0,
                        session.getTotalQuestions(),
                        null,
                        "Quiz started! Get ready!"
                )
        );

        // ✅ FIX: Send first question to all participants regardless of mode
        scheduler.schedule(() -> {
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(session.getId());
            if (session.getMode() == QuizMode.SYNC) {
                log.info("SYNC: Sending first question to {} participants in session {}", 
                    participants.size(), sessionCode);
                // In SYNC mode, advance to question 1 and broadcast to all
                session.setCurrentQuestion(1);
                quizSessionRepository.save(session);
                
                // Broadcast the first question to all participants
                for (Participant participant : participants) {
                    sendNextQuestionToParticipant(participant.getId(), sessionCode, 1);
                }
            } else {
                log.info("ASYNC: Sending first question to {} participants in session {}", 
                    participants.size(), sessionCode);
                // In ASYNC mode, each participant gets their own question flow
                for (Participant participant : participants) {
                    sendNextQuestionToParticipant(participant.getId(), sessionCode, 1);
                }
            }
        }, 2, TimeUnit.SECONDS);

        return quizSessionMapper.toSessionResponse(session);
    }


    private void checkSessionCompletion(QuizSession session) {
        try {
            String activeKey = SESSION_ACTIVE_PREFIX + session.getId();
            String isActive = redisTemplate.opsForValue().get(activeKey);

            if (!"true".equals(isActive)) {
                log.debug("Session {} is not marked as active, skipping completion check", session.getId());
                return;
            }

            // ✅ FIXED: Don't auto-end if scheduled end time is set and not reached
            if (session.getScheduledEndTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(session.getScheduledEndTime())) {
                    log.debug("Session {} has scheduled end time {}, not auto-ending yet",
                            session.getId(), session.getScheduledEndTime());
                    return;
                }
            }

            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(session.getId());

            if (participants.isEmpty()) {
                log.debug("No participants in session {}, not ending session", session.getId());
                return;
            }

            boolean allCompleted = participants.stream().allMatch(participant -> {
                String progressKey = PARTICIPANT_PROGRESS_PREFIX + participant.getId();
                String progress = redisTemplate.opsForValue().get(progressKey);
                int currentQuestion = progress != null ? Integer.parseInt(progress) : 0;
                return currentQuestion > session.getTotalQuestions();
            });

            log.debug("Session {} completion check: {} participants, all completed: {}",
                    session.getId(), participants.size(), allCompleted);

            if (allCompleted) {
                log.info("All participants completed session {}, ending session", session.getId());
                endSession(session.getSessionCode());
            }
        } catch (Exception e) {
            log.error("Error checking session completion for session {}", session.getId(), e);
        }
    }
    // =================== TIMEOUT HANDLER ===================
    /**
     * ✅ FIX: Handle question timeout - notify participant but allow late answer
     */
    private void handleQuestionTimeout(String participantId, String questionId, String sessionCode, int questionNumber) {
        try {
            log.info("Time expired for participant {} on question {} in session {}", 
                    participantId, questionNumber, sessionCode);

            // Check if participant already answered this question
            if (participantAnswerRepository.existsByParticipantIdAndQuestionId(participantId, questionId)) {
                log.debug("Participant {} already answered question {} before timeout", 
                        participantId, questionId);
                return;
            }

            // Get participant and session
            Participant participant = participantRepository.findById(participantId).orElse(null);
            if (participant == null) {
                log.warn("Participant {} not found during timeout handling", participantId);
                return;
            }

            QuizSession session = participant.getSession();

            // ✅ NEW FIX: Just send notification - DON'T auto-submit or move to next question
            // Let participant still answer after time is up, but they won't get speed bonus
            GameStateMessage timeUpNotification = new GameStateMessage(
                    sessionCode,
                    "SYSTEM",
                    Status.IN_PROGRESS,
                    "TIME_UP",
                    questionNumber,
                    session.getTotalQuestions(),
                    null,
                    "Time's up! You can still answer for base points but won't earn speed bonus."
            );
            webSocketService.sendCompletionToParticipant(sessionCode, participantId, timeUpNotification);

            log.info("Sent TIME_UP notification to participant {} - they can still answer", participantId);

        } catch (Exception e) {
            log.error("Error handling timeout for participant {} question {}: {}", 
                    participantId, questionId, e.getMessage(), e);
        }
    }

    // =================== SUBMIT ANSWER ===================
    @Override
    @Transactional
    public void submitAnswer(String sessionCode, String participantId, String selectedOptionId) {
        log.info("Processing answer submission: session={}, participant={}, option={}",
                sessionCode, participantId, selectedOptionId);

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Participant not found"));

        if (!participant.getSession().getSessionCode().equals(sessionCode)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Participant does not belong to this session.");
        }

        // Get participant's current question number
        String progressKey = PARTICIPANT_PROGRESS_PREFIX + participantId;
        String progressStr = redisTemplate.opsForValue().get(progressKey);
        int currentQuestionNumber = progressStr != null ? Integer.parseInt(progressStr) : 1;

        // Get the question ID for this participant's current question
        String questionsKey = SESSION_QUESTIONS_PREFIX + participant.getSession().getId();
        String questionId = redisTemplate.opsForList().index(questionsKey, currentQuestionNumber - 1);

        if (questionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No current question for participant");
        }

        Question currentQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Check if participant already answered this question
        if (participantAnswerRepository.existsByParticipantIdAndQuestionId(participantId, currentQuestion.getId())) {
            log.warn("Participant {} already answered question {}", participantId, currentQuestion.getId());
            return;
        }

        Option selectedOption = currentQuestion.getOptions().stream()
                .filter(o -> o.getId().equals(selectedOptionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option not found"));

        boolean isCorrect = Boolean.TRUE.equals(selectedOption.getIsCorrected());
        
        // Calculate time taken for this specific participant
        int timeTaken = calculateParticipantTimeTaken(participantId, currentQuestion.getId());
        int questionTimeLimit = currentQuestion.getTimeLimit() != null ? currentQuestion.getTimeLimit() : defaultTimeLimit;
        
        // ✅ FIX: Award base points for correct answer, bonus for speed
        int points = 0;
        int bonusPoints = 0;
        
        if (isCorrect) {
            // Always award base points for correct answer
            points = currentQuestion.getPoints();
            
            // Award speed bonus ONLY if answered within time limit
            if (timeTaken <= questionTimeLimit) {
                // Calculate speed bonus (example: up to 20% bonus based on speed)
                // Faster answer = more bonus
                double speedRatio = (double) timeTaken / questionTimeLimit;
                bonusPoints = (int) ((1.0 - speedRatio) * currentQuestion.getPoints() * 0.2);
                
                log.info("On-time answer from participant {} - time: {}s/{}s, base: {}, bonus: {}",
                        participantId, timeTaken, questionTimeLimit, points, bonusPoints);
            } else {
                // Late answer: full base points but NO bonus
                bonusPoints = 0;
                log.info("Late answer from participant {} - time: {}s/{}s, base: {}, bonus: 0 (time expired)",
                        participantId, timeTaken, questionTimeLimit, points);
            }
        }
        
        // Total points = base points + speed bonus
        int totalPoints = points + bonusPoints;

        // Save the answer
        ParticipantAnswer answer = new ParticipantAnswer();
        answer.setParticipant(participant);
        answer.setQuestion(currentQuestion);
        answer.setSelectedAnswer(selectedOption);
        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(totalPoints); // Use total points (base + bonus)
        answer.setTimeTaken(timeTaken);
        participantAnswerRepository.save(answer);

        // Update participant's total score in database
        int newTotalScore = participant.getTotalScore() + totalPoints;
        participant.setTotalScore(newTotalScore);
        participantRepository.save(participant);

        log.info("Answer saved and score updated: participant={}, question={}, correct={}, base={}, bonus={}, total={}, totalScore={}",
                participant.getNickname(), currentQuestionNumber, isCorrect, points, bonusPoints, totalPoints, newTotalScore);

        // Update leaderboard
        QuizSession session = participant.getSession();
        leaderboardService.updateParticipantScore(session.getId(), participantId, participant.getNickname(), newTotalScore);

        // Send answer feedback to participant
        AnswerSubmissionMessage feedback = new AnswerSubmissionMessage(
                sessionCode,
                "SYSTEM",
                participant.getId(),
                participant.getNickname(),
                currentQuestion.getId(),
                selectedOptionId,
                (long) timeTaken,
                isCorrect,
                totalPoints // Send total points (base + bonus)
        );
        webSocketService.sendFeedbackToParticipant(sessionCode, participantId, feedback);

        // Broadcast updated leaderboard to all
        LeaderboardResponse leaderboard = leaderboardService.getRealTimeLeaderboard(
                new LeaderboardRequest(session.getId(), 10, 0, false, null)
        );
        webSocketService.broadcastLeaderboard(
                sessionCode,
                new LeaderboardMessage(
                        sessionCode,
                        "SYSTEM",
                        leaderboard,
                        "SCORE_UPDATE"
                )
        );

        // If ASYNC: immediately send next question to this participant
        if (session.getMode() == QuizMode.ASYNC) {
            scheduler.schedule(() -> sendNextQuestionToParticipant(participantId, sessionCode, currentQuestionNumber + 1),
                    2, TimeUnit.SECONDS);
        } else {
            // SYNC: host controls progression; do not auto-send next question
            log.debug("Participant {} answered in SYNC mode - waiting for host to advance question", participantId);
        }
    }



    // =================== PARTICIPANT JOIN ===================
    @Transactional
    public SessionResponse joinSession(String sessionCode, String nickname, String userId, String avatarId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new NotFoundException("Session not found for code: " + sessionCode));

        if (!(session.getStatus() == Status.WAITING || session.getStatus() == Status.IN_PROGRESS))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not joinable now");

        if (participantRepository.existsBySessionIdAndNickname(session.getId(), nickname))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname taken");

        Avatar avatar = avatarRepository.findById (Long.valueOf(avatarId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avatar with ID: " + avatarId + " does not exist"));

        Participant participant = new Participant();
        participant.setNickname(nickname);
        participant.setSession(session);
        participant.setIsActive(true);
        participant.setIsConnected(true);
        participant.setTotalScore(0);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setAvatar(avatar);


        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            participant.setUser(user);
        }
        participantRepository.save(participant);

        session.setTotalParticipants(participantRepository.countBySessionIdAndIsActiveTrue(session.getId()));
        quizSessionRepository.save(session);

        leaderboardService.updateParticipantScore(session.getId(), participant.getId(), nickname, 0);

        log.info("Participant {} joined session {} with avatar {}", nickname, sessionCode, avatarId);

        // If session is in progress AND ASYNC: send first question to this joining participant
        if (session.getStatus() == Status.IN_PROGRESS && session.getMode() == QuizMode.ASYNC) {
            scheduler.schedule(() -> sendNextQuestionToParticipant(participant.getId(), sessionCode, 1), 1, TimeUnit.SECONDS);
        }

        // Broadcast participants
        List<ParticipantResponse> participants = participantRepository.findBySessionIdAndIsActiveTrue(session.getId())
                .stream().map(participantMapper::toParticipantResponse)
                .collect(Collectors.toList());

        webSocketService.broadcastParticipantUpdate(session.getSessionCode(),
                new ParticipantMessage(
                        session.getSessionCode(),
                        "SYSTEM",
                        participants,
                        session.getTotalParticipants(),
                        "PARTICIPANT_JOINED"
                )
        );

        return quizSessionMapper.toSessionResponse(session);
    }

    // =================== END SESSION ===================
    @Override
    @Transactional
    public SessionResponse endSession(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new NotFoundException("Session not found for code: " + sessionCode));

        if (session.getStatus() == Status.ENDED) return null;

        session.setStatus(Status.ENDED);
        session.setEndTime(LocalDateTime.now());
        quizSessionRepository.save(session);

        // Mark session inactive
        String activeKey = SESSION_ACTIVE_PREFIX + session.getId();
        redisTemplate.delete(activeKey);

        leaderboardService.finalizeSessionLeaderboard(session.getId());

        log.info("Session {} ended", sessionCode);

        webSocketService.broadcastGameState(
                session.getSessionCode(),
                new GameStateMessage(
                        session.getSessionCode(),
                        session.getHostName(),
                        Status.ENDED,
                        "SESSION_ENDED",
                        session.getCurrentQuestion(),
                        session.getTotalQuestions(),
                        null,
                        "Quiz ended! Thanks for playing!"
                )
        );

        webSocketService.broadcastLeaderboard(
                session.getSessionCode(),
                new LeaderboardMessage(
                        session.getSessionCode(),
                        "SYSTEM",
                        leaderboardService.getPodium(session.getSessionCode()),
                        "FINAL_RESULTS"
                )
        );

        cleanupSessionKeys(sessionCode, session.getId());

        return quizSessionMapper.toSessionResponse(session);
    }

    private void cleanupSessionKeys(String sessionCode, String sessionId) {
        try {
            // Clean up participant progress
            List<Participant> participants = participantRepository.findBySessionId(sessionId);
            for (Participant participant : participants) {
                String progressKey = PARTICIPANT_PROGRESS_PREFIX + participant.getId();
                redisTemplate.delete(progressKey);

                // Clean up question start times (SCAN would be preferable in prod)
                Set<String> startTimeKeys = redisTemplate.keys(QUESTION_START_TIME_PREFIX + participant.getId() + ":*");
                if (startTimeKeys != null && !startTimeKeys.isEmpty()) {
                    redisTemplate.delete(startTimeKeys);
                }
            }

            // Clean up session questions
            String questionsKey = SESSION_QUESTIONS_PREFIX + sessionId;
            redisTemplate.delete(questionsKey);

            // Clean up session active flag
            String activeKey = SESSION_ACTIVE_PREFIX + sessionId;
            redisTemplate.delete(activeKey);

        } catch (Exception e) {
            log.error("Error cleaning up Redis keys for session {}", sessionCode, e);
        }
    }

    private String generateUniqueSessionCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // =================== SYNC ADVANCEMENT ===================
    @Override
    @Transactional
    public Question advanceToNextQuestion(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new NotFoundException("Session not found for code: " + sessionCode));

        if (session.getMode() == QuizMode.ASYNC) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Async mode - participants advance individually");
        }

        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in progress.");
        }

        // Advance session-level question counter
        int nextQuestionNum = session.getCurrentQuestion() + 1;
        session.setCurrentQuestion(nextQuestionNum);
        quizSessionRepository.save(session);

        // Retrieve question ID from cached list
        String questionsKey = SESSION_QUESTIONS_PREFIX + session.getId();
        String questionId = redisTemplate.opsForList().index(questionsKey, nextQuestionNum - 1);

        if (questionId == null) {

            endSession(sessionCode);
            return null;
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        Integer timeLimit = question.getTimeLimit() != null ? question.getTimeLimit() : defaultTimeLimit;

        QuestionMessage qMsg = new QuestionMessage(
                session.getSessionCode(),
                session.getHostName(),
                question,
                nextQuestionNum,
                session.getTotalQuestions(),
                timeLimit,
                "NEXT_QUESTION"
        );

        // Broadcast to everyone
        webSocketService.broadcastQuestion(session.getSessionCode(), qMsg);

        // ✅ FIX: Update all participants' progress to the new question in SYNC mode
        // This ensures they can answer the new question even if they didn't answer the previous one
        List<Participant> participants = participantRepository.findBySessionId(session.getId());
        for (Participant participant : participants) {
            // Update participant progress to current question
            String progressKey = PARTICIPANT_PROGRESS_PREFIX + participant.getId();
            redisTemplate.opsForValue().set(progressKey, String.valueOf(nextQuestionNum));
            redisTemplate.expire(progressKey, java.time.Duration.ofHours(24));
            
            // Set question start time for this participant
            String startTimeKey = QUESTION_START_TIME_PREFIX + participant.getId() + ":" + questionId;
            redisTemplate.opsForValue().set(startTimeKey, String.valueOf(System.currentTimeMillis()));
            redisTemplate.expire(startTimeKey, java.time.Duration.ofHours(1));
            
            // Schedule timeout handler
            scheduler.schedule(() -> handleQuestionTimeout(participant.getId(), questionId, sessionCode, nextQuestionNum),
                    timeLimit + 2, TimeUnit.SECONDS);
        }
        log.info("Updated progress and scheduled timeout for {} participants in SYNC mode session {}", 
                participants.size(), sessionCode);

        return question;
    }

    @Override
    public Question getCurrentQuestion(String sessionCode) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Use participant-specific questions or advanceToNextQuestion for SYNC mode");
    }

    @Override
    public void pauseSession(String sessionCode) {
        log.info("Pause session requested for: {}", sessionCode);
    }

    @Override
    public boolean canJoinSession(String sessionCode) {
        try {
            QuizSession session = quizSessionRepository.findBySessionCode(sessionCode).orElse(null);
            return session != null && (session.getStatus() == Status.WAITING || session.getStatus() == Status.IN_PROGRESS);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<QuizSession> getActiveSession() {
        return quizSessionRepository.findByStatusIn(Arrays.asList(Status.WAITING, Status.IN_PROGRESS));
    }

    @Override
    public List<QuizSession> getSessions(String hostId) {
        return quizSessionRepository.findByHostIdOrderByCreatedAtDesc(hostId);
    }

    @Override
    public Optional<QuizSession> getSessionByCode(String sessionCode) {
        return quizSessionRepository.findBySessionCode(sessionCode);
    }

    @Override
    public List<QuizSession> getCurrentUserQuizSession(Jwt accessToken) {
        String hostId = accessToken.getSubject();
        return quizSessionRepository.findByHostIdOrderByCreatedAtDesc(hostId);
    }

    @Override
    public SessionResponse setAllowJoinInProgress(String sessionId, boolean allow) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
        session.setAllowJoinInProgress(allow);
        quizSessionRepository.save(session);
        return quizSessionMapper.toSessionResponse(session);
    }

    @Override
    public SessionResponse toSessionResponse(QuizSession quizSession) {
        return quizSessionMapper.toSessionResponse(quizSession);
    }
}
