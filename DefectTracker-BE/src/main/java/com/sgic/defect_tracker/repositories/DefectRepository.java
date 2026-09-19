package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.entities.ProjectDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.entities.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.Optional;
import com.sgic.defect_tracker.entities.Defect;
import java.util.Optional;
import com.sgic.defect_tracker.entities.Defect;
import java.time.Instant;
@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> , JpaSpecificationExecutor<Defect> {

    Page<Defect> findByProjectDetailsProjectId(Long proId,Pageable pageable);


    List<Defect> findByProjectDetailsProjectIdAndReleaseViewsReleaseId(Long projectId,Long releaseId);

    List<Defect> findByStatusType_statusTypeId(Long statusTypeId);
   // List<Defect> findByStatusType_StatusTypeId(Long statusTypeId);

    boolean existsByStatusType_StatusTypeId(Long id);

    List<Defect> findBySeverity_SeverityId(Long severityId);

    List<Defect> findByPriority_PriorityId(Long priorityId);
    //mithun-dont delete the following method
    List<Defect> findByProjectDetailsProjectId(Long projectId);
    List<Defect> findByDefectType_defectTypeId(Long defectTypeId);
    boolean existsByBriefDescriptionIgnoreCase(String briefDescription);
    boolean existsByBriefDescriptionIgnoreCaseAndDefectIdNot(
        String briefDescription,
        Long defectId
    );
    boolean existsByTestCase_TestCaseId(Long testCaseId);
    Optional<Defect> findByTestCase_TestCaseId(Long testCaseId);




    /// ////// add by remark ratio ///////////////////////
    @Query("""
        SELECT d.statusType.statusType, COUNT(d)
        FROM Defect d
        WHERE d.projectDetails.projectId = :projectId
        GROUP BY d.statusType.statusType
    """)
    List<Object[]> countDefectsByProjectAndStatusType(
            @Param("projectId") Long projectId
    );
    ///////////////////////////////////////////////////////
 // severity index
    @Query("""
    SELECT d.severity.severityName, COUNT(d)
    FROM Defect d
    WHERE d.projectDetails.projectId = :projectId
    GROUP BY d.severity.severityName
""")
    List<Object[]> countDefectsByProjectAndSeverity(
            @Param("projectId") Long projectId
    );

    //project
    boolean existsByAssignTo_EmpIdAndProjectDetails_ProjectId(Long empId, Long projectId);

    //submodule
    boolean existsByAssignTo_EmpIdAndSubModule_SubModuleId(Long empId, Long subModuleId);

    //module
    boolean existsByAssignTo_EmpIdAndModule_ModuleId(Long empId, Long moduleId);

    // ================================
    // Dashboard - Total Defects
    // ================================

    @Query("""
        SELECT COUNT(d)
        FROM Defect d
        WHERE d.projectDetails.projectId = :projectId
    """)
    long countDefectsByProject(
            @Param("projectId") Long projectId
    );

    @Query("""
        SELECT m.moduleName AS moduleName, COUNT(d) AS defectCount
        FROM Defect d
        JOIN d.module m
        WHERE d.projectDetails.projectId = :projectId
        GROUP BY m.moduleName
        ORDER BY defectCount DESC
    """)
    List<Object[]> countDefectsByModule(@Param("projectId") Long projectId);

    /// ////////// vijayatharsan /////////////
    @Query("""
    SELECT d.defectType.defectTypeName, COUNT(d.defectId)
    FROM Defect d
    WHERE d.projectDetails.projectId = :projectId
    GROUP BY d.defectType.defectTypeName
    ORDER BY COUNT(d.defectId) DESC
""")
    List<Object[]> getDefectCountByType(@Param("projectId") Long projectId);

    /////////////////////////////////////////////////////////////////

    // Dashboard - Time to Find Defects ////////////
    @Query("""
        SELECT d.createdAt
        FROM Defect d
        JOIN d.releaseViews r
        WHERE d.projectDetails.projectId = :projectId
          AND r.releaseId = :releaseId
    """)
    List<Instant> findDefectFoundTimestampsByProjectAndRelease(
            @Param("projectId") Long projectId,
            @Param("releaseId") Long releaseId
    );
    ///////////////////////////////////////////////////

    //dashboard kloc (count defect)
    // Import defects - duplicate check
    boolean existsByProjectDetails_ProjectIdAndModule_ModuleIdAndSubModule_SubModuleIdAndBriefDescriptionIgnoreCaseAndSteps(
            Long projectId,
            Long moduleId,
            Long subModuleId,
            String briefDescription,
            String steps
    );
    boolean existsBySubModuleAndAssignTo(
            SubModule subModule,
            Employee employee
    );
    long countByProjectDetails_ProjectId(Long projectId);

    //testcase update -> change defect
    List<Defect> findAllByTestCase_TestCaseId(Long testCaseId);

    @Query("""
    SELECT COALESCE(MAX(d.projectDefectNumber), 0)
    FROM Defect d
    WHERE d.projectDetails.projectId = :projectId
""")
    Long findMaxProjectDefectNumber(
            @Param("projectId") Long projectId
    );



//for not allow to complete the project when the defects are active-handle by rakavi
@Query("""
    SELECT COUNT(d)
    FROM Defect d
    WHERE d.projectDetails.projectId = :projectId
    AND UPPER(d.statusType.statusType) NOT IN ('CLOSED')
""")
long countOpenDefectsByProjectId(@Param("projectId") Long projectId);


// validate defect for bench deallocate
boolean existsByAssignTo_EmpIdAndProjectDetails_ProjectIdAndStatusType_StatusNameNotIn(
        Long empId,
        Long projectId,
        List<String> statusNames
);
}

