package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ReleaseTestCase;
import com.sgic.defect_tracker.entities.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface ReleaseTestCaseRepository
        extends JpaRepository<ReleaseTestCase, Long> {

    boolean existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(
            Long releaseId,
            Long testCaseId
    );

    Optional<ReleaseTestCase> findByReleaseView_ReleaseIdAndReleaseTestCaseId(
            Long releaseId,
            Long releaseTestCaseId
    );

    List<ReleaseTestCase> findByReleaseView_ReleaseId(
            Long releaseId
    );

    Optional<ReleaseTestCase> findByReleaseView_ReleaseIdAndTestCase_TestCaseId(
            Long releaseId,
            Long testCaseId
    );

    List<ReleaseTestCase>
    findByReleaseView_ReleaseIdAndTestCase_Module_ModuleId(
            Long releaseId,
            Long moduleId
    );

    List<ReleaseTestCase>
    findByReleaseView_ReleaseIdAndTestCase_Module_ModuleIdAndTestCase_SubModule_SubModuleId(
            Long releaseId,
            Long moduleId,
            Long subModuleId
    );

    long countByReleaseView_ReleaseId(Long releaseId);

    // bench testcase for bench deallocate
// Check whether employee has any incomplete test case
    @Query("""
        SELECT CASE WHEN COUNT(rtc) > 0 THEN true ELSE false END
        FROM ReleaseTestCase rtc
        JOIN rtc.benchAllocation ba
        WHERE ba.employee.empId = :empId
          AND ba.projectDetails.projectId = :projectId
          AND (rtc.passOrFail IS NULL OR TRIM(rtc.passOrFail) = '')
    """)
    boolean existsPendingTestCases(
            @Param("empId") Long empId,
            @Param("projectId") Long projectId
    );

    //Filter Allocate Testcase for Release
    @Query("""
    SELECT rtc.testCase.testCaseId
    FROM ReleaseTestCase rtc
    WHERE rtc.releaseView.releaseId = :releaseId
    """)
    List<Long> findAllocatedTestCaseIdsByReleaseId(
            @Param("releaseId") Long releaseId
    );

    @Query("""
    SELECT t
    FROM TestCase t
    WHERE t.projectDetails.projectId = :projectId
      AND t.module.moduleId = :moduleId
      AND t.subModule.subModuleId = :subModuleId
      AND NOT EXISTS (
          SELECT rtc
          FROM ReleaseTestCase rtc
          WHERE rtc.releaseView.releaseId = :releaseId
            AND rtc.testCase.testCaseId = t.testCaseId
      )
    ORDER BY t.testCaseId ASC
""")
    List<TestCase> findAvailableTestCasesForRelease(
            @Param("releaseId") Long releaseId,
            @Param("projectId") Long projectId,
            @Param("moduleId") Long moduleId,
            @Param("subModuleId") Long subModuleId
    );

    void deleteByTestCase_TestCaseId(Long testCaseId);
}