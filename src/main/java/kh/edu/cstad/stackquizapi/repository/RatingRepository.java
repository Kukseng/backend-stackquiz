package kh.edu.cstad.stackquizapi.repository;

import io.lettuce.core.dynamic.annotation.Param;
import kh.edu.cstad.stackquizapi.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    Optional<Rating> findByQuizId(String quizId);

    Optional<Rating> findByUserId(String userId);

    void deleteByQuizIdAndUserId(String quizId, String userId);

    Optional<Rating> findByQuizIdAndUserId(String quizId, String userId);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.quiz.id = :quizId")
    double findAverageByQuizId(@Param("quizId") String quizId);


}
