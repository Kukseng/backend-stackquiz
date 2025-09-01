package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import kh.edu.cstad.stackquizapi.util.QuizDifficultyType;
import kh.edu.cstad.stackquizapi.util.VisibilityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz")
@Getter
@Setter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quiz_id")
    private String id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private String visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizDifficultyType difficulty;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<QuizCategory> quizCategories = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<Question> questions;

    @OneToMany(mappedBy = "quiz")
    private List<QuizSession> sessions;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}