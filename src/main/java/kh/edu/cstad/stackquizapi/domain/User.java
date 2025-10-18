package kh.edu.cstad.stackquizapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 250)
    private String avatarUrl;

    @Column(length = 250)
    private String profileUser;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    private List<Quiz> quizzes;

    @OneToMany(mappedBy = "user")
    private List<Participant> participants;

    @OneToMany(mappedBy = "host")
    private List<QuizSession> quizSessions;

    @OneToMany(mappedBy = "user")
    private List<QuizReport> quizReports;

    @OneToMany(mappedBy = "user")
    private List<QuizFeedback> quizFeedbacks;

}

