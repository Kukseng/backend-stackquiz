package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.CreateFeedbackRequest;
import kh.edu.cstad.stackquizapi.dto.request.CreateQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.FolkQuizRequest;
import kh.edu.cstad.stackquizapi.dto.request.QuizUpdateRequest;
import kh.edu.cstad.stackquizapi.dto.request.SuspendQuizRequest;
import kh.edu.cstad.stackquizapi.dto.response.FavoriteQuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.CreateFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuestionResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizFeedbackResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizResponse;
import kh.edu.cstad.stackquizapi.dto.response.QuizSuspensionResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for managing all quiz-related operations.
 * Handles quiz creation, updates, favorites, feedback, and moderation features.
 * Provides both user-specific and public-level functionalities such as
 * quiz discovery, suspension, and reporting.
 *
 * <p>This service acts as the core layer between the controller and
 * data persistence for quizzes, supporting both authenticated and
 * public interactions.</p>
 *
 * @author Pech Rattanakmony
 * @since 1.0
 */
public interface QuizService {

    /**
     * Create a new quiz and associate it with the authenticated user.
     *
     * @param createQuizRequest the request containing quiz details
     * @param jwt the user's authentication token
     * @return the created quiz details
     */
    QuizResponse createQuiz(CreateQuizRequest createQuizRequest, Jwt jwt);

    /**
     * Retrieve a quiz by its unique identifier.
     *
     * @param quizId the quiz ID
     * @return the quiz information if found
     */
    QuizResponse getQuizById(String quizId);

    /**
     * Retrieve all quizzes from the system.
     * Can be filtered by active status to only return visible or published quizzes.
     *
     * @param active filter by active status (true for active only)
     * @return list of quizzes matching the filter
     */
    List<QuizResponse> getAllQuiz(Boolean active);

    /**
     * Update quiz information such as title, description, or visibility.
     * Only the quiz owner or an authorized user may perform this operation.
     *
     * @param quizId the quiz ID
     * @param quizUpdateRequest the updated quiz information
     * @param accessToken the user's authentication token
     * @return the updated quiz response
     */
    QuizResponse updateQuiz(String quizId, QuizUpdateRequest quizUpdateRequest, Jwt accessToken);

    /**
     * Permanently delete a quiz from the system.
     * This action is restricted to quiz owners or administrators.
     *
     * @param quizId the quiz ID
     * @param accessToken the user's authentication token
     */
    void deleteQuiz(String quizId, Jwt accessToken);

    /**
     * Get all quizzes created by the currently authenticated user.
     *
     * @param accessToken the user's authentication token
     * @return list of user-created quizzes
     */
    List<QuizResponse> getQuizByUser(Jwt accessToken);

    /**
     * Suspend or reactivate a quiz for moderation or rule violations.
     *
     * @param quizId the quiz ID
     * @param request the suspension request
     * @param accessToken the user's authentication token
     * @return suspension result containing status and reason
     */
    QuizSuspensionResponse suspendQuiz(String quizId, SuspendQuizRequest request, Jwt accessToken);

    List<QuizSuspensionResponse> getSuspendedQuizzes(Jwt accessToken);

    /**
     * Add a quiz to the user's list of favorites.
     *
     * @param quizId the quiz ID
     * @param accessToken the user's authentication token
     * @return the added favorite quiz response
     */
    FavoriteQuizResponse atToFavorite(String quizId, Jwt accessToken);

    /**
     * Remove a quiz from the user's favorites.
     *
     * @param quizId the quiz ID
     * @param accessToken the user's authentication token
     */
    void removeFromFavorite(String quizId, Jwt accessToken);

    /**
     * Retrieve all quizzes marked as favorite by any user.
     *
     * @return list of all favorite quizzes
     */
    List<FavoriteQuizResponse> getFavoriteQuizzes();

    /**
     * Retrieve all favorite quizzes for the current authenticated user.
     *
     * @param accessToken the user's authentication token
     * @return list of favorite quizzes owned by the user
     */
    List<FavoriteQuizResponse> getCurrentUserFavoriteQuizzes(Jwt accessToken);

    /**
     * Create a forked version of an existing quiz for customization.
     * This allows users to duplicate and modify quizzes under their ownership.
     *
     * @param accessToken the user's authentication token
     * @param quizId the quiz ID to be forked
     * @param folkQuizRequest additional fork options
     * @return the newly created forked quiz response
     */
    QuizResponse folkQuiz(Jwt accessToken, String quizId, FolkQuizRequest folkQuizRequest);

    /**
     * Submit feedback for a specific quiz.
     * Feedback can include ratings, comments, and improvement suggestions.
     *
     * @param createFeedbackRequest the feedback content
     * @param quizId the quiz ID
     * @param accessToken the user's authentication token
     * @return feedback creation response
     */
    CreateFeedbackResponse giveFeedback(CreateFeedbackRequest createFeedbackRequest, String quizId, Jwt accessToken);

    /**
     * Retrieve all quiz feedback across the system (admin view).
     *
     * @return list of all quiz feedback responses
     */
    List<QuizFeedbackResponse> getAllFeedbacks();

    /**
     * Retrieve feedback submitted by the current user.
     *
     * @param accessToken the user's authentication token
     * @return list of feedbacks submitted by the user
     */
    List<QuizFeedbackResponse> getCurrentUserQuizFeedbacks(Jwt accessToken);

    /**
     * Retrieve all quizzes saved as drafts by the current user.
     * Drafted quizzes are not yet published or visible to others.
     *
     * @param accessToken the user's authentication token
     * @return list of drafted quizzes
     */
    List<QuizResponse> getDraftedQuizzes(Jwt accessToken);
}
