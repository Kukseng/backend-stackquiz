package kh.edu.cstad.stackquizapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import kh.edu.cstad.stackquizapi.util.QuestionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id")
    private String id;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)

    private Integer questionOrder = 1;

    @Column(nullable = false)
    private Integer timeLimit;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 500)
    private String imageUrl;

    @Column(updatable = false)
    private Timestamp createdAt;

    private Timestamp updatedAt;

    @OneToMany(mappedBy = "question")
    @JsonManagedReference
    private List<Option> options;

    @OneToMany(mappedBy = "question")
    @JsonIgnore // ignore in JSON output
    private List<ParticipantAnswer> participantAnswers;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonIgnore
    private Quiz quiz;

}
