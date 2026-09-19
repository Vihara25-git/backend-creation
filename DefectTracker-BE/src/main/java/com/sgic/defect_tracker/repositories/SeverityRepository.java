package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface SeverityRepository extends JpaRepository<Severity,Long> {
    Page<Severity> findAllByOrderByWeightAsc(Pageable pageable);

    //mithun created the following function,cristeena dont dlete it
    List<Severity> findAllByOrderByWeightAsc();

    boolean existsByWeight(Integer weight);
    boolean existsByWeightAndSeverityIdNot(Integer weight, Long severityId);

    //for import defeccts
    //Severity findBySeverityNameIgnoreCase(String severityName);
    Optional<Severity> findBySeverityNameIgnoreCase(String severityName);

    @Query("""
    SELECT COUNT(s) > 0
    FROM Severity s
    WHERE LOWER(REPLACE(s.severityName, ' ', '')) =
          LOWER(REPLACE(:severityName, ' ', ''))
    """)
    boolean existsByNormalizedSeverityName(
            @Param("severityName") String severityName
    );

    @Query("""
        SELECT COUNT(s) > 0
        FROM Severity s
        WHERE s.severityId <> :severityId
          AND LOWER(REPLACE(s.severityName, ' ', '')) =
              LOWER(REPLACE(:severityName, ' ', ''))
    """)

    boolean existsByNormalizedSeverityNameAndIdNot(
            @Param("severityName") String severityName,
            @Param("severityId") Long severityId
    );
}