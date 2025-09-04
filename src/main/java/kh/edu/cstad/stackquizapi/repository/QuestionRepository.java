package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {

    boolean existsByQuestionOrder(Integer order);

    List<Question> findByQuizId(String s);

    @Query("SELECT MAX(q.questionOrder) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxQuestionOrderByQuizId(@Param("quizId") String quizId);

}
