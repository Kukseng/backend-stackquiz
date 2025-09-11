package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="feedback_summaries")
public class FeedbackSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    private int totalFeedback;

    private LocalDateTime createdAt;

}
