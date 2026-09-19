package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ProjectKloc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectKlocRepository extends JpaRepository<ProjectKloc, Long> {

    Optional<ProjectKloc> findByProjectDetails_ProjectId(Long projectId);
}