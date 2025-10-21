package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks analytics for each quiz across all sessions
 * - Total times hosted (session count)
 * - Total participants across all sessions
 * - Last played timestamp
 */
@Entity
@Table(name = "quiz_analytics")
@Data
@NoArgsConstructor
public class QuizAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String quizId;

    /**
     * Total number of sessions hosted for this quiz
     * Example: 100 (this quiz has been hosted 100 times)
     */
    @Column(nullable = false)
    private Integer totalSessionsHosted = 0;

    /**
     * Total number of unique participants across all sessions
     * Example: 5000 (5K people have played this quiz)
     */
    @Column(nullable = false)
    private Integer totalParticipants = 0;

    /**
     * Total number of times this quiz has been completed
     */
    @Column(nullable = false)
    private Integer totalCompletions = 0;

    /**
     * Average number of participants per session
     */
    @Column
    private Double averageParticipantsPerSession = 0.0;

    /**
     * Highest number of participants in a single session
     */
    @Column
    private Integer peakParticipants = 0;

    /**
     * Total questions answered across all sessions
     */
    @Column(nullable = false)
    private Long totalQuestionsAnswered = 0L;

    /**
     * Total correct answers across all sessions
     */
    @Column(nullable = false)
    private Long totalCorrectAnswers = 0L;

    /**
     * Overall accuracy rate (percentage)
     */
    @Column
    private Double overallAccuracyRate = 0.0;

    /**
     * When this quiz was first played
     */
    @Column
    private LocalDateTime firstPlayedAt;

    /**
     * When this quiz was last played
     */
    @Column
    private LocalDateTime lastPlayedAt;

    /**
     * When analytics were last updated
     */
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        if (firstPlayedAt == null) {
            firstPlayedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment session count when a new session is created
     */
    public void incrementSessionCount() {
        this.totalSessionsHosted++;
        this.lastPlayedAt = LocalDateTime.now();
    }

    /**
     * Add participants when a session starts or participants join
     */
    public void addParticipants(int count) {
        this.totalParticipants += count;

        // Update peak participants if this is a new record
        if (count > this.peakParticipants) {
            this.peakParticipants = count;
        }

        // Recalculate average
        if (this.totalSessionsHosted > 0) {
            this.averageParticipantsPerSession = (double) this.totalParticipants / this.totalSessionsHosted;
        }
    }

    /**
     * Increment completion count when a session ends
     */
    public void incrementCompletionCount() {
        this.totalCompletions++;
    }

    /**
     * Update question statistics
     */
    public void updateQuestionStats(long questionsAnswered, long correctAnswers) {
        this.totalQuestionsAnswered += questionsAnswered;
        this.totalCorrectAnswers += correctAnswers;

        // Recalculate overall accuracy
        if (this.totalQuestionsAnswered > 0) {
            this.overallAccuracyRate = ((double) this.totalCorrectAnswers / this.totalQuestionsAnswered) * 100;
        }
    }
}

