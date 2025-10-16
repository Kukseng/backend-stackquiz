package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, String> {

    List<Option> findByQuestion_Id(String questionId);

    @Query("SELECT MAX(o.optionOrder) FROM Option o WHERE o.question.id = :questionId")
    Integer findMaxOptionOrderByQuestionId(@Param("questionId") String questionId);

    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId ORDER BY o.optionOrder ASC")
    List<Option> findByQuestionIdOrderByOptionOrder(@Param("questionId") String questionId);

    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId AND o.isCorrected = :isCorrect")
    List<Option> findByQuestionIdAndIsCorrected(@Param("questionId") String questionId, @Param("isCorrect") Boolean isCorrect);

}
