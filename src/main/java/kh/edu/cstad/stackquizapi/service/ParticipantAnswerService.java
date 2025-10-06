package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.AnswerSummaryResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionStatisticsResponse;

import java.util.List;

public interface ParticipantAnswerService {

    /**
     * Submit a single answer for a participant
     */
    ParticipantAnswerResponse submitAnswer(SubmitAnswerRequest request);

    /**
     * Submit multiple answers at once (bulk submission)
     */
    List<ParticipantAnswerResponse> submitBulkAnswers(BulkAnswerRequest request);

    /**
     * Get all answers for a specific participant
     */
    List<ParticipantAnswerResponse> getParticipantAnswers(String participantId);

    /**
     * Update an existing answer (if allowed)
     */
    ParticipantAnswerResponse updateAnswer(String answerId, SubmitAnswerRequest request);

    /**
     * Delete a participant's answer
     */
    void deleteAnswer(String answerId);

    /**
     * Check if participant has already answered a question
     */
    boolean hasAnswered(String participantId, String questionId);

    /**
     * Get participant's answer for a specific question
     */
    ParticipantAnswerResponse getParticipantQuestionAnswer(String participantId, String questionId);

    /**
     * Calculate participant's total score
     */
    int calculateParticipantTotalScore(String participantId);

}
