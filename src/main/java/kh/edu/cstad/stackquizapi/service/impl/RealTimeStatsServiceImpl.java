package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.websocket.HostProgressMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.LiveStatsMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;
import kh.edu.cstad.stackquizapi.repository.ParticipantAnswerRepository;
import kh.edu.cstad.stackquizapi.repository.ParticipantRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.RealTimeStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeStatsServiceImpl implements RealTimeStatsService {

    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;

    @Override
    public LiveStatsMessage calculateLiveStats(String sessionId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(sessionId);

        int totalParticipants = participants.size();
        int activeParticipants = (int) participants.stream()
                .filter(p -> p.getIsConnected() != null && p.getIsConnected())
                .count();

        // Calculate statistics
        double averageResponseTime = allAnswers.stream()
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        double accuracyRate = allAnswers.isEmpty() ? 0.0 :
                (double) allAnswers.stream()
                        .mapToInt(answer -> answer.getIsCorrect() ? 1 : 0)
                        .sum() / allAnswers.size() * 100;

        int fastestResponseTime = allAnswers.stream()
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .min()
                .orElse(0);

        int slowestResponseTime = allAnswers.stream()
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .max()
                .orElse(0);

        // Find leading participant
        String leadingParticipant = participants.stream()
                .max(Comparator.comparingInt(Participant::getTotalScore))
                .map(Participant::getNickname)
                .orElse("N/A");

        int highestScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .max()
                .orElse(0);

        // Get answer distribution for current question if available
        Map<String, Integer> answerDistribution = new HashMap<>();
        if (session.getCurrentQuestion() != null && session.getCurrentQuestion() > 0) {
            answerDistribution = getAnswerDistribution(sessionId, null);
        }

        return new LiveStatsMessage(
                session.getSessionCode(),
                "SYSTEM",
                totalParticipants,
                activeParticipants,
                session.getCurrentQuestion() != null ? session.getCurrentQuestion() : 0,
                session.getTotalQuestions() != null ? session.getTotalQuestions() : 0,
                answerDistribution,
                averageResponseTime,
                accuracyRate,
                fastestResponseTime,
                slowestResponseTime,
                leadingParticipant,
                highestScore
        );
    }

    @Override
    public HostProgressMessage calculateHostProgress(String sessionId, int currentQuestion) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        
        // Count participants who have answered the current question
        int participantsAnswered = 0;
        if (currentQuestion > 0) {
            participantsAnswered = (int) participants.stream()
                    .filter(p -> hasAnsweredQuestion(p.getId(), currentQuestion))
                    .count();
        }

        List<HostProgressMessage.ParticipantProgress> participantProgress = getParticipantProgress(sessionId);
        HostProgressMessage.SessionStatistics statistics = calculateSessionStatistics(sessionId);

        return new HostProgressMessage(
                session.getSessionCode(),
                "SYSTEM",
                currentQuestion,
                session.getTotalQuestions() != null ? session.getTotalQuestions() : 0,
                participants.size(),
                participantsAnswered,
                participants.size() - participantsAnswered,
                participantProgress,
                statistics
        );
    }

    @Override
    public ParticipantRankingMessage calculateParticipantRanking(String sessionId, String participantId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        Map<String, Integer> rankings = getCurrentRankings(sessionId);
        int currentRank = rankings.getOrDefault(participantId, 0);
        
        List<Participant> allParticipants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        allParticipants.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        int totalParticipants = allParticipants.size();
        boolean isTopPerformer = currentRank == 1;

        // Calculate points behind leader and ahead of next
        int pointsBehindLeader = 0;
        int pointsAheadOfNext = 0;

        if (!allParticipants.isEmpty()) {
            int leaderScore = allParticipants.get(0).getTotalScore();
            pointsBehindLeader = leaderScore - participant.getTotalScore();

            if (currentRank < totalParticipants) {
                int nextParticipantScore = allParticipants.get(currentRank).getTotalScore();
                pointsAheadOfNext = participant.getTotalScore() - nextParticipantScore;
            }
        }

        return new ParticipantRankingMessage(
                session.getSessionCode(),
                "SYSTEM",
                participantId,
                participant.getNickname(),
                currentRank,
                currentRank, // previousRank would need to be tracked separately
                participant.getTotalScore(),
                totalParticipants,
                isTopPerformer,
                pointsBehindLeader,
                pointsAheadOfNext,
                "SAME" // This would need to be calculated based on previous rank
        );
    }

    @Override
    public Map<String, Integer> getAnswerDistribution(String sessionId, String questionId) {
        // This would need to be implemented based on your specific question/option structure
        // For now, returning empty map
        return new HashMap<>();
    }

    @Override
    public List<HostProgressMessage.ParticipantProgress> getParticipantProgress(String sessionId) {
        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        Map<String, Integer> rankings = getCurrentRankings(sessionId);

        return participants.stream()
                .map(participant -> {
                    int answeredQuestions = (int) participantAnswerRepository.countByParticipantId(participant.getId());
                    boolean isAnswering = false; // This would need real-time tracking
                    boolean hasAnsweredCurrentQuestion = false; // This would need current question context

                    return new HostProgressMessage.ParticipantProgress(
                            participant.getId(),
                            participant.getNickname(),
                            answeredQuestions + 1, // Next question to answer
                            participant.getTotalScore(),
                            rankings.getOrDefault(participant.getId(), 0),
                            isAnswering,
                            hasAnsweredCurrentQuestion,
                            System.currentTimeMillis()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public HostProgressMessage.SessionStatistics calculateSessionStatistics(String sessionId) {
        QuizSession session = quizSessionRepository.findBySessionCode(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(sessionId);

        double averageScore = participants.stream()
                .mapToInt(Participant::getTotalScore)
                .average()
                .orElse(0.0);

        double averageResponseTime = allAnswers.stream()
                .mapToInt(ParticipantAnswer::getTimeTaken)
                .average()
                .orElse(0.0);

        int totalAnswers = allAnswers.size();
        int correctAnswers = (int) allAnswers.stream()
                .filter(ParticipantAnswer::getIsCorrect)
                .count();

        double accuracyRate = totalAnswers == 0 ? 0.0 : (double) correctAnswers / totalAnswers * 100;

        String topPerformer = participants.stream()
                .max(Comparator.comparingInt(Participant::getTotalScore))
                .map(Participant::getNickname)
                .orElse("N/A");

        return new HostProgressMessage.SessionStatistics(
                averageScore,
                averageResponseTime,
                totalAnswers,
                correctAnswers,
                accuracyRate,
                topPerformer
        );
    }

    @Override
    public Map<String, Integer> getCurrentRankings(String sessionId) {
        List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
        participants.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

        Map<String, Integer> rankings = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            rankings.put(participants.get(i).getId(), i + 1);
        }
        return rankings;
    }

    @Override
    public String calculateRankChange(int previousRank, int currentRank) {
        if (previousRank == 0) return "NEW";
        if (currentRank < previousRank) return "UP";
        if (currentRank > previousRank) return "DOWN";
        return "SAME";
    }

    private boolean hasAnsweredQuestion(String participantId, int questionNumber) {
        // This would need to be implemented based on your question tracking logic
        long answeredCount = participantAnswerRepository.countByParticipantId(participantId);
        return answeredCount >= questionNumber;
    }
}
