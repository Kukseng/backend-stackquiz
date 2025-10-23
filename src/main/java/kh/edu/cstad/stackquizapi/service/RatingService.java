package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.request.RatingRequest;
import kh.edu.cstad.stackquizapi.dto.response.RatingResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Service interface for managing quiz ratings.
 * <p>
 * Supports rating a quiz, retrieving ratings by quiz or user,
 * calculating average ratings, and deleting user-submitted ratings.
 * </p>
 *
 * author Pech Rattanakmony
 * @since 1.0
 */
public interface RatingService {

    /**
     * Submit a rating for a specific quiz.
     *
     * @param quizId the ID of the quiz to rate
     * @param ratingRequest the rating details (e.g., score, comment)
     * @param accessToken the user's JWT for authentication
     * @return the response containing the submitted rating
     */
    RatingResponse rateQuiz(String quizId, RatingRequest ratingRequest, Jwt accessToken);

    /**
     * Retrieve all ratings for a specific quiz.
     *
     * @param quizId the ID of the quiz
     * @return a list of rating responses for the quiz
     */
    List<RatingResponse> getRatingsByQuiz(String quizId);

    /**
     * Retrieve all ratings submitted by the currently authenticated user.
     *
     * @param accessToken the user's JWT
     * @return a list of rating responses submitted by the user
     */
    List<RatingResponse> getRatingsByUser(Jwt accessToken);

    /**
     * Calculate the average rating for a specific quiz.
     *
     * @param quizId the ID of the quiz
     * @return the average rating as a double
     */
    double getAverageRating(String quizId);

    /**
     * Delete the current user's rating for a specific quiz.
     *
     * @param quizId the ID of the quiz
     * @param accessToken the user's JWT
     */
    void deleteRating(String quizId, Jwt accessToken);
}

