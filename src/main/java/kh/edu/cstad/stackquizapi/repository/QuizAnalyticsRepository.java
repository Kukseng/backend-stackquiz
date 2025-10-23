package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.QuizAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizAnalyticsRepository extends JpaRepository<QuizAnalytics, Long> {

    Optional<QuizAnalytics> findByQuizId(String quizId);

    boolean existsByQuizId(String quizId);
}

