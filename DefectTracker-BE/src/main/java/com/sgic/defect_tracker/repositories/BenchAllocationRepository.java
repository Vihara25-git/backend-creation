package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.BenchAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenchAllocationRepository extends JpaRepository<BenchAllocation, Long> , JpaSpecificationExecutor<BenchAllocation> {

   // Optional<BenchAllocation> findByEmpId(Long empId);

    @Query("""
    SELECT COALESCE(SUM(b.availability), 0)
    FROM BenchAllocation b
    WHERE b.employee.empId = :employeeId
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
""")
    Long getAllocatedPercentage(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(b)
    FROM BenchAllocation b
    WHERE b.employee.empId = :employeeId
      AND b.projectDetails.projectId = :projectId
       AND b.role.roleId <> :roleId
      AND b.endDate > CURRENT_TIMESTAMP
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
""")
    Long countDifferentRoleAllocation(
            @Param("employeeId") Long employeeId,
            @Param("projectId") Long projectId,
            @Param("roleId") Long roleId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(b)
    FROM BenchAllocation b
    WHERE b.employee.empId = :empId
      AND b.projectDetails.projectId = :projectId
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
      AND b.benchAllocationId <> :benchAllocationId
""")
    Long countEmployeeProjectOverlapForUpdate(
            @Param("empId") Long empId,
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("benchAllocationId") Long benchAllocationId

    );


    @Query("""
    SELECT COUNT(b)
    FROM BenchAllocation b
    WHERE b.employee.empId = :empId
      AND b.projectDetails.projectId = :projectId
      AND b.endDate > CURRENT_TIMESTAMP
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
""")
    Long countEmployeeProjectOverlap(
            @Param("empId") Long empId,
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate

    );
    @Query("""
        SELECT COUNT(b)
        FROM BenchAllocation b
        WHERE b.employee.empId = :employeeId
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
    """)
    Long countOverlappingAllocations(
            @Param("empId") Long empId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("roleId") Long roleId,
            @Param("projectId") Long projectId
    );

    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND b.endDate >= :now
    ORDER BY b.startDate ASC
""")
    List<BenchAllocation> findByProjectDetails_ProjectId(
            @Param("projectId") Long projectId,
            @Param("now") LocalDateTime now
    );


    Optional<BenchAllocation> findByEmployee_EmpIdAndProjectDetails_ProjectId(Long empId, Long projectId);

    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.employee.empId = :empId
      AND b.projectDetails.projectId = :projectId
      AND b.endDate >= CURRENT_TIMESTAMP
    ORDER BY b.endDate DESC
""")
    List<BenchAllocation> findActiveAllocationsByEmployeeAndProject(
            @Param("empId") Long empId,
            @Param("projectId") Long projectId
    );



    void deleteByProjectDetails_ProjectId(Long projectId);
   // List<BenchAllocation> findByProjectAllocation_ProjectId(Long projectId);

    List<BenchAllocation> findByRole_RoleTypeIgnoreCase(String roleType);

    @Query("""
            SELECT b
            FROM BenchAllocation b
            JOIN FETCH b.employee e
            JOIN FETCH b.role r
            WHERE LOWER(r.roleType) = LOWER(:roleType)
              AND e.isActive = true
              AND b.startDate <= :currentTime
              AND b.endDate >= :currentTime
            """)
    List<BenchAllocation> findAvailableQAAllocations(
            @Param("roleType") String roleType,
            @Param("currentTime") LocalDateTime currentTime
    );

//    @Query("""
//        SELECT b
//        FROM BenchAllocation b
//        JOIN FETCH b.employee e
//        JOIN FETCH b.role r
//        WHERE e.empId = :employeeId
//          AND LOWER(r.roleType) = LOWER(:roleType)
//          AND e.isActive = true
//          AND b.startDate <= :currentTime
//          AND b.endDate >= :currentTime
//        ORDER BY b.startDate DESC
//        """)
//    List<BenchAllocation> findCurrentAvailableQAAllocation(
//            @Param("employeeId") Long employeeId,
//            @Param("roleType") String roleType,
//            @Param("currentTime") LocalDateTime currentTime
//    );

    @Query("""
        SELECT b
        FROM BenchAllocation b
        JOIN FETCH b.employee e
        JOIN FETCH b.role r
        WHERE e.empId = :employeeId
          AND LOWER(r.roleType) = LOWER(:roleType)
          AND e.isActive = true
          AND b.startDate <= :currentTime
          AND b.endDate >= :currentTime
        ORDER BY b.startDate DESC
        """)
    List<BenchAllocation> findCurrentAvailableQAAllocation(
            @Param("employeeId") Long employeeId,
            @Param("roleType") String roleType,
            @Param("currentTime") LocalDateTime currentTime
    );


    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND b.startDate <= :now
      AND b.endDate > :now
      AND (
          UPPER(b.role.roleType) = 'QA_LEAD'
          OR UPPER(b.role.roleType) = 'QA_ENGINEER'
      )
