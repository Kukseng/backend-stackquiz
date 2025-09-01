package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "participant_answers")
@Getter
@Setter
@NoArgsConstructor
public class ParticipantAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "answer_id")
    private String id;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer timeTaken;

    @Column(nullable = false)
    private Integer pointsEarned = 0;

    @Column(updatable = false)
    private Timestamp submittedAt;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option selectedAnswer;


    @PrePersist
    public void prePersist() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
        if (isCorrect == null) {
            isCorrect = false;
        }
        if (pointsEarned == null) {
            pointsEarned = 0;
        }
    }
}
