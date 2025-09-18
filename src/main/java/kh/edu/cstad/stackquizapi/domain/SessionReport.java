package kh.edu.cstad.stackquizapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_reports")
@Getter
@Setter
@NoArgsConstructor
public class SessionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id")
    private String id;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "session_overview")
    private JsonNode sessionOverview;


    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "question_breakdown")
    private JsonNode questionBreakdown;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "participant_details")
    private JsonNode participantDetails;


    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "final_rankings")
    private JsonNode finalRankings;

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}