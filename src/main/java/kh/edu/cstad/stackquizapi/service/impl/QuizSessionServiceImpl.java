package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.mapper.QuizSessionMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl implements QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final QuizSessionMapper quizSessionMapper;
    private final ParticipantMapper participantMapper;
    private final LeaderboardService leaderboardService;
    private final WebSocketService webSocketService;;
    private final RedisTemplate<String, String> redisTemplate;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    private static final String ANSWERS_KEY_PREFIX = "quiz:answers:";
    private static final String TIMER_KEY_PREFIX = "quiz:timer:";

    private final boolean autoPlayEnabled = true;

    @Transactional
    public void showQuestionAndStartTimer(String sessionId, String questionId, int questionNumber, int totalQuestions, String hostName, Question question) {
        String answersKey = ANSWERS_KEY_PREFIX + sessionId + ":" + questionId;
        redisTemplate.delete(answersKey);

        int timeLimit = question.getTimeLimit() != null ? question.getTimeLimit() : 30;

        // Broadcast question via WebSocket
        QuestionMessage qMsg = new QuestionMessage(
                sessionId,
                hostName,
                question,
                questionNumber,
                totalQuestions,
                timeLimit,
                "START_QUESTION"
        );
        webSocketService.broadcastQuestion(sessionId, qMsg);

        // Arm timer for this question
        if (autoPlayEnabled) {
            String timerKey = TIMER_KEY_PREFIX + sessionId + ":" + questionId;
            redisTemplate.opsForValue().set(timerKey, "active", timeLimit, TimeUnit.SECONDS);

            scheduler.schedule(() -> {
                String status = redisTemplate.opsForValue().get(timerKey);
                if ("active".equals(status)) {
                    autoAdvanceToNextQuestion(sessionId);
                    redisTemplate.delete(timerKey);
                }
            }, timeLimit, TimeUnit.SECONDS);
        }
    }

    @Transactional
    public void trackAnswerAndAutoAdvance(String sessionId, String questionId, String participantId) {
        String answersKey = ANSWERS_KEY_PREFIX + sessionId + ":" + questionId;
        redisTemplate.opsForSet().add(answersKey, participantId);

        long answered = redisTemplate.opsForSet().size(answersKey);
        long participantCount = participantRepository.countBySessionIdAndIsActiveTrue(sessionId);

        if (autoPlayEnabled && answered >= participantCount) {
            String timerKey = TIMER_KEY_PREFIX + sessionId + ":" + questionId;
            redisTemplate.delete(timerKey); // stops timer
            autoAdvanceToNextQuestion(sessionId);
        }
    }

    // Synchronized to prevent concurrent triggers
    @Transactional
    public synchronized void autoAdvanceToNextQuestion(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (session.getStatus() != Status.IN_PROGRESS) return;

        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getQuestionOrder))
                .toList();

        int curr = session.getCurrentQuestion();
        if (curr >= questions.size()) {
            endSession(sessionId);
            return;
        }

        session.setCurrentQuestion(curr + 1);
        quizSessionRepository.save(session);

        Question nextQuestion = questions.get(curr);
        showQuestionAndStartTimer(
                sessionId, nextQuestion.getId(), curr + 1, questions.size(),
                session.getHostName(), nextQuestion
        );
    }

    @Override
    public SessionResponse endSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (session.getStatus() == Status.ENDED) return null;

        session.setStatus(Status.ENDED);
        session.setEndTime(LocalDateTime.now());
        quizSessionRepository.save(session);

        leaderboardService.finalizeSessionLeaderboard(sessionId);

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
                        "Quiz session has ended!"
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

        // Optionally cleanup keys
        redisTemplate.delete(ANSWERS_KEY_PREFIX + sessionId + ":*");
        redisTemplate.delete(TIMER_KEY_PREFIX + sessionId + ":*");
        return null;
    }

    @Override
    public SessionResponse createSession(SessionCreateRequest request, Jwt accessToken) {
        String hostId = accessToken.getSubject();
        User user = userRepository.findById(hostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

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

        QuizSession savedSession = quizSessionRepository.save(quizSession);
        leaderboardService.initializeSessionLeaderboard(savedSession.getId());

        log.info("Created quiz session with ID: {}, Code: {}, Participants: {}",
                savedSession.getId(), savedSession.getSessionCode(), savedSession.getTotalParticipants());

        // Start lobby (Kahoot-style)
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
                        "Lobby opened! Get ready..."
                )
        );

        return quizSessionMapper.toSessionResponse(savedSession);
    }

    @Override
    public SessionResponse startSession(String sessionCode) {
        QuizSession quizSession = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionCode));

        if (quizSession.getStatus() != Status.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in waiting status.");
        }

        quizSession.setStatus(Status.IN_PROGRESS);
        quizSession.setStartTime(LocalDateTime.now());
        QuizSession updatedSession = quizSessionRepository.save(quizSession);

        log.info("Started session with ID: {}", sessionCode);

        // Kahoot-style countdown (if needed, keep or remove)
        for (int i = 5; i > 0; i--) {
            webSocketService.broadcastGameState(
                    updatedSession.getSessionCode(),
                    new GameStateMessage(
                            updatedSession.getSessionCode(),
                            updatedSession.getHostName(),
                            Status.WAITING,
                            "COUNTDOWN",
                            null,
                            updatedSession.getTotalQuestions(),
                            (long) i,
                            "Starting in " + i + " seconds..."
                    )
            );
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }

        // Broadcast session started
        webSocketService.broadcastGameState(
                updatedSession.getSessionCode(),
                new GameStateMessage(
                        updatedSession.getSessionCode(),
                        updatedSession.getHostName(),
                        Status.IN_PROGRESS,
                        "SESSION_STARTED",
                        1,
                        updatedSession.getTotalQuestions(),
                        null,
                        "Quiz started! Good luck!"
                )
        );

        // Broadcast leaderboard immediately after SESSION_STARTED
        LeaderboardResponse leaderboard = leaderboardService.getRealTimeLeaderboard(
                new LeaderboardRequest(updatedSession.getSessionCode(), 10, 0, false, null)
        );
        webSocketService.broadcastLeaderboard(
                updatedSession.getSessionCode(),
                new LeaderboardMessage(
                        updatedSession.getSessionCode(),
                        "SYSTEM",
                        leaderboard,
                        "SCORE_UPDATE"
                )
        );

        return quizSessionMapper.toSessionResponse(updatedSession);
    }

    @Transactional
    public Question advanceToNextQuestion(String sessionId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));

        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in progress status.");
        }

        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getQuestionOrder))
                .toList();

        int currentIndex = session.getCurrentQuestion();
        if (currentIndex >= questions.size()) {
            // ...session end logic
        }

        // increment first!
        session.setCurrentQuestion(currentIndex + 1);
        quizSessionRepository.save(session);

        Question nextQuestion = questions.get(currentIndex); // get Qn for this turn

        // ...broadcast logic
        return nextQuestion;
    }




