package kh.edu.cstad.stackquizapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import kh.edu.cstad.stackquizapi.util.QuizMode;
import kh.edu.cstad.stackquizapi.util.Status;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_sessions")
@Getter
@Setter
@NoArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private String sessionName;

    @Column(nullable = false, length = 100)
    private String hostName;

    @Column(nullable = false, length = 10)
    private String sessionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    Integer totalQuestions;

    @Column(nullable = false)
    private Integer totalParticipants = 0;

    private Integer currentQuestion;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session")
    private List<Participant> participants;

    @Enumerated(EnumType.STRING)
    private QuizMode mode;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode leaderboardData;

    @Column(name = "session_time_limit")
    private Integer sessionTimeLimit;

    @Column(name = "scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private LocalDateTime scheduledEndTime;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "allow_join_in_progress")
    private Boolean allowJoinInProgress;

    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions;

    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers;

    @Column(name = "default_question_time_limit")
    private Integer defaultQuestionTimeLimit;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "show_leaderboard")
    private Boolean showLeaderboard = true;

    @Column(name = "show_progress")
    private Boolean showProgress = true;

    @Column(name = "play_sound")
    private Boolean playSound = true;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "completion_rate")
    private Double completionRate = 0.0;

    @Column(name = "average_score")
    private Double averageScore = 0.0;

    @Column(name = "total_answers")
    private Integer totalAnswers = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    public boolean isScheduled() {
        return scheduledStartTime != null;
    }

    public boolean canStart() {
        if (scheduledStartTime == null) return true;
        return LocalDateTime.now().isAfter(scheduledStartTime) || LocalDateTime.now().isEqual(scheduledStartTime);
    }

    public boolean isExpired() {
        if (scheduledEndTime == null) return false;
        return LocalDateTime.now().isAfter(scheduledEndTime);
    }

    public boolean canJoin() {
        return status == Status.WAITING ||
                (status == Status.IN_PROGRESS && Boolean.TRUE.equals(allowJoinInProgress));
    }

    public boolean isAtCapacity() {
        return totalParticipants >= maxParticipants;
    }

    public void updateStatistics(int totalAnswers, int correctAnswers, double averageScore) {
        this.totalAnswers = totalAnswers;
        this.correctAnswers = correctAnswers;
        this.averageScore = averageScore;

        if (totalQuestions != null && totalParticipants > 0) {
            int expectedAnswers = totalQuestions * totalParticipants;
            this.completionRate = expectedAnswers > 0 ? (double) totalAnswers / expectedAnswers * 100 : 0.0;
        }
    }
}