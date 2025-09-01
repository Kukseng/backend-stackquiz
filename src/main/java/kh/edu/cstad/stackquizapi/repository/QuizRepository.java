package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, String> {

    Optional<Quiz> findQuizById(String quizId);

    Optional<Quiz> findByTitle(String title);

    Optional<Quiz> findByIsActive(Boolean isActive);
    Optional<Quiz> findByUserId(String userId);
//    Optional<Quiz>   findUserById(String userId);
}

