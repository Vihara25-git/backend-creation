package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ProjectDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectDetailsRepository extends JpaRepository<ProjectDetails, Long> {

    boolean existsByProjectNameIgnoreCase(String projectName);

    boolean existsByProjectNameIgnoreCaseAndProjectIdNot(
            String projectName,
            Long projectId
    );

    boolean existsByProjectManager_EmpIdAndStatus(Long empId, String status);

    @Query("""
        SELECT p FROM ProjectDetails p
        WHERE (:status = '' OR p.status = :status)
        AND (
            :search = ''
            OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.projectDescription) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    List<ProjectDetails> filterProjects(
            String status,
            String search
    );
}