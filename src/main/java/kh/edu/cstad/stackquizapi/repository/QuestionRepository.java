package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {

    boolean existsByQuestionOrder(Integer order);

    List<Question> findByQuizUserId(String userId);

    List<Question> findByQuizId(String s);
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.questionOrder = :order")
    Optional<Question> findQuestionByQuizIdAndOrder(@Param("quizId") String quizId,
                                                    @Param("order") Integer order);

    @Query("SELECT MAX(q.questionOrder) FROM Question q WHERE q.quiz.id = :quizId")
    Integer findMaxQuestionOrderByQuizId(@Param("quizId") String quizId);

    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId ORDER BY q.questionOrder ASC")
    List<Question> findByQuizIdOrderByQuestionOrder(@Param("quizId") String quizId);

}
