package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_categories")
@Getter
@Setter
@NoArgsConstructor
public class QuizCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quiz_category_id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
