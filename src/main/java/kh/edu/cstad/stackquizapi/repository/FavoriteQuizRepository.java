package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.FavoriteQuiz;
import kh.edu.cstad.stackquizapi.domain.Quiz;
import kh.edu.cstad.stackquizapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteQuizRepository extends JpaRepository<FavoriteQuiz, String> {

    Optional<FavoriteQuiz> findByUserAndQuiz(User user, Quiz quiz);

}
