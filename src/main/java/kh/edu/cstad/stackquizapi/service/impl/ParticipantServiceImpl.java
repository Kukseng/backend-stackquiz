package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import kh.edu.cstad.stackquizapi.util.QuizMode;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final ParticipantMapper participantMapper;
    private final AvatarRepository avatarRepository;
    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;
    private final WebSocketService webSocketService;


    private final kh.edu.cstad.stackquizapi.service.impl.QuizSessionServiceImpl quizSessionServiceImpl;
    private final kh.edu.cstad.stackquizapi.service.RealTimeRankingService realTimeRankingService;

    // --- Helper to broadcast leaderboard ---
    private void broadcastLeaderboardToSession(String sessionId) {
        LeaderboardResponse leaderboard = leaderboardService.getRealTimeLeaderboard(
                new LeaderboardRequest(sessionId, 50, 0, false, null)
        );
        webSocketService.broadcastLeaderboard(
                sessionId,
                new LeaderboardMessage(
                        sessionId,
                        "SYSTEM",
                        leaderboard,
                        "SCORE_UPDATE"
                )
        );
    }

    @Override
    public ParticipantResponse joinSession(JoinSessionRequest request) {
        QuizSession session = quizSessionRepository.findBySessionCode(request.quizCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found with code: " + request.quizCode()
                ));

        if (session.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot join session. Session has ended.");
        }

        if (participantRepository.existsBySessionIdAndNickname(session.getId(), request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nickname '" + request.nickname() + "' is already taken in this session");
        }

        Avatar avatar = avatarRepository.findById(request.avatarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avatar with ID: " + request.avatarId() + " does not exist"));

        Participant participant = participantMapper.toParticipant(request);

        ParticipantResponse response = getParticipantResponse(session, avatar, participant);

        // Broadcast participant list so host sees players
        broadcastParticipants(session);

        // Broadcast leaderboard
        broadcastLeaderboardToSession(session.getId());

        // Broadcast current session state (lobby/in-progress/ended)
        GameStateMessage currentState = new GameStateMessage(
                session.getSessionCode(),
                session.getHostName(),
                session.getStatus(),
                session.getStatus() == Status.WAITING ? "SESSION_LOBBY"
                        : session.getStatus() == Status.IN_PROGRESS ? "QUESTION_STARTED"
                        : "SESSION_ENDED",
                session.getCurrentQuestion(),
                session.getTotalQuestions(),
                null,
                session.getStatus() == Status.WAITING
                        ? "Lobby opened! Get ready..."
                        : session.getStatus() == Status.IN_PROGRESS
                        ? "Quiz in progress..."
                        : "Quiz session has ended!"
        );
        webSocketService.broadcastGameState(session.getSessionCode(), currentState);


        if (session.getStatus() == Status.IN_PROGRESS) {
            if (session.getMode() == QuizMode.SYNC) {

                if (session.getCurrentQuestion() != null && session.getCurrentQuestion() > 0) {
                    List<Question> questions = session.getQuiz().getQuestions().stream()
                            .sorted(Comparator.comparingInt(Question::getQuestionOrder)).toList();
                    int questionIndex = session.getCurrentQuestion() - 1;
                    if (questionIndex >= 0 && questionIndex < questions.size()) {
                        Question currentQuestion = questions.get(questionIndex);
                        if (currentQuestion != null) {
                            QuestionMessage qMsg = new QuestionMessage(
                                    session.getSessionCode(),
                                    session.getHostName(),
                                    currentQuestion,
                                    session.getCurrentQuestion(),
                                    session.getTotalQuestions(),
                                    currentQuestion.getTimeLimit() != null ? currentQuestion.getTimeLimit().intValue() : 30,
                                    "START_QUESTION"
                            );

                            webSocketService.broadcastQuestion(session.getSessionCode(), qMsg);
                        }
                    } else {
                        log.warn("Current question index {} out of bounds for session {}", questionIndex, session.getSessionCode());
                    }
                }
            } else {

                try {

                    quizSessionServiceImpl.sendNextQuestionToParticipant(response.id(), session.getSessionCode(), 1);
                    log.info("Sent first question to late joiner {} in async session {}", response.nickname(), session.getSessionCode());
                } catch (Exception e) {
                    log.error("Failed to send first question to late joiner {}: {}", response.nickname(), e.getMessage(), e);
                }
            }
        }

        return response;
    }

    @Override
    public ParticipantResponse joinSessionAsAuthenticatedUser(Jwt accessToken, JoinSessionRequest request) {
        String userId = accessToken.getSubject();

        QuizSession session = quizSessionRepository.findBySessionCode(request.quizCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found with code: " + request.quizCode()
                ));

        if (session.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot join session. Session has ended.");
        }

        if (participantRepository.existsBySessionIdAndNickname(session.getId(), request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nickname '" + request.nickname() + "' is already taken in this session");
        }

        Avatar avatar = avatarRepository.findById(request.avatarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avatar with ID: " + request.avatarId() + " does not exist"));

        Participant participant = participantMapper.toParticipant(request);

        userRepository.findById(userId).ifPresent(participant::setUser);

        ParticipantResponse response = getParticipantResponse(session, avatar, participant);

        // Broadcast leaderboard
        broadcastLeaderboardToSession(session.getId());

        // If session already running, same behavior as guest join:
        if (session.getStatus() == Status.IN_PROGRESS && session.getMode() == QuizMode.ASYNC) {
            try {
                quizSessionServiceImpl.sendNextQuestionToParticipant(response.id(), session.getSessionCode(), 1);
            } catch (Exception e) {
                log.error("Failed to send first question to authenticated late joiner {}: {}", response.nickname(), e.getMessage(), e);
            }
        } else if (session.getStatus() == Status.IN_PROGRESS && session.getMode() == QuizMode.SYNC) {
            // SYNC handled elsewhere (we broadcast global question)
        }

        return response;
    }

    private void broadcastParticipants(QuizSession session) {
        List<ParticipantResponse> participants = participantRepository
                .findBySessionIdAndIsActiveTrue(session.getId())
                .stream()
                .map(participantMapper::toParticipantResponse)
                .collect(Collectors.toList());

        webSocketService.broadcastParticipantUpdate(
                session.getSessionCode(),
                new ParticipantMessage(
                        session.getSessionCode(),
                        "SYSTEM",
                        participants,
                        participants.size(),
                        "PARTICIPANT_JOINED"
                )
        );
    }

    private ParticipantResponse getParticipantResponse(QuizSession session, Avatar avatar, Participant participant) {
        participant.setSession(session);
        participant.setAvatar(avatar);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setTotalScore(0);
        participant.setIsActive(true);
        participant.setIsConnected(true);

        Participant savedParticipant = participantRepository.save(participant);

        int currentCount = participantRepository.countBySessionIdAndIsActiveTrue(session.getId());
        session.setTotalParticipants(currentCount);
        quizSessionRepository.save(session);

        leaderboardService.updateParticipantScore(
                session.getId(),
                savedParticipant.getId(),
                savedParticipant.getNickname(),
                0
        );

        // ✅ ENHANCED: Initialize participant in ranking system
        realTimeRankingService.updateParticipantScoreAndRanking(
                session.getId(),
                savedParticipant.getId(),
                savedParticipant.getNickname(),
                0,
                false,
                0
        );

        return participantMapper.toParticipantResponse(savedParticipant);
    }

    @Override
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {

        Participant participant = participantRepository.findByIdAndIsActiveTrue(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question not found"));

        if (!question.getQuiz().getId().equals(participant.getSession().getQuiz().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Question does not belong to this session's quiz");
        }

        Option selectedAnswer = optionRepository.findById(request.optionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Answer option not found"));

        if (!selectedAnswer.getQuestion().getId().equals(question.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selected answer does not belong to the specified question");
        }

        boolean alreadyAnswered = participantAnswerRepository
                .existsByParticipantIdAndQuestionId(participant.getId(), question.getId());

        if (alreadyAnswered) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You have already answered this question");
        }

        boolean isCorrect = selectedAnswer.getIsCorrected();
        int pointsEarned = calculatePoints(isCorrect, Math.toIntExact(request.timeTaken()), question.getTimeLimit());

        ParticipantAnswer answer = new ParticipantAnswer();
        answer.setParticipant(participant);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);
        answer.setAnsweredAt(LocalDateTime.now());
        answer.setTimeTaken(Math.toIntExact(request.timeTaken()));
        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);

        ParticipantAnswer savedAnswer = participantAnswerRepository.save(answer);

        int newTotalScore = participant.getTotalScore() + pointsEarned;
        participant.setTotalScore(newTotalScore);
        participantRepository.save(participant);

        // ✅ ENHANCED: Use RealTimeRankingService for comprehensive real-time updates
        realTimeRankingService.updateParticipantScoreAndRanking(
                participant.getSession().getId(),
                participant.getId(),
                participant.getNickname(),
                newTotalScore,
                isCorrect,
                pointsEarned
        );

        // Send answer feedback with ranking information
        realTimeRankingService.sendAnswerFeedback(
                participant.getSession().getId(),
                participant.getId(),
                question.getId(),
                isCorrect,
                pointsEarned,
                Math.toIntExact(request.timeTaken())
        );


        if (participant.getSession().getMode() == QuizMode.ASYNC) {
            try {


                long answeredCount = participantAnswerRepository.countByParticipantId(participant.getId());

                int nextQuestionNumber = (int) answeredCount + 1;

                quizSessionServiceImpl.sendNextQuestionToParticipant(
                        participant.getId(),
                        participant.getSession().getSessionCode(),
                        nextQuestionNumber
                );
            } catch (Exception e) {
                log.error("Failed to send next question to participant {} in async mode: {}",
                        participant.getNickname(), e.getMessage(), e);
            }
        }

        return SubmitAnswerResponse.builder()
                .answerId(savedAnswer.getId())
                .participantId(participant.getId())
                .questionId(question.getId())
                .selectedAnswerId(selectedAnswer.getId())
                .isCorrect(isCorrect)
                .pointsEarned(pointsEarned)
                .timeTaken(Math.toIntExact(request.timeTaken()))
                .answeredAt(savedAnswer.getAnsweredAt())
                .newTotalScore(newTotalScore)
                .build();
    }

    @Override
    public List<ParticipantResponse> getSessionParticipants(String quizCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(quizCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session not found with code: " + quizCode));

        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(session.getId());
        return participants.stream()
                .map(participantMapper::toParticipantResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void leaveSession(String participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        participant.setIsActive(false);
        participantRepository.save(participant);


        broadcastLeaderboardToSession(participant.getSession().getId());
    }

    @Override
    public Optional<Participant> getParticipantById(String participantId) {
        return participantRepository.findById(participantId);
    }

    @Override
    public boolean isNicknameAvailable(String sessionId, String nickname) {
        return !participantRepository.existsBySessionIdAndNickname(sessionId, nickname);
    }

    @Override
    public boolean canJoinSession(String sessionCode) {
        Optional<QuizSession> sessionOpt = quizSessionRepository.findBySessionCode(sessionCode);
        if (sessionOpt.isEmpty()) {
            return false;
        }
        QuizSession session = sessionOpt.get();
        return session.getStatus() == Status.WAITING || session.getStatus() == Status.IN_PROGRESS;
    }

    private int calculatePoints(boolean isCorrect, Integer timeTaken, Integer timeLimit) {
        if (!isCorrect) {
            return 0;
        }
        int basePoints = 1000;
        if (timeTaken == null || timeLimit == null) {
            return basePoints;
        }
        double timeRatio = (double) timeTaken / timeLimit;
        double speedBonus = Math.max(0, 1 - timeRatio);
        int bonusPoints = (int) (speedBonus * 500);
        return basePoints + bonusPoints;
    }
}
