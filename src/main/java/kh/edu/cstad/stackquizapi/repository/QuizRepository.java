package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, String> {

    Optional<Quiz> findQuizById(String quizId);

    Optional<Quiz> findByTitle(String title);

    Optional<Quiz> findByIsActive(Boolean isActive);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") String quizId);

    Optional<Quiz> findByUserId(String userId);

    @Query("SELECT q FROM Quiz q WHERE q.user.id = :userId ORDER BY q.createdAt DESC")
    java.util.List<Quiz> findByUser_Id(@Param("userId") String userId);
}

