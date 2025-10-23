package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Integer totalSessionsHosted = 0;

    @Column(nullable = false)
    private Integer totalParticipants = 0;

    @Column(nullable = false)
    private Integer totalCompletions = 0;

    @Column
    private Double averageParticipantsPerSession = 0.0;

    @Column
    private Integer peakParticipants = 0;

    @Column(nullable = false)
    private Long totalQuestionsAnswered = 0L;

    @Column(nullable = false)
    private Long totalCorrectAnswers = 0L;

    @Column
    private Double overallAccuracyRate = 0.0;

    @Column
    private LocalDateTime firstPlayedAt;

    @Column
    private LocalDateTime lastPlayedAt;

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

    public void incrementSessionCount() {
        this.totalSessionsHosted++;
        this.lastPlayedAt = LocalDateTime.now();
    }

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

    public void incrementCompletionCount() {
        this.totalCompletions++;
    }

    public void updateQuestionStats(long questionsAnswered, long correctAnswers) {
        this.totalQuestionsAnswered += questionsAnswered;
        this.totalCorrectAnswers += correctAnswers;

        // Recalculate overall accuracy
        if (this.totalQuestionsAnswered > 0) {
            this.overallAccuracyRate = ((double) this.totalCorrectAnswers / this.totalQuestionsAnswered) * 100;
        }
    }
}

