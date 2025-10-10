package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.transaction.Transactional;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.AnswerSubmissionMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.ParticipantAnswerService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantAnswerServiceImpl implements ParticipantAnswerService {

    private final ParticipantAnswerRepository participantAnswerRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final LeaderboardService leaderboardService;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public ParticipantAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        log.info("Submitting answer for participant {} on question {}", request.participantId(), request.questionId());

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> {
                    sendWsError(request, "Participant not found");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found");
                });

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> {
                    sendWsError(request, "Question not found");
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
                });

        if (hasAnswered(request.participantId(), request.questionId())) {
            sendWsError(request, "You have already answered this question.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Participant has already answered this question");
        }

        ParticipantAnswer answer = createAnswer(request, participant, question);
        scoreAnswer(answer, question);
        ParticipantAnswer savedAnswer = participantAnswerRepository.save(answer);

        updateParticipantTotalScore(participant);

        // Update leaderboard in real time
        leaderboardService.updateParticipantScore(
                participant.getSession().getId(),
                participant.getId(),
                participant.getNickname(),
                participant.getTotalScore()
        );

        // Broadcast result only to this participant
        ParticipantAnswerResponse response = mapToResponse(savedAnswer);

        webSocketService.sendToParticipant(
                participant.getNickname(), // Used to identify recipient
                participant.getSession().getSessionCode(),
                new AnswerSubmissionMessage(
                        participant.getSession().getSessionCode(),
                        "SYSTEM",
                        participant.getNickname(),
                        participant.getNickname(),
                        question.getId(),
                        answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getId() : null,
                        request.timeTaken().longValue()
                )
        );


        // Broadcast leaderboard update to all (after every answer)
        webSocketService.broadcastLeaderboard(
                participant.getSession().getSessionCode(),
                new LeaderboardMessage(
                        participant.getSession().getSessionCode(),
                        "SYSTEM",
                        leaderboardService.getRealTimeLeaderboard(
                                // Supply session code and reasonable leaderboard page size
                                new kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest(
                                        participant.getSession().getSessionCode(), 10, 0, false, null)
                        ),
                        "SCORE_UPDATE"
                )
        );

        log.info("Answer submitted successfully: {}", savedAnswer.getId());
        return response;
    }

    @Override
    @Transactional
    public List<ParticipantAnswerResponse> submitBulkAnswers(BulkAnswerRequest request) {
        log.info("Submitting bulk answers for participant {} in session {}",
                request.participantId(), request.sessionId());

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

        List<ParticipantAnswerResponse> responses = request.answers().stream()
                .map(singleAnswer -> {
                    SubmitAnswerRequest submitRequest = new SubmitAnswerRequest(
                            request.participantId(),
                            singleAnswer.questionId(),
                            singleAnswer.optionId(),
                            singleAnswer.answerText(),
                            singleAnswer.timeTaken(),
                            request.sessionId()
                    );
                    return submitAnswer(submitRequest);
                })
                .collect(Collectors.toList());

        webSocketService.broadcastLeaderboard(
                participant.getSession().getSessionCode(),
                new LeaderboardMessage(
                        participant.getSession().getSessionCode(),
                        "SYSTEM",
                        leaderboardService.getRealTimeLeaderboard(
                                new kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest(
                                        participant.getSession().getSessionCode(), 10, 0, false, null)
                        ),
                        "SCORE_UPDATE"
                )
        );

        log.info("Bulk submission completed: {} answers processed", responses.size());
        return responses;
    }

    @Override
    public List<ParticipantAnswerResponse> getParticipantAnswers(String participantId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantIdOrderByAnsweredAtAsc(participantId);

        return answers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipantAnswerResponse updateAnswer(String answerId, SubmitAnswerRequest request) {
        ParticipantAnswer answer = participantAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));
        if (request.optionId() != null) {
            Option option = optionRepository.findById(request.optionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option not found"));
            answer.setSelectedAnswer(option);
        }

        if (request.answerText() != null) {
            answer.setAnswerText(request.answerText());
        }

        answer.setTimeTaken(Math.toIntExact(request.timeTaken()));
        answer.setAnsweredAt(LocalDateTime.now());
        scoreAnswer(answer, answer.getQuestion());
        ParticipantAnswer updatedAnswer = participantAnswerRepository.save(answer);

        updateParticipantTotalScore(answer.getParticipant());
        return mapToResponse(updatedAnswer);
    }

    @Override
    @Transactional
    public void deleteAnswer(String answerId) {
        ParticipantAnswer answer = participantAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));

        Participant participant = answer.getParticipant();
        participantAnswerRepository.delete(answer);

        updateParticipantTotalScore(participant);

        // After deletion, update leaderboard in real time
        leaderboardService.updateParticipantScore(
                participant.getSession().getId(),
                participant.getId(),
                participant.getNickname(),
                participant.getTotalScore()
        );
        webSocketService.broadcastLeaderboard(
                participant.getSession().getSessionCode(),
                new LeaderboardMessage(
                        participant.getSession().getSessionCode(),
                        "SYSTEM",
                        leaderboardService.getRealTimeLeaderboard(
                                new kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest(
                                        participant.getSession().getSessionCode(), 10, 0, false, null)
                        ),
                        "SCORE_UPDATE"
                )
        );

        log.info("Answer deleted: {}", answerId);
    }

    @Override
    public boolean hasAnswered(String participantId, String questionId) {
        return participantAnswerRepository.existsByParticipantIdAndQuestionId(participantId, questionId);
    }

    @Override
    public ParticipantAnswerResponse getParticipantQuestionAnswer(String participantId, String questionId) {
        ParticipantAnswer answer = participantAnswerRepository
                .findByParticipantIdAndQuestionId(participantId, questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));
        return mapToResponse(answer);
    }

    @Override
    public int calculateParticipantTotalScore(String participantId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantIdOrderByAnsweredAtAsc(participantId);

        return answers.stream()
                .mapToInt(ParticipantAnswer::getPointsEarned)
                .sum();
    }

    private ParticipantAnswer createAnswer(SubmitAnswerRequest request, Participant participant, Question question) {
        ParticipantAnswer answer = new ParticipantAnswer();
        answer.setParticipant(participant);
        answer.setQuestion(question);
        answer.setTimeTaken(Math.toIntExact(request.timeTaken()));
        answer.setAnsweredAt(LocalDateTime.now());

        if (request.optionId() != null) {
            Option option = optionRepository.findById(request.optionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option not found"));
            answer.setSelectedAnswer(option);
        }

        if (request.answerText() != null && !request.answerText().trim().isEmpty()) {
            answer.setAnswerText(request.answerText().trim());
        }
        return answer;
    }

    private void scoreAnswer(ParticipantAnswer answer, Question question) {
        boolean isCorrect = false;
        int pointsEarned = 0;

        switch (question.getType()) {
            case MCQ, TF -> {
                if (answer.getSelectedAnswer() != null) {
                    isCorrect = answer.getSelectedAnswer().getIsCorrected();
                    if (isCorrect) {
                        int basePoints = question.getPoints();
                        if (answer.getTimeTaken() != null && question.getTimeLimit() != null) {
                            double timeRatio = (double) answer.getTimeTaken() / question.getTimeLimit();
                            double speedBonus = Math.max(0, 1 - timeRatio);
                            int bonusPoints = (int) (speedBonus * (question.getPoints() / 2));
                            pointsEarned = basePoints + bonusPoints;
                        }
                    }

                }
            }
            case FILL_THE_BLANK -> {
                if (answer.getAnswerText() != null && !answer.getAnswerText().trim().isEmpty()) {
                    isCorrect = checkFillInTheBlankAnswer(answer.getAnswerText(), question);
                    if (isCorrect) {
                        int basePoints = question.getPoints();
                        if (answer.getTimeTaken() != null && question.getTimeLimit() != null) {
                            double timeRatio = (double) answer.getTimeTaken() / question.getTimeLimit();
                            double speedBonus = Math.max(0, 1 - timeRatio);
                            int bonusPoints = (int) (speedBonus * (question.getPoints() / 2));
                            pointsEarned = basePoints + bonusPoints;
                        }
                    }

                }
            }
        }
        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);
    }

    private boolean checkFillInTheBlankAnswer(String answerText, Question question) {
        // Implement your fill-in-the-blank validation logic here
        // For now, this is a placeholder that always returns false
        return false;
    }

    @Transactional
    public void updateParticipantTotalScore(Participant participant) {
        int totalScore = calculateParticipantTotalScore(participant.getId());
        participant.setTotalScore(totalScore);
        participantRepository.save(participant);
    }

    private ParticipantAnswerResponse mapToResponse(ParticipantAnswer answer) {
        return ParticipantAnswerResponse.builder()
                .answerId(answer.getId())
                .participantId(answer.getParticipant().getId())
                .participantNickname(answer.getParticipant().getNickname())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getText())
                .optionId(answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getId() : null)
                .optionText(answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getOptionText() : null)
                .answerText(answer.getAnswerText())
                .isCorrect(answer.getIsCorrect())
                .timeTaken(answer.getTimeTaken())
                .pointsEarned(answer.getPointsEarned())
                .answeredAt(answer.getAnsweredAt())
                .sessionId(answer.getParticipant().getSession().getId())
                .build();
    }

    // Helper to send error via websocket for REST error flows
    private void sendWsError(SubmitAnswerRequest request, String errorMessage) {
        try {
            Participant participant = participantRepository.findById(request.participantId()).orElse(null);
            if (participant != null) {
                webSocketService.sendErrorToParticipant(
                        participant.getNickname(),
                        participant.getSession().getSessionCode(),
                        errorMessage
                );
            }
        } catch (Exception ignored) { }
    }
}
