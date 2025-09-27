package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.GameStateMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.QuestionMessage;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
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
        QuizSession getSessionByCode = quizSessionRepository.findBySessionCode(request.quizCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found with code: " + request.quizCode()
                ));

        if (getSessionByCode.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot join session. Session has ended.");
        }

        if (participantRepository.existsBySessionIdAndNickname(getSessionByCode.getId(), request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nickname '" + request.nickname() + "' is already taken in this session");
        }

        Avatar avatar = avatarRepository.findById(request.avatarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avatar with ID: " + request.avatarId() + " does not exist"));

        Participant participant = participantMapper.toParticipant(request);

        ParticipantResponse response = getParticipantResponse(getSessionByCode, avatar, participant);

        // Broadcast updated leaderboard
        broadcastLeaderboardToSession(getSessionByCode.getId());

        // Broadcast current session state
        GameStateMessage currentState = new GameStateMessage(
                getSessionByCode.getSessionCode(),
                getSessionByCode.getHostName(),
                getSessionByCode.getStatus(),
                getSessionByCode.getStatus() == Status.WAITING ? "SESSION_LOBBY"
                        : getSessionByCode.getStatus() == Status.IN_PROGRESS ? "QUESTION_STARTED"
                        : "SESSION_ENDED",
                getSessionByCode.getCurrentQuestion(),
                getSessionByCode.getTotalQuestions(),
                null,
                getSessionByCode.getStatus() == Status.WAITING
                        ? "Lobby opened! Get ready..."
                        : getSessionByCode.getStatus() == Status.IN_PROGRESS
                        ? "Quiz in progress..."
                        : "Quiz session has ended!"
        );
        webSocketService.broadcastGameState(getSessionByCode.getSessionCode(), currentState);

        if (getSessionByCode.getStatus() == Status.IN_PROGRESS && getSessionByCode.getCurrentQuestion() > 0) {
            List<Question> questions = getSessionByCode.getQuiz().getQuestions().stream()
                    .sorted(Comparator.comparingInt(Question::getQuestionOrder)).toList();
            int questionIndex = getSessionByCode.getCurrentQuestion() - 1;
            if (questionIndex >= 0 && questionIndex < questions.size()) {
                Question currentQuestion = questions.get(questionIndex);
                if (currentQuestion != null) {
                    QuestionMessage qMsg = new QuestionMessage(
                            getSessionByCode.getSessionCode(),
                            getSessionByCode.getHostName(),
                            currentQuestion,
                            getSessionByCode.getCurrentQuestion(),
                            getSessionByCode.getTotalQuestions(),
                            currentQuestion.getTimeLimit(),
                            "START_QUESTION"
                    );
                    webSocketService.broadcastQuestion(getSessionByCode.getSessionCode(), qMsg);
                } else {
                    log.warn("No current question found for late joiner on session {}", getSessionByCode.getSessionCode());
                }
            } else {
                log.warn("Current question index {} is out of bounds (questions.size={} session={})",
                        questionIndex, questions.size(), getSessionByCode.getSessionCode());
            }
        }

        return response;
    }



    @Override
    public ParticipantResponse joinSessionAsAuthenticatedUser(Jwt accessToken, JoinSessionRequest request) {
        String userId = accessToken.getSubject();

        QuizSession getSessionByCode = quizSessionRepository.findBySessionCode(request.quizCode())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found with code: " + request.quizCode()
                ));

        if (getSessionByCode.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot join session. Session has ended.");
        }

        if (participantRepository.existsBySessionIdAndNickname(getSessionByCode.getId(), request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nickname '" + request.nickname() + "' is already taken in this session");
        }

        Avatar avatar = avatarRepository.findById(request.avatarId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Avatar with ID: " + request.avatarId() + " does not exist"));

        Participant participant = participantMapper.toParticipant(request);

        userRepository.findById(userId).ifPresent(participant::setUser);

        ParticipantResponse response = getParticipantResponse(getSessionByCode, avatar, participant);

        // Now broadcast updated leaderboard!
        broadcastLeaderboardToSession(getSessionByCode.getId());

        return response;
    }

    private ParticipantResponse getParticipantResponse(QuizSession getSessionByCode, Avatar avatar, Participant participant) {
        participant.setSession(getSessionByCode);
        participant.setAvatar(avatar);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setTotalScore(0);
        participant.setIsActive(true);
        participant.setIsConnected(true);

        Participant savedParticipant = participantRepository.save(participant);

        int currentCount = participantRepository.countBySessionIdAndIsActiveTrue(getSessionByCode.getId());
        getSessionByCode.setTotalParticipants(currentCount);
        quizSessionRepository.save(getSessionByCode);

        leaderboardService.updateParticipantScore(
                getSessionByCode.getId(),
                savedParticipant.getId(),
                savedParticipant.getNickname(),
                0
        );

        return participantMapper.toParticipantResponse(savedParticipant);
    }

    @Override
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {

        Participant participant = participantRepository.findByIdAndIsActiveTrue(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        if (participant.getSession().getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Session is not in progress");
        }

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
        int pointsEarned = calculatePoints(isCorrect, request.timeTaken(), question.getTimeLimit());

        ParticipantAnswer answer = new ParticipantAnswer();
        answer.setParticipant(participant);
        answer.setQuestion(question);
        answer.setSelectedAnswer(selectedAnswer);
        answer.setAnsweredAt(LocalDateTime.now());
        answer.setTimeTaken(request.timeTaken());
        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);

        ParticipantAnswer savedAnswer = participantAnswerRepository.save(answer);

        int newTotalScore = participant.getTotalScore() + pointsEarned;
        participant.setTotalScore(newTotalScore);
        participantRepository.save(participant);

        leaderboardService.updateParticipantScore(
                participant.getSession().getId(),
                participant.getId(),
                participant.getNickname(),
                newTotalScore
        );

        // Now broadcast updated leaderboard!
        broadcastLeaderboardToSession(participant.getSession().getId());

        return SubmitAnswerResponse.builder()
                .answerId(savedAnswer.getId())
                .participantId(participant.getId())
                .questionId(question.getId())
                .selectedAnswerId(selectedAnswer.getId())
                .isCorrect(isCorrect)
                .pointsEarned(pointsEarned)
                .timeTaken(request.timeTaken())
                .answeredAt(savedAnswer.getAnsweredAt())
                .newTotalScore(newTotalScore)
                .build();
    }

    @Override
    public List<ParticipantResponse> getSessionParticipants(String sessionId) {
        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
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

        // Optionally, broadcast updated leaderboard on leave as well:
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
