package kh.edu.cstad.stackquizapi.repository;

import kh.edu.cstad.stackquizapi.domain.SessionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionReportRepository extends JpaRepository<SessionReport, String> {

    @Query("SELECT sr FROM SessionReport sr WHERE sr.session.id = :sessionId")
    Optional<SessionReport> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT sr FROM SessionReport sr WHERE sr.session.host.id = :hostId ORDER BY sr.generatedAt DESC")
    List<SessionReport> findBySessionHostIdOrderByGeneratedAtDesc(@Param("hostId") String hostId);

    @Query("SELECT COUNT(sr) FROM SessionReport sr WHERE sr.session.host.id = :hostId")
    long countByHostId(@Param("hostId") String hostId);
}