package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.DefectHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;


public interface DefectHistoryRepository
        extends JpaRepository<DefectHistory, Long> {

    List<DefectHistory> findByDefect_DefectIdOrderByDefectDateDescDefectTimeDesc(
            Long defectId
    );

    void deleteByDefect_DefectId(Long defectId);

    @Query("""
        SELECT dh.defect.defectId, COUNT(dh)
        FROM DefectHistory dh
        WHERE dh.defect.projectDetails.projectId = :projectId
          AND UPPER(dh.defectStatus) = 'REOPENED'
        GROUP BY dh.defect.defectId
        HAVING COUNT(dh) > 1
    """)
    List<Object[]> findDefectsReopenedMultipleTimes(
            @Param("projectId") Long projectId
    );

    // Pie chart - multiple times reopened defects
    @Query("""
        SELECT COUNT(dh.defect.defectId)
        FROM DefectHistory dh
        WHERE dh.defect.projectDetails.projectId = :projectId
          AND UPPER(dh.defectStatus) = 'REOPENED'
        GROUP BY dh.defect.defectId
        HAVING COUNT(dh) > 1
    """)
    List<Long> countDefectsReopenedMultipleTimes(
            @Param("projectId") Long projectId
    );



    // Dashboard - Time to Fix Defects /////////////////////////
    @Query("""
        SELECT dh.defectDate
        FROM DefectHistory dh
        JOIN dh.defect.releaseViews r
        WHERE dh.defect.projectDetails.projectId = :projectId
          AND r.releaseId = :releaseId
          AND UPPER(dh.defectStatus) = 'FIXED'
    """)
    List<LocalDate> findFixedDefectDatesByProjectAndRelease(
            @Param("projectId") Long projectId,
            @Param("releaseId") Long releaseId
    );
    //////////////////////////////////////////////////////////////
}
