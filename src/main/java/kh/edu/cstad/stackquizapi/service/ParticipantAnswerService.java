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
     * Get participant's answers for a specific session
     */
    List<ParticipantAnswerResponse> getParticipantSessionAnswers(String participantId, String sessionId);

    /**
     * Get all answers for a specific question across all participants
     */
    List<ParticipantAnswerResponse> getQuestionAnswers(String questionId);

    /**
     * Get all answers for a specific question in a session
     */
    List<ParticipantAnswerResponse> getQuestionSessionAnswers(String questionId, String sessionId);

    /**
     * Get answer summary for a participant (statistics)
     */
    AnswerSummaryResponse getParticipantAnswerSummary(String participantId);

    /**
     * Get question statistics (how many answered correctly, etc.)
     */
    QuestionStatisticsResponse getQuestionStatistics(String questionId, String sessionId);

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
     * Validate and score an answer
     */
    ParticipantAnswerResponse validateAndScoreAnswer(SubmitAnswerRequest request);

    /**
     * Get all answers for a session (for reporting)
     */
    List<ParticipantAnswerResponse> getSessionAnswers(String sessionId);

    /**
     * Calculate participant's total score
     */
    int calculateParticipantTotalScore(String participantId);

}