//    @Override
//    public SessionResponse endSession(String sessionId) {
//        QuizSession session = quizSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));
//        if (session.getStatus() == Status.ENDED) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is already ended");
//        }
//
//        session.setStatus(Status.ENDED);
//        session.setEndTime(LocalDateTime.now());
//        QuizSession updatedSession = quizSessionRepository.save(session);
//        leaderboardService.finalizeSessionLeaderboard(sessionId);
//
//        webSocketService.broadcastGameState(
//                session.getSessionCode(),
//                new GameStateMessage(
//                        session.getSessionCode(),
//                        session.getHostName(),
//                        Status.ENDED,
//                        "SESSION_ENDED",
//                        session.getCurrentQuestion(),
//                        session.getTotalQuestions(),
//                        null,
//                        "Quiz session has ended!"
//                )
//        );
//
//        webSocketService.broadcastLeaderboard(
//                session.getSessionCode(),
//                new LeaderboardMessage(
//                        session.getSessionCode(),
//                        "SYSTEM",
//                        leaderboardService.getPodium(session.getSessionCode()),
//                        "FINAL_RESULTS"
//                )
//        );
//
//        log.info("Ended session with ID: {}", sessionId);
//        return quizSessionMapper.toSessionResponse(updatedSession);
//    }

    @Override
    public Question getCurrentQuestion(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));

        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in progress status.");
        }

        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted((q1, q2) -> q1.getQuestionOrder().compareTo(q2.getQuestionOrder()))
                .toList();

        int currentQuestionIndex = session.getCurrentQuestion() - 1;
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No current question available.");
        }

        return questions.get(currentQuestionIndex);
    }

    @Override
    public boolean canJoinSession(String sessionCode) {
        Optional<QuizSession> sessionOpt = quizSessionRepository.findBySessionCode(sessionCode);
        boolean canJoin = sessionOpt.isPresent() && sessionOpt.get().getStatus() == Status.WAITING;
        log.info("Checked joinability for session code {}: {}", sessionCode, canJoin);
        return canJoin;
    }

    @Override
    public List<QuizSession> getActiveSession() {
        List<QuizSession> activeSessions = quizSessionRepository.findByStatusIn(List.of(Status.WAITING, Status.IN_PROGRESS));
        log.info("Retrieved {} active sessions", activeSessions.size());
        return activeSessions;
    }

    @Override
    public void pauseSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in progress and cannot be paused.");
        }
        session.setStatus(Status.PAUSED);
        quizSessionRepository.save(session);

        // Broadcast pause event to frontend
        webSocketService.broadcastGameState(
                session.getSessionCode(),
                new GameStateMessage(
                        session.getSessionCode(),
                        session.getHostName(),
                        Status.PAUSED,
                        "SESSION_PAUSED",
                        session.getCurrentQuestion(),
                        session.getTotalQuestions(),
                        null,
                        "Session paused by host."
                )
        );
    }


    @Override
    public List<QuizSession> getSessions(String hostId) {
        List<QuizSession> sessions = quizSessionRepository.findByHostIdOrderByCreatedAtDesc(hostId);
        log.info("Retrieved {} sessions for host {}", sessions.size(), hostId);
        return sessions;
    }

    @Override
    public Optional<QuizSession> getSessionByCode(String sessionCode) {
        Optional<QuizSession> session = quizSessionRepository.findBySessionCode(sessionCode);
        log.info("Looked up session by code {}: {}", sessionCode, session.isPresent() ? "Found" : "Not found");
        return session;
    }

    public SessionResponse setAllowJoinInProgress(String sessionId, boolean allow) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        session.setAllowJoinInProgress(allow);
        quizSessionRepository.save(session);
        // Optionally, broadcast state to all participants/host
        return quizSessionMapper.toSessionResponse(session);
    }


    // Add WebSocket broadcast for participant join
    public SessionResponse joinSession(String sessionCode, String nickname, String userId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with code: " + sessionCode));
        if (!(session.getStatus() == Status.WAITING ||
                (session.getStatus() == Status.IN_PROGRESS && Boolean.TRUE.equals(session.getAllowJoinInProgress())))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not joinable at this time.");
        }
        if (participantRepository.existsBySessionIdAndNickname(session.getId(), nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nickname " + nickname + " is already taken in this session.");
        }

        Participant participant = new Participant();
        participant.setNickname(nickname);
        participant.setSession(session);
        participant.setIsActive(true);
        participant.setIsConnected(true);
        participant.setTotalScore(0);
        participant.setJoinedAt(LocalDateTime.now());
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            participant.setUser(user);
        }
        participantRepository.save(participant);

        session.setTotalParticipants(participantRepository.countBySessionIdAndIsActiveTrue(session.getId()));
        quizSessionRepository.save(session);

        leaderboardService.updateParticipantScore(session.getId(), participant.getId(), nickname, 0);

        // WebSocket: broadcast participant update
        List<ParticipantResponse> participantResponses = participantRepository
                .findBySessionIdAndIsActiveTrue(session.getId())
                .stream()
                .map(participantMapper::toParticipantResponse)
                .collect(Collectors.toList());

        webSocketService.broadcastParticipantUpdate(
                session.getSessionCode(),
                new ParticipantMessage(
                        session.getSessionCode(),
                        "SYSTEM",
                        participantResponses,
                        session.getTotalParticipants(),
                        "PARTICIPANT_JOINED"
                )
        );

        log.info("Participant {} joined session {} (ID: {})", nickname, sessionCode, session.getId());
        return quizSessionMapper.toSessionResponse(session);
    }
    @Override
    public List<QuizSession> getCurrentUserQuizSession(Jwt accessToken) {
        return getSessions(accessToken.getSubject());
    }


    private String generateUniqueSessionCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (quizSessionRepository.findBySessionCode(code).isPresent());
        return code;
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 6)
                .toUpperCase();
    }
}