""")
    List<BenchAllocation> findQaAllocationsByProject(
            @Param("projectId") Long projectId,
            @Param("now") LocalDateTime now
    );

    @Query("""
    SELECT COUNT(b) > 0
    FROM BenchAllocation b
    WHERE b.employee.empId = :empId
      AND b.projectDetails IS NOT NULL
      AND b.endDate > CURRENT_TIMESTAMP
""")
    boolean existsActiveAllocation(@Param("empId") Long empId);



    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      
      AND b.endDate > :now
      AND (
          UPPER(b.role.roleType) = 'DEVELOPER'
          OR UPPER(b.role.roleType) = 'SENIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'JUNIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'DEV_LEAD'
      )
""")
    List<BenchAllocation> findDeveloperAllocationsByProject(
            @Param("projectId") Long projectId,
            @Param("now") LocalDateTime now
    );

    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND b.employee.empId = :employeeId
      
      AND b.endDate > :now
      AND (
          UPPER(b.role.roleType) = 'DEVELOPER'
          OR UPPER(b.role.roleType) = 'SENIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'JUNIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'DEV_LEAD'
      )
""")
    List<BenchAllocation> findDeveloperAllocationsByProjectAndEmployee(
            @Param("projectId") Long projectId,
            @Param("employeeId") Long employeeId,
            @Param("now") LocalDateTime now
    );
    @Query("""
    SELECT b
    FROM BenchAllocation b
    JOIN FETCH b.role r
    WHERE b.employee.empId = :empId
      AND b.startDate <= CURRENT_TIMESTAMP
      AND b.endDate >= CURRENT_TIMESTAMP
""")
    List<BenchAllocation> findCurrentAllocationsByEmployeeId(
            @Param("empId") Long empId
    );

    @Query("""
    SELECT b
    FROM BenchAllocation b
    JOIN FETCH b.role r
    WHERE b.employee.empId = :empId
""")
    List<BenchAllocation> findAllAllocationsByEmployeeId(
            @Param("empId") Long empId
    );

    @Query("""
    SELECT COUNT(b)
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND (UPPER(b.role.roleType) = 'PROJECT_MANAGER' OR UPPER(b.role.roleName) = 'PROJECT MANAGER' OR UPPER(b.role.roleName) = 'PROJECT_MANAGER')
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
""")
    Long countProjectManagerOverlap(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
    SELECT COUNT(b)
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND (UPPER(b.role.roleType) = 'PROJECT_MANAGER' OR UPPER(b.role.roleName) = 'PROJECT MANAGER' OR UPPER(b.role.roleName) = 'PROJECT_MANAGER')
      AND b.startDate <= :endDate
      AND b.endDate >= :startDate
      AND b.benchAllocationId <> :benchAllocationId
""")
    Long countProjectManagerOverlapForUpdate(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("benchAllocationId") Long benchAllocationId
    );


    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
    ORDER BY b.startDate DESC
""")
    List<BenchAllocation> findAllAllocationsByProjectId(
            @Param("projectId") Long projectId
    );
    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND b.startDate <= :now
      AND b.endDate > :now
      AND (
          UPPER(b.role.roleType) = 'DEVELOPER'
          OR UPPER(b.role.roleType) = 'SENIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'JUNIOR_DEVELOPER'
          OR UPPER(b.role.roleType) = 'DEV_LEAD'
      )
    ORDER BY b.startDate ASC
""")
    List<BenchAllocation> findCurrentProjectDevelopers(
            @Param("projectId") Long projectId,
            @Param("now") LocalDateTime now
    );

    @Query("""
    SELECT b
    FROM BenchAllocation b
    WHERE b.projectDetails.projectId = :projectId
      AND (UPPER(b.role.roleType) = 'PROJECT_MANAGER' OR UPPER(b.role.roleName) = 'PROJECT MANAGER' OR UPPER(b.role.roleName) = 'PROJECT_MANAGER')
    ORDER BY b.startDate DESC
""")
    List<BenchAllocation> findProjectManagerAllocationsByProjectId(
            @Param("projectId") Long projectId
    );

    // findCurrentAllocationsByEmployeeAndProject
    @Query("""
    SELECT b
    FROM BenchAllocation b
    JOIN FETCH b.role r
    WHERE b.employee.empId = :empId
      AND b.projectDetails.projectId = :projectId
      AND b.startDate <= CURRENT_TIMESTAMP
      AND b.endDate >= CURRENT_TIMESTAMP
""")
    List<BenchAllocation> findCurrentAllocationsByEmployeeAndProject(
            @Param("empId") Long empId,
            @Param("projectId") Long projectId
    );


}