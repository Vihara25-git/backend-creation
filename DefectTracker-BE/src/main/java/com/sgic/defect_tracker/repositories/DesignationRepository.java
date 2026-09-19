package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    @Query("""
        SELECT COUNT(d) > 0
        FROM Designation d
        WHERE LOWER(REPLACE(d.designationName, ' ', ''))
            = LOWER(REPLACE(:designationName, ' ', ''))
    """)
    boolean existsByNormalizedDesignationName(
            @Param("designationName") String designationName
    );

    List<Designation> findByDesignationIdNot(Long designationId);

    Page<Designation> findByDesignationIdNot(
            Long designationId,
            Pageable pageable
    );
}
