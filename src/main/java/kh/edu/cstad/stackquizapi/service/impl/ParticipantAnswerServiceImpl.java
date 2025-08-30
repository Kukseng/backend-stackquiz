package kh.edu.cstad.stackquizapi.service.impl;

import jakarta.transaction.Transactional;
import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.AnswerSummaryResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionStatisticsResponse;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.ParticipantAnswerService;
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

    @Override
    @Transactional
    public ParticipantAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        log.info("Submitting answer for participant {} on question {}",
                request.participantId(), request.questionId());

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));


        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question not found"));


        if (hasAnswered(request.participantId(), request.questionId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Participant has already answered this question");
        }


        ParticipantAnswer answer = createAnswer(request, participant, question);


        scoreAnswer(answer, question);


        ParticipantAnswer savedAnswer = participantAnswerRepository.save(answer);


        updateParticipantTotalScore(participant);

        log.info("Answer submitted successfully: {}", savedAnswer.getId());

        return mapToResponse(savedAnswer);
    }

    @Override
    @Transactional
    public List<ParticipantAnswerResponse> submitBulkAnswers(BulkAnswerRequest request) {
        log.info("Submitting bulk answers for participant {} in session {}",
                request.participantId(), request.sessionId());

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

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
    public List<ParticipantAnswerResponse> getParticipantSessionAnswers(String participantId, String sessionId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantIdAndParticipantSessionIdOrderByAnsweredAtAsc(participantId, sessionId);

        return answers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipantAnswerResponse> getQuestionAnswers(String questionId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByQuestionIdOrderByAnsweredAtAsc(questionId);

        return answers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipantAnswerResponse> getQuestionSessionAnswers(String questionId, String sessionId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByQuestionIdAndParticipantSessionId(questionId, sessionId);

        return answers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AnswerSummaryResponse getParticipantAnswerSummary(String participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantIdOrderByAnsweredAtAsc(participantId);

        int totalAnswered = answers.size();
        long correctAnswers = answers.stream().mapToLong(a -> a.getIsCorrect() ? 1L : 0L).sum();
        int incorrectAnswers = totalAnswered - (int) correctAnswers;
        double accuracyRate = totalAnswered > 0 ? (double) correctAnswers / totalAnswered * 100 : 0.0;
        int totalScore = answers.stream().mapToInt(ParticipantAnswer::getPointsEarned).sum();
        double averageResponseTime = answers.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        LocalDateTime lastAnswered = answers.stream()
                .map(ParticipantAnswer::getAnsweredAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return AnswerSummaryResponse.builder()
                .participantId(participantId)
                .participantNickname(participant.getNickname())
                .sessionId(participant.getSession().getId())
                .totalAnswered(totalAnswered)
                .correctAnswers((int) correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .accuracyRate(Math.round(accuracyRate * 100.0) / 100.0)
                .totalScore(totalScore)
                .averageResponseTime(Math.round(averageResponseTime * 100.0) / 100.0)
                .lastAnswered(lastAnswered)
                .build();
    }

    @Override
    public QuestionStatisticsResponse getQuestionStatistics(String questionId, String sessionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question not found"));

        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByQuestionIdAndParticipantSessionId(questionId, sessionId);

        int totalResponses = answers.size();
        long correctAnswers = answers.stream().mapToLong(a -> a.getIsCorrect() ? 1L : 0L).sum();
        int incorrectAnswers = totalResponses - (int) correctAnswers;
        double accuracyRate = totalResponses > 0 ? (double) correctAnswers / totalResponses * 100 : 0.0;
        double averageResponseTime = answers.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);
        double averagePointsEarned = answers.stream()
                .mapToInt(ParticipantAnswer::getPointsEarned)
                .average()
                .orElse(0.0);

        return QuestionStatisticsResponse.builder()
                .questionId(questionId)
                .questionText(question.getText())
                .sessionId(sessionId)
                .totalResponses(totalResponses)
                .correctAnswers((int) correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .accuracyRate(Math.round(accuracyRate * 100.0) / 100.0)
                .averageResponseTime(Math.round(averageResponseTime * 100.0) / 100.0)
                .maxPoints(question.getPoints())
                .averagePointsEarned(Math.round(averagePointsEarned * 100.0) / 100.0)
                .build();
    }

    @Override
    @Transactional
    public ParticipantAnswerResponse updateAnswer(String answerId, SubmitAnswerRequest request) {
        ParticipantAnswer answer = participantAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Answer not found"));


        if (request.optionId() != null) {
            Option option = optionRepository.findById(request.optionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Option not found"));
            answer.setSelectedAnswer(option);
        }

        if (request.answerText() != null) {
            answer.setAnswerText(request.answerText());
        }

        answer.setTimeTaken(request.timeTaken());
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Answer not found"));

        Participant participant = answer.getParticipant();
        participantAnswerRepository.delete(answer);

        updateParticipantTotalScore(participant);

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Answer not found"));

        return mapToResponse(answer);
    }

    @Override
    public ParticipantAnswerResponse validateAndScoreAnswer(SubmitAnswerRequest request) {

        Participant participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Participant not found"));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Question not found"));

        ParticipantAnswer answer = createAnswer(request, participant, question);
        scoreAnswer(answer, question);

        return mapToResponse(answer);
    }

    @Override
    public List<ParticipantAnswerResponse> getSessionAnswers(String sessionId) {
        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantSessionId(sessionId);

        return answers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        answer.setTimeTaken(request.timeTaken());
        answer.setAnsweredAt(LocalDateTime.now());


        if (request.optionId() != null) {
            Option option = optionRepository.findById(request.optionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Option not found"));
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
                        pointsEarned = question.getPoints();
                    }
                }
            }
            case FILL_THE_BLANK -> {
                if (answer.getAnswerText() != null && !answer.getAnswerText().trim().isEmpty()) {

                    isCorrect = checkFillInTheBlankAnswer(answer.getAnswerText(), question);
                    if (isCorrect) {
                        pointsEarned = question.getPoints();
                    }
                }
            }
        }

        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);
    }

    private boolean checkFillInTheBlankAnswer(String answerText, Question question) {
        // Implement your fill-in-the-blank validation logic here
        // This could involve checking against stored correct answers,
        // fuzzy matching, or other validation rules

        // For now, this is a placeholder that always returns false
        // You should implement proper validation based on your requirements
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
                .optionId(answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getOptionText() : null)
                .optionText(answer.getSelectedAnswer() != null ? answer.getSelectedAnswer().getOptionText() : null)
                .answerText(answer.getAnswerText())
                .isCorrect(answer.getIsCorrect())
                .timeTaken(answer.getTimeTaken())
                .pointsEarned(answer.getPointsEarned())
                .answeredAt(answer.getAnsweredAt())
                .sessionId(answer.getParticipant().getSession().getId())
                .build();
    }
}