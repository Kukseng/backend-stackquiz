package kh.edu.cstad.stackquizapi.controller;

import kh.edu.cstad.stackquizapi.dto.response.QuizAnalyticsResponse;
import kh.edu.cstad.stackquizapi.service.QuizAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for quiz analytics endpoints
 * Provides statistics like "Played by 5K students" and "100 times hosted"
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizAnalyticsController {

    private final QuizAnalyticsService quizAnalyticsService;

    /**
     * Get analytics for a specific quiz
     *
     * Example response:
     * {
     *   "totalSessionsHosted": 100,
     *   "totalParticipants": 5000,
     *   "participantsDisplay": "5.0K participants",
     *   "sessionsDisplay": "100 sessions",
     *   "accuracyDisplay": "85.5% accuracy"
     * }
     */
    @GetMapping("/{quizId}/analytics")
    public ResponseEntity<QuizAnalyticsResponse> getQuizAnalytics(@PathVariable String quizId) {
        QuizAnalyticsResponse analytics = quizAnalyticsService.getQuizAnalytics(quizId);
        return ResponseEntity.ok(analytics);
    }
}

