package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.mail.Session;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.JoinSessionRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantResponse;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.response.SubmitAnswerResponse;
import kh.edu.cstad.stackquizapi.mapper.ParticipantMapper;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantService;
import kh.edu.cstad.stackquizapi.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final ParticipantMapper participantMapper;

    private final LeaderboardService leaderboardService;

    @Override
    public ParticipantResponse joinSession(JoinSessionRequest request) {

        QuizSession getSessionByCode = quizSessionRepository.findBySessionCode(request.quizCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Session not found with code: " + request.quizCode()));

        if (getSessionByCode.getStatus() == Status.ENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot join session. Session has ended.");
        }

        if (participantRepository.existsBySessionIdAndNickname(getSessionByCode.getId(), request.nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nickname '" + request.nickname() + "' is already taken in this session");
        }

//         get session code
//        Optional<QuizSession> sessionOpt = getSessionByCode(sessionCode);
        // Join Quiz by SessionCode
        Participant participant = participantMapper.toParticipant(request);
        participant.setSession(getSessionByCode);

//        participant.setNickname(request.nickname());
        participant.setJoinedAt(LocalDateTime.now());
        participant.setTotalScore(0);
        participant.setIsActive(true);

        Participant savedParticipant = participantRepository.save(participant);



        int currentCount = participantRepository.countBySessionIdAndIsActiveTrue(getSessionByCode.getId());
        getSessionByCode.setTotalParticipants(currentCount);
        quizSessionRepository.save(getSessionByCode);

        // ADD PARTICIPANT TO REDIS LEADERBOARD
        leaderboardService.updateParticipantScore(
                getSessionByCode.getId(),
                savedParticipant.getId(),
                savedParticipant.getNickname(),
                0
        );

        return participantMapper.toParticipantResponse(savedParticipant);
    }

    // Add this method to your ParticipantServiceImpl class
    @Override
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));


        if (participant.getSession().getStatus() != Status.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Session is not in progress");
        }


        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question not found"));

        // Verify question belongs to the same quiz as the session
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

        // UPDATE REDIS LEADERBOARD WITH NEW SCORE
        leaderboardService.updateParticipantScore(
                participant.getSession().getId(),
                participant.getId(),
                participant.getNickname(),
                newTotalScore
        );


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

//    @Override
//    public LeaderboardResponse getLeaderboard(String sessionId) {
//        List<Participant> participants = participantRepository.findBySessionIdOrderByScoreDesc(sessionId);
//
//        List<LeaderboardResponse.LeaderboardEntry> entries = participants.stream()
//                .map(participant -> {
//                    LeaderboardResponse.LeaderboardEntry entry = new LeaderboardResponse.LeaderboardEntry();
//                    entry.setParticipantId(participant.getId());
//                    entry.setNickname(participant.getNickname());
//                    entry.setTotalScore(participant.getTotalScore());
//                    entry.setPosition(participants.indexOf(participant) + 1);
//                    return entry;
//                })
//                .collect(Collectors.toList());
//
//        LeaderboardResponse leaderboard = new LeaderboardResponse();
//        leaderboard.setSessionId(sessionId);
//        leaderboard.setEntries(entries);
//        leaderboard.setTotalParticipants(entries.size());
//
//        return leaderboard;
//    }

    @Override
    public void leaveSession(String participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        participant.setIsActive(false);
        participantRepository.save(participant);
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

        // Base points for correct answer
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