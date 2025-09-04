package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

}