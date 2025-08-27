package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.ParticipantAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParticipantAnswerRepository extends JpaRepository<ParticipantAnswer, String> {

    boolean existsByParticipantIdAndQuestionId (String participantId, String questionId);

    List<ParticipantAnswer> findByParticipantIdOrderByAnsweredAt(String participantId);


    List<ParticipantAnswer> findByParticipantSessionId(String participantSessionId);
    @Query("SELECT pa FROM ParticipantAnswer pa WHERE pa.question.id = :questionId AND pa.participant.session.id = :sessionId")
    List<ParticipantAnswer> findByQuestionIdAndParticipantSessionId(
            @Param("questionId") String questionId,
            @Param("sessionId") String sessionId
    );


}