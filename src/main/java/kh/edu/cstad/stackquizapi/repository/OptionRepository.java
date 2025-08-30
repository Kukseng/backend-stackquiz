package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, String> {

    @Query("SELECT MAX(o.optionOrder) FROM Option o WHERE o.question.id = :questionId")
    Integer findMaxOptionOrderByQuestionId(@Param("questionId") String questionId);

}
