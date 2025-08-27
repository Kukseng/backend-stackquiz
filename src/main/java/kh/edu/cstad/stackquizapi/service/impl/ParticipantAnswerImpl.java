package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.Question;
import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.AnswerSummaryResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionStatisticsResponse;
import kh.edu.cstad.stackquizapi.repository.QuestionRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.ParticipantAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantAnswerImpl implements ParticipantAnswerService {

    @Override
    public ParticipantAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        return null;
    }

    @Override
    public List<ParticipantAnswerResponse> submitBulkAnswers(BulkAnswerRequest request) {
        return List.of();
    }

    @Override
    public List<ParticipantAnswerResponse> getParticipantAnswers(String participantId) {
        return List.of();
    }

    @Override
    public List<ParticipantAnswerResponse> getParticipantSessionAnswers(String participantId, String sessionId) {
        return List.of();
    }

    @Override
    public List<ParticipantAnswerResponse> getQuestionAnswers(String questionId) {
        return List.of();
    }

    @Override
    public List<ParticipantAnswerResponse> getQuestionSessionAnswers(String questionId, String sessionId) {
        return List.of();
    }

    @Override
    public AnswerSummaryResponse getParticipantAnswerSummary(String participantId) {
        return null;
    }

    @Override
    public QuestionStatisticsResponse getQuestionStatistics(String questionId, String sessionId) {
        return null;
    }

    @Override
    public ParticipantAnswerResponse updateAnswer(String answerId, SubmitAnswerRequest request) {
        return null;
    }

    @Override
    public void deleteAnswer(String answerId) {

    }

    @Override
    public boolean hasAnswered(String participantId, String questionId) {
        return false;
    }

    @Override
    public ParticipantAnswerResponse getParticipantQuestionAnswer(String participantId, String questionId) {
        return null;
    }

    @Override
    public ParticipantAnswerResponse validateAndScoreAnswer(SubmitAnswerRequest request) {
        return null;
    }

    @Override
    public List<ParticipantAnswerResponse> getSessionAnswers(String sessionId) {
        return List.of();
    }

    @Override
    public int calculateParticipantTotalScore(String participantId) {
        return 0;
    }
}
