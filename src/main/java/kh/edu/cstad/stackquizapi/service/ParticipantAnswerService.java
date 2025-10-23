package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.BulkAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.request.SubmitAnswerRequest;
import kh.edu.cstad.stackquizapi.dto.response.AnswerSummaryResponse;
import kh.edu.cstad.stackquizapi.dto.response.ParticipantAnswerResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionStatisticsResponse;

import java.util.List;

/**
 * Service interface for managing participant answers and submissions.
 * <p>
 * Provides operations for submitting, updating, retrieving, and deleting
 * participant answers in both individual and bulk formats. It also supports
 * validation, scoring, and answer tracking functionalities.
 * </p>
 *
 * <p>Main features include:
 * <ul>
 *   <li>Single and bulk answer submission</li>
 *   <li>Answer management (update and delete)</li>
 *   <li>Score calculation and question completion tracking</li>
 *   <li>Participant-level answer retrieval</li>
 * </ul>
 * </p>
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface ParticipantAnswerService {

    /**
     * Submits a single answer for a participant.
     *
     * @param request the request containing participant ID, question ID, and answer data
     * @return the response containing the submitted answer details
     */
    ParticipantAnswerResponse submitAnswer(SubmitAnswerRequest request);

    /**
     * Submits multiple answers at once (bulk submission).
     *
     * @param request the bulk submission request containing a list of participant answers
     * @return a list of responses for each submitted answer
     */
    List<ParticipantAnswerResponse> submitBulkAnswers(BulkAnswerRequest request);

    /**
     * Retrieves all answers submitted by a specific participant.
     *
     * @param participantId the unique identifier of the participant
     * @return a list of participant answer responses
     */
    List<ParticipantAnswerResponse> getParticipantAnswers(String participantId);

    /**
     * Updates an existing answer if updates are allowed.
     *
     * @param answerId the unique identifier of the answer to update
     * @param request  the request containing updated answer information
     * @return the updated participant answer response
     */
    ParticipantAnswerResponse updateAnswer(String answerId, SubmitAnswerRequest request);

    /**
     * Deletes a participant’s answer from the system.
     *
     * @param answerId the unique identifier of the answer to delete
     */
    void deleteAnswer(String answerId);

    /**
     * Checks if a participant has already submitted an answer for a given question.
     *
     * @param participantId the unique identifier of the participant
     * @param questionId    the unique identifier of the question
     * @return {@code true} if the participant has already answered, otherwise {@code false}
     */
    boolean hasAnswered(String participantId, String questionId);

    /**
     * Retrieves a participant’s answer for a specific question.
     *
     * @param participantId the unique identifier of the participant
     * @param questionId    the unique identifier of the question
     * @return the response containing the participant’s answer details
     */
    ParticipantAnswerResponse getParticipantQuestionAnswer(String participantId, String questionId);

    /**
     * Calculates the total score accumulated by a participant across all questions.
     *
     * @param participantId the unique identifier of the participant
     * @return the participant’s total score as an integer value
     */
    int calculateParticipantTotalScore(String participantId);
}
