package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;

import java.util.List;

/**
 * Extended question service providing additional functionality
 * for retrieving questions within the context of a quiz session.
 *
 * This interface builds on {@link QuestionService} and offers
 * methods for navigating through questions, accessing them by index,
 * and retrieving session-specific question details.
 *
 * @author Pech Rattanakmony
 */
public interface QuestionServiceExtended extends QuestionService {

    /**
     * Retrieves the next question in the given quiz session.
     *
     * @param sessionId the unique identifier of the quiz session
     * @return a {@link QuestionResponse} representing the next question
     */
    QuestionResponse getNextQuestionForSession(String sessionId);

    /**
     * Retrieves all questions associated with a specific quiz session.
     *
     * @param sessionId the unique identifier of the quiz session
     * @return a list of {@link QuestionResponse} objects containing all session questions
     */
    List<QuestionResponse> getQuestionsForSession(String sessionId);

    /**
     * Retrieves a question by its index in the given quiz session.
     *
     * @param sessionId the unique identifier of the quiz session
     * @param questionIndex the index of the question (0-based or 1-based depending on implementation)
     * @return a {@link QuestionResponse} representing the question at the specified index
     */
    QuestionResponse getQuestionByIndexForSession(String sessionId, Integer questionIndex);

    /**
     * Retrieves the current active question for the given quiz session.
     *
     * @param sessionId the unique identifier of the quiz session
     * @return a {@link QuestionResponse} representing the current question
     */
    QuestionResponse getCurrentQuestionForSession(String sessionId);
}

