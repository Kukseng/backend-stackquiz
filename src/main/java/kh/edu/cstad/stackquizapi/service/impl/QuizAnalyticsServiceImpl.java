package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.QuizAnalytics;
import kh.edu.cstad.stackquizapi.dto.response.QuizAnalyticsResponse;
import kh.edu.cstad.stackquizapi.repository.QuizAnalyticsRepository;
import kh.edu.cstad.stackquizapi.service.QuizAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAnalyticsServiceImpl implements QuizAnalyticsService {

    private final QuizAnalyticsRepository quizAnalyticsRepository;

    @Override
    @Transactional(readOnly = true)
    public QuizAnalyticsResponse getQuizAnalytics(String quizId) {
        QuizAnalytics analytics = quizAnalyticsRepository.findByQuizId(quizId)
                .orElseGet(() -> {
                    // If no analytics exist, return empty analytics
                    QuizAnalytics empty = new QuizAnalytics();
                    empty.setQuizId(quizId);
                    return empty;
                });

        return QuizAnalyticsResponse.from(
                analytics.getQuizId(),
                analytics.getTotalSessionsHosted(),
                analytics.getTotalParticipants(),
                analytics.getTotalCompletions(),
                analytics.getAverageParticipantsPerSession(),
                analytics.getPeakParticipants(),
                analytics.getTotalQuestionsAnswered(),
                analytics.getTotalCorrectAnswers(),
                analytics.getOverallAccuracyRate(),
                analytics.getFirstPlayedAt(),
                analytics.getLastPlayedAt(),
                analytics.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void recordSessionCreated(String quizId) {
        QuizAnalytics analytics = getOrCreateAnalytics(quizId);

        analytics.incrementSessionCount();

        quizAnalyticsRepository.save(analytics);

        log.info("Session created for quiz {}. Total sessions: {}",
                quizId, analytics.getTotalSessionsHosted());
    }

    @Override
    @Transactional
    public void recordParticipantsJoined(String quizId, int participantCount) {
        QuizAnalytics analytics = getOrCreateAnalytics(quizId);

        analytics.addParticipants(participantCount);

        quizAnalyticsRepository.save(analytics);

        log.info("{} participants joined quiz {}. Total participants: {}, Avg per session: {}",
                participantCount, quizId, analytics.getTotalParticipants(),
                String.format("%.1f", analytics.getAverageParticipantsPerSession()));
    }

    @Override
    @Transactional
    public void recordSessionCompleted(String quizId) {
        QuizAnalytics analytics = getOrCreateAnalytics(quizId);

        analytics.incrementCompletionCount();
        analytics.setLastPlayedAt(LocalDateTime.now());

        quizAnalyticsRepository.save(analytics);

        log.info("Session completed for quiz {}. Total completions: {}",
                quizId, analytics.getTotalCompletions());
    }

    @Override
    @Transactional
    public void updateQuestionStatistics(String quizId, long questionsAnswered, long correctAnswers) {
        QuizAnalytics analytics = getOrCreateAnalytics(quizId);

        analytics.updateQuestionStats(questionsAnswered, correctAnswers);

        quizAnalyticsRepository.save(analytics);

        log.info("Updated question stats for quiz {}. Accuracy: {:.1f}%",
                quizId, analytics.getOverallAccuracyRate());
    }

    @Override
    @Transactional
    public void initializeAnalytics(String quizId) {
        if (!quizAnalyticsRepository.existsByQuizId(quizId)) {
            QuizAnalytics analytics = new QuizAnalytics();
            analytics.setQuizId(quizId);
            analytics.setFirstPlayedAt(LocalDateTime.now());

            quizAnalyticsRepository.save(analytics);

            log.info("📊 Initialized analytics for quiz {}", quizId);
        }
    }

    private QuizAnalytics getOrCreateAnalytics(String quizId) {
        return quizAnalyticsRepository.findByQuizId(quizId)
                .orElseGet(() -> {
                    QuizAnalytics analytics = new QuizAnalytics();
                    analytics.setQuizId(quizId);
                    analytics.setFirstPlayedAt(LocalDateTime.now());
                    return analytics;
                });
    }
}

