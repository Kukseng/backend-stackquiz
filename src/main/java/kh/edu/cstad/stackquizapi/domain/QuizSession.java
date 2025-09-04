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

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode leaderboardData;

}