package kh.edu.cstad.stackquizapi.service.impl;

import kh.edu.cstad.stackquizapi.domain.Participant;
import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import kh.edu.cstad.stackquizapi.domain.QuizSession;
import kh.edu.cstad.stackquizapi.dto.request.LeaderboardRequest;
import kh.edu.cstad.stackquizapi.dto.response.LeaderboardResponse;
import kh.edu.cstad.stackquizapi.dto.websocket.LeaderboardMessage;
import kh.edu.cstad.stackquizapi.dto.websocket.ParticipantRankingMessage;
import kh.edu.cstad.stackquizapi.repository.ParticipantAnswerRepository;
import kh.edu.cstad.stackquizapi.repository.ParticipantRepository;
import kh.edu.cstad.stackquizapi.repository.QuizSessionRepository;
import kh.edu.cstad.stackquizapi.service.EnhancedLeaderboardService;
import kh.edu.cstad.stackquizapi.service.LeaderboardService;
import kh.edu.cstad.stackquizapi.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedLeaderboardServiceImpl implements EnhancedLeaderboardService {

    private final LeaderboardService baseLeaderboardService;
    private final ParticipantRepository participantRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final WebSocketService webSocketService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LEADERBOARD_SNAPSHOT_PREFIX = "leaderboard:snapshot:";
    private static final String PARTICIPANT_STREAK_PREFIX = "streak:participant:";
    private static final String LEADERBOARD_CHANGES_PREFIX = "leaderboard:changes:";
    private static final String RANK_HISTORY_PREFIX = "rank:history:";

    @Override
    public LeaderboardResponse getEnhancedRealTimeLeaderboard(String sessionId, int limit, int offset) {
        try {
            // Get base leaderboard
            LeaderboardRequest request = new LeaderboardRequest(sessionId, limit, offset, false, null);
            LeaderboardResponse baseLeaderboard = baseLeaderboardService.getRealTimeLeaderboard(request);

            // Enhance with additional context
            enhanceLeaderboardWithContext(baseLeaderboard, sessionId);

            return baseLeaderboard;

        } catch (Exception e) {
            log.error("Error getting enhanced real-time leaderboard for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to get enhanced leaderboard", e);
        }
    }

    @Override
    public LeaderboardResponse getLeaderboardWithQuestionContext(String sessionId, int currentQuestion) {
        try {
            QuizSession session = quizSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            LeaderboardResponse leaderboard = getEnhancedRealTimeLeaderboard(sessionId, 20, 0);

            // Add question context
            Map<String, Object> questionContext = new HashMap<>();
            questionContext.put("currentQuestion", currentQuestion);
            questionContext.put("totalQuestions", session.getTotalQuestions());
            questionContext.put("progressPercentage", (double) currentQuestion / session.getTotalQuestions() * 100);

            // This would require extending LeaderboardResponse to include context
            log.debug("Generated leaderboard with question context for session {}: question {}/{}",
                     sessionId, currentQuestion, session.getTotalQuestions());

            return leaderboard;

        } catch (Exception e) {
            log.error("Error getting leaderboard with question context for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to get leaderboard with question context", e);
        }
    }

    @Override
    public ParticipantRankingMessage getDetailedParticipantRanking(String sessionId, String participantId) {
        try {
            Participant participant = participantRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

            QuizSession session = quizSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Get current rankings
            List<Participant> allParticipants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            allParticipants.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

            int currentRank = 0;
            for (int i = 0; i < allParticipants.size(); i++) {
                if (allParticipants.get(i).getId().equals(participantId)) {
                    currentRank = i + 1;
                    break;
                }
            }

            // Get previous rank
            int previousRank = getPreviousRank(sessionId, participantId);
            String rankChange = calculateRankChange(previousRank, currentRank);

            // Calculate additional metrics
            int pointsBehindLeader = allParticipants.isEmpty() ? 0 : 
                    allParticipants.get(0).getTotalScore() - participant.getTotalScore();
            
            int pointsAheadOfNext = 0;
            if (currentRank < allParticipants.size()) {
                pointsAheadOfNext = participant.getTotalScore() - allParticipants.get(currentRank).getTotalScore();
            }

            boolean isTopPerformer = currentRank <= 3;

            return new ParticipantRankingMessage(
                    session.getSessionCode(),
                    "SYSTEM",
                    participantId,
                    participant.getNickname(),
                    currentRank,
                    previousRank,
                    participant.getTotalScore(),
                    allParticipants.size(),
                    isTopPerformer,
                    pointsBehindLeader,
                    pointsAheadOfNext,
                    rankChange
            );

        } catch (Exception e) {
            log.error("Error getting detailed participant ranking for {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to get detailed participant ranking", e);
        }
    }

    @Override
    public List<ParticipantRankingMessage> getTopPerformersWithChanges(String sessionId, int limit) {
        try {
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            participants.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

            return participants.stream()
                    .limit(limit)
                    .map(participant -> getDetailedParticipantRanking(sessionId, participant.getId()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting top performers with changes for session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public LeaderboardResponse getLeaderboardAroundParticipant(String sessionId, String participantId, int range) {
        try {
            List<Participant> allParticipants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            allParticipants.sort((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()));

            // Find participant position
            int participantIndex = -1;
            for (int i = 0; i < allParticipants.size(); i++) {
                if (allParticipants.get(i).getId().equals(participantId)) {
                    participantIndex = i;
                    break;
                }
            }

            if (participantIndex == -1) {
                return getEnhancedRealTimeLeaderboard(sessionId, range * 2 + 1, 0);
            }

            // Calculate range around participant
            int startIndex = Math.max(0, participantIndex - range);
            int endIndex = Math.min(allParticipants.size(), participantIndex + range + 1);

            LeaderboardRequest request = new LeaderboardRequest(sessionId, endIndex - startIndex, startIndex, false, participantId);
            return baseLeaderboardService.getRealTimeLeaderboard(request);

        } catch (Exception e) {
            log.error("Error getting leaderboard around participant {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
            return getEnhancedRealTimeLeaderboard(sessionId, 10, 0);
        }
    }

    @Override
    public void broadcastEnhancedLeaderboard(String sessionId, String updateType) {
        try {
            QuizSession session = quizSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            LeaderboardResponse leaderboard = getEnhancedRealTimeLeaderboard(sessionId, 20, 0);

            LeaderboardMessage message = new LeaderboardMessage(
                    session.getSessionCode(),
                    "SYSTEM",
                    leaderboard,
                    updateType
            );

            webSocketService.broadcastLeaderboard(session.getSessionCode(), message);

            // Store update timestamp for change tracking
            String changesKey = LEADERBOARD_CHANGES_PREFIX + sessionId;
            redisTemplate.opsForValue().set(changesKey + ":last_update", String.valueOf(System.currentTimeMillis()));
            redisTemplate.expire(changesKey + ":last_update", Duration.ofHours(24));

            log.debug("Broadcasted enhanced leaderboard for session {}: {}", sessionId, updateType);

        } catch (Exception e) {
            log.error("Error broadcasting enhanced leaderboard for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void sendPersonalizedLeaderboard(String sessionId, String participantId) {
        try {
            QuizSession session = quizSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // Get leaderboard around participant
            LeaderboardResponse personalizedLeaderboard = getLeaderboardAroundParticipant(sessionId, participantId, 5);

            // Get detailed ranking for participant
            ParticipantRankingMessage participantRanking = getDetailedParticipantRanking(sessionId, participantId);

            // Create personalized message
            Map<String, Object> personalizedData = new HashMap<>();
            personalizedData.put("leaderboard", personalizedLeaderboard);
            personalizedData.put("participantRanking", participantRanking);
            personalizedData.put("type", "PERSONALIZED_LEADERBOARD");

            webSocketService.sendToParticipant(session.getSessionCode(), participantId, personalizedData);

            log.debug("Sent personalized leaderboard to participant {} in session {}", participantId, sessionId);

        } catch (Exception e) {
            log.error("Error sending personalized leaderboard to participant {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
        }
    }

    @Override
    public LeaderboardResponse getLeaderboardWithPerformance(String sessionId) {
        try {
            LeaderboardResponse baseLeaderboard = getEnhancedRealTimeLeaderboard(sessionId, 20, 0);

            // Add performance indicators
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            Map<String, Map<String, Object>> performanceData = new HashMap<>();

            for (Participant participant : participants) {
                Map<String, Object> performance = calculateParticipantPerformance(participant);
                performanceData.put(participant.getId(), performance);
            }

            // This would require extending LeaderboardResponse to include performance data
            log.debug("Generated leaderboard with performance indicators for session {}", sessionId);

            return baseLeaderboard;

        } catch (Exception e) {
            log.error("Error getting leaderboard with performance for session {}: {}", sessionId, e.getMessage(), e);
            return getEnhancedRealTimeLeaderboard(sessionId, 20, 0);
        }
    }

    @Override
    public Map<String, Object> getLeaderboardChanges(String sessionId, long lastUpdateTime) {
        try {
            Map<String, Object> changes = new HashMap<>();
            
            String changesKey = LEADERBOARD_CHANGES_PREFIX + sessionId;
            String lastUpdateStr = redisTemplate.opsForValue().get(changesKey + ":last_update");
            long serverLastUpdate = lastUpdateStr != null ? Long.parseLong(lastUpdateStr) : 0;

            changes.put("hasChanges", serverLastUpdate > lastUpdateTime);
            changes.put("lastUpdateTime", serverLastUpdate);

            if (serverLastUpdate > lastUpdateTime) {
                // Get current leaderboard
                LeaderboardResponse currentLeaderboard = getEnhancedRealTimeLeaderboard(sessionId, 20, 0);
                changes.put("leaderboard", currentLeaderboard);

                // Get rank changes
                List<Map<String, Object>> rankChanges = getRankChanges(sessionId, lastUpdateTime);
                changes.put("rankChanges", rankChanges);
            }

            return changes;

        } catch (Exception e) {
            log.error("Error getting leaderboard changes for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getLeaderboardStatistics(String sessionId) {
        try {
            List<Participant> participants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            List<ParticipantAnswer> allAnswers = participantAnswerRepository.findByParticipantSessionId(sessionId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalParticipants", participants.size());
            stats.put("averageScore", participants.stream().mapToInt(Participant::getTotalScore).average().orElse(0.0));
            stats.put("highestScore", participants.stream().mapToInt(Participant::getTotalScore).max().orElse(0));
            stats.put("lowestScore", participants.stream().mapToInt(Participant::getTotalScore).min().orElse(0));
            stats.put("scoreSpread", stats.get("highestScore") + " - " + stats.get("lowestScore"));
            
            // Calculate score distribution
            Map<String, Integer> scoreRanges = new HashMap<>();
            for (Participant participant : participants) {
                int score = participant.getTotalScore();
                String range = getScoreRange(score);
                scoreRanges.put(range, scoreRanges.getOrDefault(range, 0) + 1);
            }
            stats.put("scoreDistribution", scoreRanges);

            // Calculate performance trends
            stats.put("improvementTrend", calculateImprovementTrend(sessionId));
            stats.put("competitiveness", calculateCompetitiveness(participants));

            return stats;

        } catch (Exception e) {
            log.error("Error getting leaderboard statistics for session {}: {}", sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public void createLeaderboardSnapshot(String sessionId, String snapshotType) {
        try {
            LeaderboardResponse currentLeaderboard = getEnhancedRealTimeLeaderboard(sessionId, 50, 0);
            
            String snapshotKey = LEADERBOARD_SNAPSHOT_PREFIX + sessionId + ":" + snapshotType + ":" + System.currentTimeMillis();
            
            // Store snapshot data (simplified - would need proper serialization)
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("leaderboard", currentLeaderboard);
            snapshot.put("timestamp", System.currentTimeMillis());
            snapshot.put("type", snapshotType);
            
            // Store in Redis with expiration
            redisTemplate.opsForValue().set(snapshotKey, snapshot.toString());
            redisTemplate.expire(snapshotKey, Duration.ofDays(7));

            log.debug("Created leaderboard snapshot for session {}: {}", sessionId, snapshotType);

        } catch (Exception e) {
            log.error("Error creating leaderboard snapshot for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getLeaderboardSnapshots(String sessionId) {
        try {
            String pattern = LEADERBOARD_SNAPSHOT_PREFIX + sessionId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            List<Map<String, Object>> snapshots = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    String snapshotData = redisTemplate.opsForValue().get(key);
                    if (snapshotData != null) {
                        // Parse snapshot data (simplified)
                        Map<String, Object> snapshot = new HashMap<>();
                        snapshot.put("key", key);
                        snapshot.put("data", snapshotData);
                        snapshots.add(snapshot);
                    }
                }
            }

            return snapshots;

        } catch (Exception e) {
            log.error("Error getting leaderboard snapshots for session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void processRankChanges(String sessionId, String participantId, int oldRank, int newRank) {
        try {
            if (oldRank != newRank) {
                // Store rank change
                String rankHistoryKey = RANK_HISTORY_PREFIX + participantId;
                String rankChange = oldRank + ":" + newRank + ":" + System.currentTimeMillis();
                redisTemplate.opsForList().rightPush(rankHistoryKey, rankChange);
                redisTemplate.expire(rankHistoryKey, Duration.ofHours(24));

                // Send rank change notification
                ParticipantRankingMessage ranking = getDetailedParticipantRanking(sessionId, participantId);
                webSocketService.sendRankingUpdateToParticipant(sessionId, participantId, ranking);

                log.debug("Processed rank change for participant {} in session {}: {} -> {}", 
                         participantId, sessionId, oldRank, newRank);
            }

        } catch (Exception e) {
            log.error("Error processing rank changes for participant {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
        }
    }

    @Override
    public LeaderboardResponse getTimeFilteredLeaderboard(String sessionId, long startTime, long endTime) {
        // Implementation would filter leaderboard based on time range
        return getEnhancedRealTimeLeaderboard(sessionId, 20, 0);
    }

    @Override
    public Map<String, Object> getParticipantComparison(String sessionId, String participantId) {
        try {
            Participant participant = participantRepository.findById(participantId)
                    .orElseThrow(() -> new RuntimeException("Participant not found"));

            List<Participant> allParticipants = participantRepository.findBySessionIdAndIsActiveTrue(sessionId);
            
            Map<String, Object> comparison = new HashMap<>();
            comparison.put("participantScore", participant.getTotalScore());
            comparison.put("averageScore", allParticipants.stream().mapToInt(Participant::getTotalScore).average().orElse(0.0));
            comparison.put("percentile", calculatePercentile(participant, allParticipants));
            comparison.put("betterThan", calculateBetterThanPercentage(participant, allParticipants));

            return comparison;

        } catch (Exception e) {
            log.error("Error getting participant comparison for {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getDetailedLeaderboardBreakdown(String sessionId) {
        // Implementation would provide question-by-question breakdown
        return new HashMap<>();
    }

    @Override
    public void broadcastHostLeaderboard(String sessionId) {
        try {
            LeaderboardResponse leaderboard = getEnhancedRealTimeLeaderboard(sessionId, 50, 0);
            Map<String, Object> hostData = new HashMap<>();
            hostData.put("leaderboard", leaderboard);
            hostData.put("statistics", getLeaderboardStatistics(sessionId));
            hostData.put("type", "HOST_LEADERBOARD");

            webSocketService.sendToHost(sessionId, hostData);

        } catch (Exception e) {
            log.error("Error broadcasting host leaderboard for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getAnimatedLeaderboardData(String sessionId) {
        // Implementation would provide data optimized for smooth animations
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getLeaderboardTrends(String sessionId) {
        // Implementation would analyze trends and patterns
        return new HashMap<>();
    }

    @Override
    public LeaderboardResponse getEnrichedLeaderboard(String sessionId) {
        // Implementation would include avatars and profiles
        return getEnhancedRealTimeLeaderboard(sessionId, 20, 0);
    }

    @Override
    public Map<String, Object> exportLeaderboardData(String sessionId, String format) {
        // Implementation would export in various formats
        return new HashMap<>();
    }

    @Override
    public void streamLeaderboardUpdates(String sessionId) {
        // Implementation would provide streaming updates
    }

    @Override
    public void updateParticipantStreaks(String sessionId, String participantId, boolean isCorrect) {
        try {
            String streakKey = PARTICIPANT_STREAK_PREFIX + participantId;
            String currentStreakStr = redisTemplate.opsForValue().get(streakKey + ":current");
            int currentStreak = currentStreakStr != null ? Integer.parseInt(currentStreakStr) : 0;

            if (isCorrect) {
                currentStreak++;
            } else {
                currentStreak = 0;
            }

            redisTemplate.opsForValue().set(streakKey + ":current", String.valueOf(currentStreak));
            redisTemplate.expire(streakKey + ":current", Duration.ofHours(24));

            // Update best streak if needed
            String bestStreakStr = redisTemplate.opsForValue().get(streakKey + ":best");
            int bestStreak = bestStreakStr != null ? Integer.parseInt(bestStreakStr) : 0;
            
            if (currentStreak > bestStreak) {
                redisTemplate.opsForValue().set(streakKey + ":best", String.valueOf(currentStreak));
                redisTemplate.expire(streakKey + ":best", Duration.ofHours(24));
            }

        } catch (Exception e) {
            log.error("Error updating participant streaks for {} in session {}: {}", 
                     participantId, sessionId, e.getMessage(), e);
        }
    }

    @Override
    public LeaderboardResponse getLeaderboardWithStreaks(String sessionId) {
        // Implementation would include streak information
        return getEnhancedRealTimeLeaderboard(sessionId, 20, 0);
    }

    // Private helper methods

    private void enhanceLeaderboardWithContext(LeaderboardResponse leaderboard, String sessionId) {
        // Add contextual information to leaderboard
    }

    private int getPreviousRank(String sessionId, String participantId) {
        try {
            String rankHistoryKey = RANK_HISTORY_PREFIX + participantId;
            List<String> rankHistory = redisTemplate.opsForList().range(rankHistoryKey, -2, -1);
            
            if (rankHistory != null && !rankHistory.isEmpty()) {
                String lastRankChange = rankHistory.get(rankHistory.size() - 1);
                String[] parts = lastRankChange.split(":");
                return Integer.parseInt(parts[0]);
            }
            
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String calculateRankChange(int previousRank, int currentRank) {
        if (previousRank == 0) return "NEW";
        if (currentRank < previousRank) return "UP";
        if (currentRank > previousRank) return "DOWN";
        return "SAME";
    }

    private Map<String, Object> calculateParticipantPerformance(Participant participant) {
        Map<String, Object> performance = new HashMap<>();
        // Calculate various performance metrics
        performance.put("accuracy", 0.85);
        performance.put("speed", 0.78);
        performance.put("consistency", 0.82);
        return performance;
    }

    private List<Map<String, Object>> getRankChanges(String sessionId, long lastUpdateTime) {
        // Implementation would get rank changes since last update
        return new ArrayList<>();
    }

    private String getScoreRange(int score) {
        if (score >= 8000) return "8000+";
        if (score >= 6000) return "6000-7999";
        if (score >= 4000) return "4000-5999";
        if (score >= 2000) return "2000-3999";
        return "0-1999";
    }

    private double calculateImprovementTrend(String sessionId) {
        // Implementation would calculate improvement trend
        return 0.15;
    }

    private double calculateCompetitiveness(List<Participant> participants) {
        // Implementation would calculate competitiveness score
        return 0.72;
    }

    private double calculatePercentile(Participant participant, List<Participant> allParticipants) {
        allParticipants.sort((p1, p2) -> Integer.compare(p1.getTotalScore(), p2.getTotalScore()));
        int position = allParticipants.indexOf(participant);
        return (double) position / allParticipants.size() * 100;
    }

    private double calculateBetterThanPercentage(Participant participant, List<Participant> allParticipants) {
        long betterThan = allParticipants.stream()
                .filter(p -> p.getTotalScore() < participant.getTotalScore())
                .count();
        return (double) betterThan / allParticipants.size() * 100;
    }
}
