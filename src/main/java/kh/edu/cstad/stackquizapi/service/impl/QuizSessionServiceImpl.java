package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.domain.User;
import kh.edu.cstad.stackquizapi.dto.request.SessionCreateRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionResponse;
import kh.edu.cstad.stackquizapi.mapper.QuizSessionMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.QuizSessionService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private final LeaderboardService leaderboardService;

    @Override
    public SessionResponse createSession(SessionCreateRequest request) {
        User user = userRepository.findById(request.hostId())
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

        log.info("Created quiz session with ID: {}, Code: {}, Participants: {}", savedSession.getId(), savedSession.getSessionCode(), savedSession.getTotalParticipants());

        return quizSessionMapper.toSessionResponse(savedSession);
    }

    @Override
    public SessionResponse startSession(String sessionId) {
        QuizSession quizSession = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));

        if (quizSession.getStatus() != Status.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in waiting status.");
        }

        quizSession.setStatus(Status.IN_PROGRESS);
        quizSession.setStartTime(LocalDateTime.now());

        QuizSession updatedSession = quizSessionRepository.save(quizSession);
        log.info("Started session with ID: {}", sessionId);
        return quizSessionMapper.toSessionResponse(updatedSession);
    }

    @Override
    public Question advanceToNextQuestion(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));

        if (session.getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in progress status.");
        }

        List<Question> questions = session.getQuiz().getQuestions().stream()
                .sorted((q1, q2) -> q1.getQuestionOrder().compareTo(q2.getQuestionOrder()))
                .toList();

        int currentQuestionIndex = session.getCurrentQuestion();

        if (currentQuestionIndex >= questions.size()) {
            session.setStatus(Status.ENDED);
            session.setEndTime(LocalDateTime.now());
            quizSessionRepository.save(session);
            leaderboardService.finalizeSessionLeaderboard(sessionId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No more questions in this quiz. Session has been ended.");
        }

        Question currentQuestion = questions.get(currentQuestionIndex);

        session.setCurrentQuestion(currentQuestionIndex + 1);
        quizSessionRepository.save(session);

        log.info("Advanced to question {} in session {}", currentQuestionIndex + 1, sessionId);
        return currentQuestion;
    }

    @Override
    public SessionResponse endSession(String sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with id: " + sessionId));

        if (session.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is already ended");
        }

        session.setStatus(Status.ENDED);
        session.setEndTime(LocalDateTime.now());

        QuizSession updatedSession = quizSessionRepository.save(session);
        leaderboardService.finalizeSessionLeaderboard(sessionId);

        log.info("Ended session with ID: {}", sessionId);
        return quizSessionMapper.toSessionResponse(updatedSession);
    }

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


    public SessionResponse joinSession(String sessionCode, String nickname, String userId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found with code: " + sessionCode));

        if (session.getStatus() != Status.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session is not in waiting status.");
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

        log.info("Participant {} joined session {} (ID: {})", nickname, sessionCode, session.getId());

        return quizSessionMapper.toSessionResponse(session);
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