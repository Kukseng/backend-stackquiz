package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.*;
import kh.edu.cstad.stackquizapi.dto.request.SessionReportRequest;
import kh.edu.cstad.stackquizapi.dto.response.SessionReportResponse;
import kh.edu.cstad.stackquizapi.repository.*;
import kh.edu.cstad.stackquizapi.service.SessionReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReportServiceImpl implements SessionReportService {

    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    @Override
    public SessionReportResponse generateSessionReport(String sessionCode, SessionReportRequest request) {
        log.info("Generating comprehensive session report for session: {}", sessionCode);

        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        // Build comprehensive report
        return SessionReportResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .sessionName(session.getSessionName())
                .hostName(session.getHostName())
                .quizTitle(session.getQuiz() != null ? session.getQuiz().getTitle() : "Unknown Quiz")
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .durationMinutes(calculateSessionDuration(session))
                .status(session.getStatus().toString())
                .statistics(getSessionStatistics(sessionCode))
                .questionAnalysis(getQuestionAnalysis(sessionCode))
                .participantReports(getAllParticipantReports(sessionCode))
                .insights(getPerformanceInsights(sessionCode))
                .build();
    }

    @Override
    public SessionReportResponse.SessionStatistics getSessionStatistics(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        List<Participant> participants = participantRepository.findBySessionId(session.getId());
        List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(session.getId());

        int totalParticipants = participants.size();
        int completedParticipants = (int) participants.stream()
                .filter(p -> isParticipantCompleted(p, session))
                .count();

        int totalQuestions = session.getTotalQuestions();
        double averageScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .average()
                .orElse(0.0);

        int totalAnswers = allAnswers.size();
        int correctAnswers = (int) allAnswers.stream()
                .filter(ParticipantAnswer::getIsCorrect)
                .count();
        int incorrectAnswers = totalAnswers - correctAnswers;
        int unansweredQuestions = (totalParticipants * totalQuestions) - totalAnswers;

        double averageAccuracy = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0.0;
        double averageResponseTime = allAnswers.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToDouble(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        double completionRate = totalParticipants > 0 ? (double) completedParticipants / totalParticipants * 100 : 0.0;
        double engagementRate = calculateEngagementRate(participants, session);

        return SessionReportResponse.SessionStatistics.builder()
                .totalParticipants(totalParticipants)
                .completedParticipants(completedParticipants)
                .totalQuestions(totalQuestions)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .averageAccuracy(Math.round(averageAccuracy * 100.0) / 100.0)
                .averageResponseTime(Math.round(averageResponseTime * 100.0) / 100.0)
                .totalAnswers(totalAnswers)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .unansweredQuestions(unansweredQuestions)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .engagementRate(Math.round(engagementRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<SessionReportResponse.QuestionAnalysis> getQuestionAnalysis(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        if (session.getQuiz() == null) {
            throw new RuntimeException("Quiz not found for session: " + sessionCode);
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByQuestionOrder(session.getQuiz().getId());
        List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(session.getId());
        int totalParticipants = participantRepository.countBySessionId(session.getId());

        return questions.stream()
                .map(question -> analyzeQuestion(question, allAnswers, totalParticipants))
                .collect(Collectors.toList());
    }

    @Override
    public Page<SessionReportResponse.ParticipantReport> getParticipantReports(
            String sessionCode, SessionReportRequest request) {

        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        // Create pageable with sorting
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                sort
        );

        Page<Participant> participantPage = new org.springframework.data.domain.PageImpl<>(participantRepository.findBySessionId(session.getId()), pageable, participantRepository.countBySessionId(session.getId()));

        return participantPage.map(participant -> generateParticipantReport(participant, session));
    }

    @Override
    public SessionReportResponse.ParticipantReport getParticipantReport(
            String sessionCode, String participantId) {

        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found: " + participantId));

        return generateParticipantReport(participant, session);
    }

    @Override
    public SessionReportResponse.PerformanceInsights getPerformanceInsights(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        List<Participant> participants = participantRepository.findBySessionId(session.getId());
        List<SessionReportResponse.QuestionAnalysis> questionAnalysis = getQuestionAnalysis(sessionCode);

        // Top performers (top 3 by score)
        List<String> topPerformers = participants.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()))
                .limit(3)
                .map(Participant::getNickname)
                .collect(Collectors.toList());

        // Most improved (based on performance trend)
        List<String> mostImproved = findMostImprovedParticipants(participants, session);

        // Struggling participants (bottom 20% by accuracy)
        List<String> struggling = findStrugglingParticipants(participants, session);

        // Question insights
        SessionReportResponse.QuestionAnalysis easiest = questionAnalysis.stream()
                .max(Comparator.comparing(SessionReportResponse.QuestionAnalysis::getAccuracyRate))
                .orElse(null);

        SessionReportResponse.QuestionAnalysis hardest = questionAnalysis.stream()
                .min(Comparator.comparing(SessionReportResponse.QuestionAnalysis::getAccuracyRate))
                .orElse(null);

        SessionReportResponse.QuestionAnalysis mostSkipped = questionAnalysis.stream()
                .max(Comparator.comparing(SessionReportResponse.QuestionAnalysis::getNoResponses))
                .orElse(null);

        SessionReportResponse.QuestionAnalysis fastest = questionAnalysis.stream()
                .min(Comparator.comparing(SessionReportResponse.QuestionAnalysis::getAverageResponseTime))
                .orElse(null);

        // Generate recommendations
        List<String> hostRecommendations = generateHostRecommendations(session, questionAnalysis, participants);
        List<String> contentRecommendations = generateContentRecommendations(questionAnalysis);
        Map<String, String> participantFeedback = generateParticipantFeedback(participants, session);

        return SessionReportResponse.PerformanceInsights.builder()
                .topPerformers(topPerformers)
                .mostImprovedParticipants(mostImproved)
                .strugglingParticipants(struggling)
                .easiestQuestion(easiest)
                .hardestQuestion(hardest)
                .mostSkippedQuestion(mostSkipped)
                .fastestAnsweredQuestion(fastest)
                .peakParticipationTime(findPeakParticipationTime(session))
                .averageSessionDuration(calculateAverageSessionDuration(participants, session))
                .dropoffRate(calculateDropoffRate(participants, session))
                .hostRecommendations(hostRecommendations)
                .contentRecommendations(contentRecommendations)
                .participantFeedback(participantFeedback)
                .build();
    }

    @Override
    public byte[] exportSessionReport(String sessionCode, SessionReportRequest request) {
        // Implementation for exporting to different formats (CSV, PDF, Excel)
        SessionReportResponse report = generateSessionReport(sessionCode, request);

        switch (request.getFormat()) {
            case CSV:
                return exportToCsv(report);
            case PDF:
                return exportToPdf(report);
            case EXCEL:
                return exportToExcel(report);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + request.getFormat());
        }
    }

    @Override
    public SessionReportResponse getLiveSessionReport(String sessionCode) {
        // Real-time report for ongoing sessions
        return generateSessionReport(sessionCode, SessionReportRequest.builder()
                .reportType(SessionReportRequest.ReportType.SUMMARY)
                .build());
    }

    @Override
    public List<SessionReportResponse> compareSessionReports(List<String> sessionCodes) {
        return sessionCodes.stream()
                .map(code -> generateSessionReport(code, SessionReportRequest.builder()
                        .reportType(SessionReportRequest.ReportType.SUMMARY)
                        .build()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionReportResponse.ParticipantAnswer> getQuestionAnswerHistory(
            String sessionCode, String questionId) {

        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByQuestionIdAndParticipantSessionId(question.getId(), session.getId());

        return answers.stream()
                .map(this::convertToParticipantAnswerDto)
                .collect(Collectors.toList());
    }

    // Helper methods

    private SessionReportResponse.QuestionAnalysis analyzeQuestion(
            Question question, List<ParticipantAnswer> allAnswers, int totalParticipants) {

        List<ParticipantAnswer> questionAnswers = allAnswers.stream()
                .filter(a -> a.getQuestion().getId().equals(question.getId()))
                .collect(Collectors.toList());

        int totalResponses = questionAnswers.size();
        int correctResponses = (int) questionAnswers.stream()
                .filter(ParticipantAnswer::getIsCorrect)
                .count();
        int incorrectResponses = totalResponses - correctResponses;
        int noResponses = totalParticipants - totalResponses;

        double accuracyRate = totalResponses > 0 ? (double) correctResponses / totalResponses * 100 : 0.0;
        double averageResponseTime = questionAnswers.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToDouble(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        String difficulty = determineDifficulty(accuracyRate);
        List<SessionReportResponse.OptionAnalysis> optionAnalysis = analyzeOptions(question, questionAnswers);

        return SessionReportResponse.QuestionAnalysis.builder()
                .questionId(question.getId())
                .questionText(question.getText())
                .questionNumber(question.getQuestionOrder())
                .questionType(question.getType().toString())
                .timeLimit(question.getTimeLimit())
                .totalResponses(totalResponses)
                .correctResponses(correctResponses)
                .incorrectResponses(incorrectResponses)
                .noResponses(noResponses)
                .accuracyRate(Math.round(accuracyRate * 100.0) / 100.0)
                .averageResponseTime(Math.round(averageResponseTime * 100.0) / 100.0)
                .difficulty(difficulty)
                .optionAnalysis(optionAnalysis)
                .correctOptionId(findCorrectOptionId(question))
                .explanation(null)
                .discriminationIndex(calculateDiscriminationIndex(questionAnswers))
                .commonMistakes(findCommonMistakes(questionAnswers, question))
                .build();
    }

    private SessionReportResponse.ParticipantReport generateParticipantReport(
            Participant participant, QuizSession session) {

        List<ParticipantAnswer> answers = participantAnswerRepository
                .findByParticipantIdOrderByAnsweredAt(participant.getId());

        int questionsAnswered = answers.size();
        int correctAnswers = (int) answers.stream()
                .filter(ParticipantAnswer::getIsCorrect)
                .count();
        int incorrectAnswers = questionsAnswered - correctAnswers;
        int skippedQuestions = session.getTotalQuestions() - questionsAnswered;

        double accuracyRate = questionsAnswered > 0 ? (double) correctAnswers / questionsAnswered * 100 : 0.0;
        double averageResponseTime = answers.stream()
                .filter(a -> a.getTimeTaken() != null)
                .mapToDouble(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        String completionStatus = determineCompletionStatus(participant, session);
        List<SessionReportResponse.ParticipantAnswer> answerDetails = answers.stream()
                .map(this::convertToParticipantAnswerDto)
                .collect(Collectors.toList());

        SessionReportResponse.PerformanceMetrics performance = analyzeParticipantPerformance(participant, answers);

        return SessionReportResponse.ParticipantReport.builder()
                .participantId(participant.getId())
                .nickname(participant.getNickname())
                .avatarId(participant.getAvatar() != null ? String.valueOf(participant.getAvatar().getId()) : null)
                .joinTime(participant.getJoinedAt())
                .lastActivity(findLastActivity(participant))
                .totalScore(participant.getTotalScore())
                .finalRank(0)
                .accuracyRate(Math.round(accuracyRate * 100.0) / 100.0)
                .averageResponseTime(Math.round(averageResponseTime * 100.0) / 100.0)
                .questionsAnswered(questionsAnswered)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .skippedQuestions(skippedQuestions)
                .completionStatus(completionStatus)
                .answers(answerDetails)
                .performance(performance)
                .longestCorrectStreak(calculateLongestCorrectStreak(answers))
                .longestIncorrectStreak(calculateLongestIncorrectStreak(answers))
                .strongTopics(identifyStrongTopics(answers))
                .weakTopics(identifyWeakTopics(answers))
                .build();
    }

    private SessionReportResponse.ParticipantAnswer convertToParticipantAnswerDto(ParticipantAnswer answer) {
        Question question = answer.getQuestion();
        Option selectedOption = answer.getSelectedAnswer();
        Option correctOption = findCorrectOption(question);

        return SessionReportResponse.ParticipantAnswer.builder()
                .questionId(question.getId())
                .questionNumber(question.getQuestionOrder())
                .questionText(question.getText())
                .selectedOptionId(selectedOption != null ? selectedOption.getId() : null)
                .selectedOptionText(selectedOption != null ? selectedOption.getOptionText() : "No answer")
                .correctOptionId(correctOption != null ? correctOption.getId() : null)
                .correctOptionText(correctOption != null ? correctOption.getOptionText() : null)
                .isCorrect(answer.getIsCorrect())
                .pointsEarned(answer.getPointsEarned())
                .maxPoints(question.getPoints())
                .responseTime(answer.getTimeTaken() != null ? answer.getTimeTaken().doubleValue() : null)
                .answeredAt(answer.getAnsweredAt())
                .answerStatus(determineAnswerStatus(answer))
                .explanation(null)
                .responseSpeed(categorizeResponseSpeed(answer.getTimeTaken(), question.getTimeLimit()))
                .wasGuessed(analyzeIfGuessed(answer))
                .attemptNumber(1) // Assuming single attempt for now
                .build();
    }

    // Additional helper methods for calculations and analysis

    private Long calculateSessionDuration(QuizSession session) {
        if (session.getStartTime() != null && session.getEndTime() != null) {
            return Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
        }
        return null;
    }

    private boolean isParticipantCompleted(Participant participant, QuizSession session) {
        int answeredQuestions = (int) participantAnswerRepository.countByParticipantId(participant.getId());
        return answeredQuestions >= session.getTotalQuestions() * 0.8; // 80% completion threshold
    }

    private double calculateEngagementRate(List<Participant> participants, QuizSession session) {
        if (participants.isEmpty()) return 0.0;

        long activeParticipants = participants.stream()
                .filter(p -> {
                    long answers = participantAnswerRepository.countByParticipantId(p.getId());
                    return answers > 0; // Has answered at least one question
                })
                .count();

        return (double) activeParticipants / participants.size() * 100;
    }

    private String determineDifficulty(double accuracyRate) {
        if (accuracyRate >= 80) return "Easy";
        if (accuracyRate >= 60) return "Medium";
        return "Hard";
    }

    private List<SessionReportResponse.OptionAnalysis> analyzeOptions(
            Question question, List<ParticipantAnswer> questionAnswers) {

        List<Option> options = optionRepository.findByQuestionIdOrderByOptionOrder(question.getId());

        return options.stream()
                .map(option -> {
                    long responseCount = questionAnswers.stream()
                            .filter(a -> a.getSelectedAnswer() != null &&
                                    a.getSelectedAnswer().getId().equals(option.getId()))
                            .count();

                    double responsePercentage = questionAnswers.size() > 0 ?
                            (double) responseCount / questionAnswers.size() * 100 : 0.0;

                    double avgResponseTime = questionAnswers.stream()
                            .filter(a -> a.getSelectedAnswer() != null &&
                                    a.getSelectedAnswer().getId().equals(option.getId()) &&
                                    a.getTimeTaken() != null)
                            .mapToDouble(ParticipantAnswer::getTimeTaken)
                            .average()
                            .orElse(0.0);

                    return SessionReportResponse.OptionAnalysis.builder()
                            .optionId(option.getId())
                            .optionText(option.getOptionText())
                            .isCorrect(option.getIsCorrected())
                            .responseCount((int) responseCount)
                            .responsePercentage(Math.round(responsePercentage * 100.0) / 100.0)
                            .averageResponseTime(Math.round(avgResponseTime * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String findCorrectOptionId(Question question) {
        return optionRepository.findByQuestionIdAndIsCorrected(question.getId(), true)
                .stream()
                .findFirst()
                .map(Option::getId)
                .orElse(null);
    }

    private Option findCorrectOption(Question question) {
        return optionRepository.findByQuestionIdAndIsCorrected(question.getId(), true)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private double calculateDiscriminationIndex(List<ParticipantAnswer> answers) {
        // Simplified discrimination index calculation
        // In a real implementation, this would compare high vs low performers
        return 0.5; // Placeholder
    }

    private List<String> findCommonMistakes(List<ParticipantAnswer> answers, Question question) {
        // Analyze incorrect answers to find common patterns
        Map<String, Long> incorrectAnswers = answers.stream()
                .filter(a -> !a.getIsCorrect() && a.getSelectedAnswer() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getSelectedAnswer().getOptionText(),
                        Collectors.counting()
                ));

        return incorrectAnswers.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String determineCompletionStatus(Participant participant, QuizSession session) {
        int answeredQuestions = (int) participantAnswerRepository.countByParticipantId(participant.getId());
        double completionRate = (double) answeredQuestions / session.getTotalQuestions();

        if (completionRate >= 0.9) return "COMPLETED";
        if (completionRate >= 0.5) return "PARTIAL";
        return "ABANDONED";
    }

    private LocalDateTime findLastActivity(Participant participant) {
        return participantAnswerRepository.findByParticipantIdOrderByAnsweredAt(participant.getId()).stream().findFirst()
                .map(ParticipantAnswer::getAnsweredAt)
                .orElse(participant.getJoinedAt());
    }

    private SessionReportResponse.PerformanceMetrics analyzeParticipantPerformance(
            Participant participant, List<ParticipantAnswer> answers) {

        double accuracyRate = answers.isEmpty() ? 0.0 :
                (double) answers.stream().mapToInt(a -> a.getIsCorrect() ? 1 : 0).sum() / answers.size() * 100;

        String performanceLevel;
        if (accuracyRate >= 90) performanceLevel = "EXCELLENT";
        else if (accuracyRate >= 75) performanceLevel = "GOOD";
        else if (accuracyRate >= 60) performanceLevel = "AVERAGE";
        else if (accuracyRate >= 40) performanceLevel = "BELOW_AVERAGE";
        else performanceLevel = "POOR";

        double consistencyScore = calculateConsistencyScore(answers);
        double improvementTrend = calculateImprovementTrend(answers);
        double speedAccuracyBalance = calculateSpeedAccuracyBalance(answers);

        List<String> strengths = identifyStrengths(answers, accuracyRate);
        List<String> improvements = identifyAreasForImprovement(answers, accuracyRate);
        String recommendations = generateRecommendations(performanceLevel, strengths, improvements);

        return SessionReportResponse.PerformanceMetrics.builder()
                .performanceLevel(performanceLevel)
                .consistencyScore(Math.round(consistencyScore * 100.0) / 100.0)
                .improvementTrend(Math.round(improvementTrend * 100.0) / 100.0)
                .speedAccuracyBalance(Math.round(speedAccuracyBalance * 100.0) / 100.0)
                .strengths(strengths)
                .areasForImprovement(improvements)
                .recommendedActions(recommendations)
                .build();
    }

    private int calculateLongestCorrectStreak(List<ParticipantAnswer> answers) {
        int maxStreak = 0;
        int currentStreak = 0;

        for (ParticipantAnswer answer : answers) {
            if (answer.getIsCorrect()) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return maxStreak;
    }

    private int calculateLongestIncorrectStreak(List<ParticipantAnswer> answers) {
        int maxStreak = 0;
        int currentStreak = 0;

        for (ParticipantAnswer answer : answers) {
            if (!answer.getIsCorrect()) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return maxStreak;
    }

    private List<String> identifyStrongTopics(List<ParticipantAnswer> answers) {
        // Placeholder - would analyze question topics/categories
        return Arrays.asList("Quick Response", "Multiple Choice");
    }

    private List<String> identifyWeakTopics(List<ParticipantAnswer> answers) {
        // Placeholder - would analyze question topics/categories
        return Arrays.asList("Complex Problems", "Time Management");
    }

    private String determineAnswerStatus(ParticipantAnswer answer) {
        if (answer.getSelectedAnswer() == null) return "SKIPPED";
        if (answer.getIsCorrect()) return "CORRECT";
        return "INCORRECT";
    }

    private String categorizeResponseSpeed(Integer timeTaken, Integer timeLimit) {
        if (timeTaken == null || timeLimit == null) return "UNKNOWN";

        double ratio = (double) timeTaken / timeLimit;
        if (ratio <= 0.2) return "VERY_FAST";
        if (ratio <= 0.4) return "FAST";
        if (ratio <= 0.7) return "NORMAL";
        if (ratio <= 0.9) return "SLOW";
        return "VERY_SLOW";
    }

    private boolean analyzeIfGuessed(ParticipantAnswer answer) {
        // Simple heuristic: very fast incorrect answers might be guesses
        if (answer.getTimeTaken() == null || answer.getQuestion().getTimeLimit() == null) {
            return false;
        }

        double ratio = (double) answer.getTimeTaken() / answer.getQuestion().getTimeLimit();
        return !answer.getIsCorrect() && ratio < 0.1;
    }

    // Additional helper methods for insights and recommendations

    private List<String> findMostImprovedParticipants(List<Participant> participants, QuizSession session) {
        // Placeholder - would analyze performance trends
        return participants.stream()
                .limit(3)
                .map(Participant::getNickname)
                .collect(Collectors.toList());
    }

    private List<String> findStrugglingParticipants(List<Participant> participants, QuizSession session) {
        return participants.stream()
                .filter(p -> {
                    List<ParticipantAnswer> answers = participantAnswerRepository.findByParticipantIdOrderByAnsweredAt(p.getId());
                    if (answers.isEmpty()) return true;
                    double accuracy = (double) answers.stream().mapToInt(a -> a.getIsCorrect() ? 1 : 0).sum() / answers.size();
                    return accuracy < 0.4; // Less than 40% accuracy
                })
                .limit(5)
                .map(Participant::getNickname)
                .collect(Collectors.toList());
    }

    private String findPeakParticipationTime(QuizSession session) {
        // Placeholder - would analyze answer timestamps
        return "Mid-session";
    }

    private String calculateAverageSessionDuration(List<Participant> participants, QuizSession session) {
        // Placeholder - would calculate based on participant activity
        return "25 minutes";
    }

    private double calculateDropoffRate(List<Participant> participants, QuizSession session) {
        if (participants.isEmpty()) return 0.0;

        long droppedOut = participants.stream()
                .filter(p -> {
                    long answers = participantAnswerRepository.countByParticipantId(p.getId());
                    return answers < session.getTotalQuestions() * 0.3; // Less than 30% completion
                })
                .count();

        return (double) droppedOut / participants.size() * 100;
    }

    private List<String> generateHostRecommendations(
            QuizSession session,
            List<SessionReportResponse.QuestionAnalysis> questionAnalysis,
            List<Participant> participants) {

        List<String> recommendations = new ArrayList<>();

        // Analyze question difficulty distribution
        long hardQuestions = questionAnalysis.stream()
                .filter(q -> "Hard".equals(q.getDifficulty()))
                .count();

        if (hardQuestions > questionAnalysis.size() * 0.5) {
            recommendations.add("Consider reducing question difficulty - over 50% of questions were marked as 'Hard'");
        }

        // Analyze participation
        double avgAccuracy = questionAnalysis.stream()
                .mapToDouble(SessionReportResponse.QuestionAnalysis::getAccuracyRate)
                .average()
                .orElse(0.0);

        if (avgAccuracy < 60) {
            recommendations.add("Overall accuracy is low - consider providing more guidance or review materials");
        }

        // Analyze timing
        double avgResponseTime = questionAnalysis.stream()
                .mapToDouble(SessionReportResponse.QuestionAnalysis::getAverageResponseTime)
                .average()
                .orElse(0.0);

        if (avgResponseTime > 30) {
            recommendations.add("Participants took longer than expected - consider increasing time limits");
        }

        return recommendations;
    }

    private List<String> generateContentRecommendations(
            List<SessionReportResponse.QuestionAnalysis> questionAnalysis) {

        List<String> recommendations = new ArrayList<>();

        // Find questions with low accuracy
        questionAnalysis.stream()
                .filter(q -> q.getAccuracyRate() < 40)
                .forEach(q -> recommendations.add(
                        "Question " + q.getQuestionNumber() + " had low accuracy (" +
                                q.getAccuracyRate() + "%) - consider reviewing or providing better explanations"
                ));

        // Find questions with high skip rate
        questionAnalysis.stream()
                .filter(q -> q.getNoResponses() > q.getTotalResponses())
                .forEach(q -> recommendations.add(
                        "Question " + q.getQuestionNumber() + " was skipped frequently - " +
                                "consider simplifying or providing more context"
                ));

        return recommendations;
    }

    private Map<String, String> generateParticipantFeedback(
            List<Participant> participants, QuizSession session) {

        Map<String, String> feedback = new HashMap<>();

        participants.forEach(participant -> {
            List<ParticipantAnswer> answers = participantAnswerRepository.findByParticipantIdOrderByAnsweredAt(participant.getId());

            if (answers.isEmpty()) {
                feedback.put(participant.getId(), "No answers submitted - encourage participation next time!");
                return;
            }

            double accuracy = (double) answers.stream().mapToInt(a -> a.getIsCorrect() ? 1 : 0).sum() / answers.size() * 100;

            String personalFeedback;
            if (accuracy >= 90) {
                personalFeedback = "Excellent performance! You demonstrated strong understanding of the material.";
            } else if (accuracy >= 75) {
                personalFeedback = "Good job! You showed solid knowledge with room for minor improvements.";
            } else if (accuracy >= 60) {
                personalFeedback = "Average performance. Consider reviewing the topics you found challenging.";
            } else if (accuracy >= 40) {
                personalFeedback = "Below average performance. Focus on studying the key concepts covered.";
            } else {
                personalFeedback = "Significant improvement needed. Consider additional study time and practice.";
            }

            feedback.put(participant.getId(), personalFeedback);
        });

        return feedback;
    }

    // Performance analysis helper methods

    private double calculateConsistencyScore(List<ParticipantAnswer> answers) {
        if (answers.size() < 2) return 100.0;

        // Calculate variance in performance
        List<Boolean> results = answers.stream()
                .map(ParticipantAnswer::getIsCorrect)
                .collect(Collectors.toList());

        // Simple consistency measure based on streaks
        int changes = 0;
        for (int i = 1; i < results.size(); i++) {
            if (!results.get(i).equals(results.get(i-1))) {
                changes++;
            }
        }

        // Higher consistency = fewer changes
        return Math.max(0, 100 - (changes * 10));
    }

    private double calculateImprovementTrend(List<ParticipantAnswer> answers) {
        if (answers.size() < 3) return 0.0;

        // Compare first third vs last third performance
        int thirdSize = answers.size() / 3;

        double firstThirdAccuracy = answers.subList(0, thirdSize).stream()
                .mapToDouble(a -> a.getIsCorrect() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);

        double lastThirdAccuracy = answers.subList(answers.size() - thirdSize, answers.size()).stream()
                .mapToDouble(a -> a.getIsCorrect() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);

        return (lastThirdAccuracy - firstThirdAccuracy) * 100;
    }

    private double calculateSpeedAccuracyBalance(List<ParticipantAnswer> answers) {
        if (answers.isEmpty()) return 0.0;

        double avgAccuracy = answers.stream()
                .mapToDouble(a -> a.getIsCorrect() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);

        double avgSpeed = answers.stream()
                .filter(a -> a.getTimeTaken() != null && a.getQuestion().getTimeLimit() != null)
                .mapToDouble(a -> 1.0 - ((double) a.getTimeTaken() / a.getQuestion().getTimeLimit()))
                .average()
                .orElse(0.0);

        // Balance score: both accuracy and speed are important
        return (avgAccuracy * 0.7 + avgSpeed * 0.3) * 100;
    }

    private List<String> identifyStrengths(List<ParticipantAnswer> answers, double accuracyRate) {
        List<String> strengths = new ArrayList<>();

        if (accuracyRate >= 80) {
            strengths.add("High accuracy rate");
        }

        double avgResponseTime = answers.stream()
                .filter(a -> a.getTimeTaken() != null && a.getQuestion().getTimeLimit() != null)
                .mapToDouble(a -> (double) a.getTimeTaken() / a.getQuestion().getTimeLimit())
                .average()
                .orElse(1.0);

        if (avgResponseTime < 0.5) {
            strengths.add("Quick response time");
        }

        int longestStreak = calculateLongestCorrectStreak(answers);
        if (longestStreak >= 3) {
            strengths.add("Good consistency with " + longestStreak + " correct answers in a row");
        }

        return strengths;
    }

    private List<String> identifyAreasForImprovement(List<ParticipantAnswer> answers, double accuracyRate) {
        List<String> improvements = new ArrayList<>();

        if (accuracyRate < 60) {
            improvements.add("Focus on understanding core concepts");
        }

        double avgResponseTime = answers.stream()
                .filter(a -> a.getTimeTaken() != null && a.getQuestion().getTimeLimit() != null)
                .mapToDouble(a -> (double) a.getTimeTaken() / a.getQuestion().getTimeLimit())
                .average()
                .orElse(0.5);

        if (avgResponseTime > 0.8) {
            improvements.add("Work on time management skills");
        }

        long skippedQuestions = answers.stream()
                .filter(a -> a.getSelectedAnswer() == null)
                .count();

        if (skippedQuestions > answers.size() * 0.2) {
            improvements.add("Reduce question skipping - attempt all questions");
        }

        return improvements;
    }

    private String generateRecommendations(String performanceLevel, List<String> strengths, List<String> improvements) {
        StringBuilder recommendations = new StringBuilder();

        switch (performanceLevel) {
            case "EXCELLENT":
                recommendations.append("Outstanding performance! Continue practicing to maintain this level. ");
                break;
            case "GOOD":
                recommendations.append("Good work! Focus on consistency to reach excellent level. ");
                break;
            case "AVERAGE":
                recommendations.append("Solid foundation. Work on weak areas to improve performance. ");
                break;
            case "BELOW_AVERAGE":
                recommendations.append("Additional study time needed. Focus on fundamental concepts. ");
                break;
            case "POOR":
                recommendations.append("Significant improvement required. Consider seeking additional help. ");
                break;
        }

        if (!improvements.isEmpty()) {
            recommendations.append("Key areas to focus on: ").append(String.join(", ", improvements)).append(".");
        }

        return recommendations.toString();
    }

    // Export helper methods (simplified implementations)

    private byte[] exportToCsv(SessionReportResponse report) {
        // Implementation for CSV export
        StringBuilder csv = new StringBuilder();
        csv.append("Session Report CSV Export\n");
        // Add CSV content here
        return csv.toString().getBytes();
    }

    private byte[] exportToPdf(SessionReportResponse report) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            
            // Add title
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("Session Report")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(title);
            
            // Add session details
            document.add(new com.itextpdf.layout.element.Paragraph("Quiz: " + report.getQuizTitle()).setFontSize(14));
            document.add(new com.itextpdf.layout.element.Paragraph("Session Code: " + report.getSessionCode()).setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("Host: " + report.getHostName()).setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("Date: " + report.getStartTime()).setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            
            // Add statistics
            document.add(new com.itextpdf.layout.element.Paragraph("Statistics").setFontSize(18).setBold());
            SessionReportResponse.SessionStatistics stats = report.getStatistics();
            document.add(new com.itextpdf.layout.element.Paragraph("Total Participants: " + stats.getTotalParticipants()));
            document.add(new com.itextpdf.layout.element.Paragraph("Completion Rate: " + String.format("%.1f%%", stats.getCompletionRate())));
            document.add(new com.itextpdf.layout.element.Paragraph("Average Score: " + String.format("%.1f", stats.getAverageScore())));
            document.add(new com.itextpdf.layout.element.Paragraph("Average Accuracy: " + String.format("%.1f%%", stats.getAverageAccuracy())));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            
            // Add question analysis
            document.add(new com.itextpdf.layout.element.Paragraph("Question Analysis").setFontSize(18).setBold());
            for (SessionReportResponse.QuestionAnalysis qa : report.getQuestionAnalysis()) {
                document.add(new com.itextpdf.layout.element.Paragraph("Q" + qa.getQuestionNumber() + ": " + qa.getQuestionText())
                        .setBold());
                document.add(new com.itextpdf.layout.element.Paragraph("  Accuracy: " + String.format("%.1f%%", qa.getAccuracyRate())));
                document.add(new com.itextpdf.layout.element.Paragraph("  Correct: " + qa.getCorrectResponses() + " / " + qa.getTotalResponses()));
                document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            }
            
            // Add participant rankings
            document.add(new com.itextpdf.layout.element.Paragraph("Top Participants").setFontSize(18).setBold());
            
            // Create table
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(4);
            table.addHeaderCell("Rank");
            table.addHeaderCell("Nickname");
            table.addHeaderCell("Score");
            table.addHeaderCell("Accuracy");
            
            report.getParticipantReports().stream()
                    .limit(10)
                    .forEach(p -> {
                        table.addCell(String.valueOf(p.getFinalRank()));
                        table.addCell(p.getNickname());
                        table.addCell(String.valueOf(p.getTotalScore()));
                        table.addCell(String.format("%.1f%%", p.getAccuracyRate()));
                    });
            
            document.add(table);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private byte[] exportToExcel(SessionReportResponse report) {
        try {
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Create styles
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            // Sheet 1: Overview
            org.apache.poi.ss.usermodel.Sheet overviewSheet = workbook.createSheet("Overview");
            int rowNum = 0;
            
            // Session details
            org.apache.poi.ss.usermodel.Row row = overviewSheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Session Report");
            row.getCell(0).setCellStyle(headerStyle);
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Quiz: " + report.getQuizTitle());
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Session Code: " + report.getSessionCode());
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Host: " + report.getHostName());
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Date: " + report.getStartTime());
            rowNum++;
            
            // Statistics
            SessionReportResponse.SessionStatistics stats = report.getStatistics();
            row = overviewSheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Statistics");
            row.getCell(0).setCellStyle(headerStyle);
            
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Total Participants: " + stats.getTotalParticipants());
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Completion Rate: " + String.format("%.1f%%", stats.getCompletionRate()));
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Average Score: " + String.format("%.1f", stats.getAverageScore()));
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Average Accuracy: " + String.format("%.1f%%", stats.getAverageAccuracy()));
            overviewSheet.createRow(rowNum++).createCell(0).setCellValue("Total Questions: " + stats.getTotalQuestions());
            
            // Sheet 2: Questions
            org.apache.poi.ss.usermodel.Sheet questionsSheet = workbook.createSheet("Questions");
            rowNum = 0;
            
            // Headers
            row = questionsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Question #");
            row.createCell(1).setCellValue("Question Text");
            row.createCell(2).setCellValue("Type");
            row.createCell(3).setCellValue("Difficulty");
            row.createCell(4).setCellValue("Total Attempts");
            row.createCell(5).setCellValue("Correct");
            row.createCell(6).setCellValue("Incorrect");
            row.createCell(7).setCellValue("Accuracy %");
            
            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(headerStyle);
            }
            
            // Question data
            for (SessionReportResponse.QuestionAnalysis qa : report.getQuestionAnalysis()) {
                row = questionsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(qa.getQuestionNumber());
                row.createCell(1).setCellValue(qa.getQuestionText());
                row.createCell(2).setCellValue(qa.getQuestionType());
                row.createCell(3).setCellValue(qa.getDifficulty());
                row.createCell(4).setCellValue(qa.getTotalResponses());
                row.createCell(5).setCellValue(qa.getCorrectResponses());
                row.createCell(6).setCellValue(qa.getIncorrectResponses());
                row.createCell(7).setCellValue(String.format("%.1f%%", qa.getAccuracyRate()));
            }
            
            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                questionsSheet.autoSizeColumn(i);
            }
            
            // Sheet 3: Participants
            org.apache.poi.ss.usermodel.Sheet participantsSheet = workbook.createSheet("Participants");
            rowNum = 0;
            
            // Headers
            row = participantsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Rank");
            row.createCell(1).setCellValue("Nickname");
            row.createCell(2).setCellValue("Total Score");
            row.createCell(3).setCellValue("Questions Answered");
            row.createCell(4).setCellValue("Correct Answers");
            row.createCell(5).setCellValue("Incorrect Answers");
            row.createCell(6).setCellValue("Accuracy %");
            row.createCell(7).setCellValue("Avg Response Time (s)");
            
            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(headerStyle);
            }
            
            // Participant data
            for (SessionReportResponse.ParticipantReport pr : report.getParticipantReports()) {
                row = participantsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(pr.getFinalRank());
                row.createCell(1).setCellValue(pr.getNickname());
                row.createCell(2).setCellValue(pr.getTotalScore());
                row.createCell(3).setCellValue(pr.getQuestionsAnswered());
                row.createCell(4).setCellValue(pr.getCorrectAnswers());
                row.createCell(5).setCellValue(pr.getIncorrectAnswers());
                row.createCell(6).setCellValue(String.format("%.1f%%", pr.getAccuracyRate()));
                row.createCell(7).setCellValue(String.format("%.1f", pr.getAverageResponseTime()));
            }
            
            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                participantsSheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        String property = switch (sortBy != null ? sortBy.toLowerCase() : "score") {
            case "accuracy" -> "totalScore"; // Would need custom sorting logic
            case "responsetime" -> "totalScore"; // Would need custom sorting logic
            case "nickname" -> "nickname";
            default -> "totalScore";
        };

        return Sort.by(direction, property);
    }

    private List<SessionReportResponse.ParticipantReport> getAllParticipantReports(String sessionCode) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        List<Participant> participants = participantRepository.findBySessionId(session.getId());

        return participants.stream()
                .map(participant -> generateParticipantReport(participant, session))
                .collect(Collectors.toList());
    }
}