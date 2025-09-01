package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "leaderboards")
@Getter
@Setter
@NoArgsConstructor
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "leaderboard_id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false)
    private Integer currentRank;

    @Column(updatable = false)
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

}
