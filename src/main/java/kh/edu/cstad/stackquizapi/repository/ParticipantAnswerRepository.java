package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantAnswerRepository extends JpaRepository<ParticipantAnswer, String> {

    boolean existsByParticipantIdAndQuestionId (String participantId, String questionId);

    List<ParticipantAnswer> findByParticipantIdOrderByAnsweredAt(String participantId);

    Optional<ParticipantAnswer> findByParticipantIdAndQuestionId(String participantId, String questionId);

    List<ParticipantAnswer> findByParticipantIdOrderByAnsweredAtAsc(String participantId);
    long countByParticipantId(String participantId);

//    List<ParticipantAnswer> findByQuestionIdOrderByAnsweredAtAsc(String questionId);

//    boolean existsByParticipantIdAndQuestionId(String participantId, String questionId);

    long countByQuestionId(String questionId);

//    @Query("SELECT COUNT(pa) FROM ParticipantAnswer pa " +
//           "WHERE pa.participant.id = :participantId AND pa.isCorrect = true")
//    long countCorrectAnswersByParticipantId(@Param("participantId") String participantId);

    @Query("SELECT pa FROM ParticipantAnswer pa " +
            "WHERE pa.participant.session.id = :sessionId")
    List<ParticipantAnswer> findByParticipantSessionId(@Param("sessionId") String sessionId);
    
    @Query("SELECT pa FROM ParticipantAnswer pa " +
            "WHERE pa.question.id = :questionId " +
            "AND pa.participant.session.id = :sessionId")
    List<ParticipantAnswer> findByQuestionIdAndParticipantSessionId(
            @Param("questionId") String questionId,
            @Param("sessionId") String sessionId
    );

    @Query("SELECT pa FROM ParticipantAnswer pa " +
            "JOIN pa.participant p " +
            "WHERE pa.participant.id = :participantId AND p.session.id = :sessionId " +
            "ORDER BY pa.answeredAt ASC")
    List<ParticipantAnswer> findByParticipantIdAndParticipantSessionIdOrderByAnsweredAtAsc(
            @Param("participantId") String participantId,
            @Param("sessionId") String sessionId);

}