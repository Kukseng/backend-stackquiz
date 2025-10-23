package kh.edu.cstad.stackquizapi.service;

import kh.edu.cstad.stackquizapi.dto.response.QuizAnalyticsResponse;

/**
 * Service for tracking and retrieving quiz analytics
 * Tracks: total sessions hosted, total participants, accuracy, etc.
 *
 * @author Phou Kukseng
 * @since 1.0
 */
public interface QuizAnalyticsService {
    
    /**
     * Get analytics for a specific quiz
     * Shows "Played by 5K students" and "100 times hosted"
     */
    QuizAnalyticsResponse getQuizAnalytics(String quizId);
    
    /**
     * Record that a new session was created for this quiz
     * Increments totalSessionsHosted
     */
    void recordSessionCreated(String quizId);
    
    /**
     * Record participants joining a session
     * Updates totalParticipants and averageParticipantsPerSession
     */
    void recordParticipantsJoined(String quizId, int participantCount);
    
    /**
     * Record that a session was completed
     * Increments totalCompletions
     */
    void recordSessionCompleted(String quizId);
    
    /**
     * Update question statistics after a session ends
     * Updates totalQuestionsAnswered, totalCorrectAnswers, overallAccuracyRate
     */
    void updateQuestionStatistics(String quizId, long questionsAnswered, long correctAnswers);
    
    /**
     * Initialize analytics for a new quiz
     */
    void initializeAnalytics(String quizId);
}

