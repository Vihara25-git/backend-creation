package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepository extends JpaRepository <TestCase, Long> {

    Page<TestCase> findBySubModule_SubModuleIdOrderByTestCaseIdAsc(
            Long subModuleId,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM TestCase t
            WHERE (cast(:description as string) IS NULL
                   OR LOWER(t.description) LIKE LOWER(CONCAT('%', cast(:description as string), '%')))
            AND (:defectTypeId IS NULL
                 OR t.defectType.defectTypeId = :defectTypeId)
            AND (:projectId IS NULL
                 OR t.projectDetails.projectId = :projectId)
            AND (:moduleId IS NULL
                 OR t.module.moduleId = :moduleId)
            AND (:subModuleId IS NULL
                 OR t.subModule.subModuleId = :subModuleId)
           AND (:severityId IS NULL
     OR t.severity.severityId = :severityId)
ORDER BY t.testCaseId ASC
""")
    List<TestCase> filterTestCases(
            @Param("description") String description,
            @Param("defectTypeId") Long defectTypeId,
            @Param("projectId") Long projectId,
            @Param("moduleId") Long moduleId,
            @Param("subModuleId") Long subModuleId,
            @Param("severityId") Long severityId
    );

    //check duplicate while create
    @Query("""
        SELECT COUNT(t) > 0
        FROM TestCase t
        WHERE t.subModule.subModuleId = :subModuleId
        AND LOWER(t.description) = LOWER(:description)
        """)
    boolean existsDuplicate(
            @Param("subModuleId") Long subModuleId,
            @Param("description") String description
    );

    //check duplicate while update
    @Query("""
    SELECT COUNT(t) > 0
    FROM TestCase t
    WHERE t.subModule.subModuleId = :subModuleId
    AND LOWER(t.description) = LOWER(:description)
    AND t.testCaseId <> :testCaseId
    """)
    boolean existsDuplicateForUpdate(
            @Param("subModuleId") Long subModuleId,
            @Param("description") String description,
            @Param("testCaseId") Long testCaseId
    );

    @Query("""
    SELECT t FROM TestCase t
    WHERE t.projectDetails.projectId = :projectId
    ORDER BY t.module.moduleId ASC,
             t.subModule.subModuleId ASC,
             t.testCaseId ASC
    """)
    Page<TestCase> findByProjectId(
            @Param("projectId") Long projectId,
            Pageable pageable
    );

    @Query("""
SELECT t FROM TestCase t
WHERE t.projectDetails.projectId = :projectId
AND t.module.moduleId = :moduleId
ORDER BY t.subModule.subModuleId ASC,
         t.testCaseId ASC
""")
    Page<TestCase> findByProjectIdAndModuleId(
            @Param("projectId") Long projectId,
            @Param("moduleId") Long moduleId,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(MAX(t.testCaseNumber), 0)
    FROM TestCase t
    WHERE t.projectDetails.projectId = :projectId
""")
    Long findMaxTestCaseNumberByProjectId(
            @Param("projectId") Long projectId
    );

    // EXPORT TEST CASES

    List<TestCase> findByProjectDetails_ProjectIdOrderByModule_ModuleIdAscSubModule_SubModuleIdAscTestCaseIdAsc(
            Long projectId
    );

    List<TestCase> findByProjectDetails_ProjectIdAndModule_ModuleIdOrderBySubModule_SubModuleIdAscTestCaseIdAsc(
            Long projectId,
            Long moduleId
    );

    List<TestCase> findByProjectDetails_ProjectIdAndModule_ModuleIdAndSubModule_SubModuleIdOrderByTestCaseIdAsc(
            Long projectId,
            Long moduleId,
            Long subModuleId
    );

    List<TestCase> findBySubModule_SubModuleIdOrderByTestCaseIdAsc(Long subModuleId);

    @Query("""
    SELECT t FROM TestCase t
    WHERE t.projectDetails.projectId = :projectId
    ORDER BY t.testCaseNumber ASC
    """)
    Page<TestCase> findByProjectIdOrderByTestCaseNumber(
            @Param("projectId") Long projectId,
            Pageable pageable
    );

    // Import test case
}
