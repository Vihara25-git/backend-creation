package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ReleaseView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReleaseViewRepository extends JpaRepository<ReleaseView, Long> {

    @Query("""
        SELECT MAX(r.releaseDate)
        FROM ReleaseView r
        WHERE r.projectDetails.projectId = :projectId
    """)
    Optional<LocalDate> findLatestReleaseDateByProjectId(
            @Param("projectId") Long projectId
    );


    boolean existsByReleaseNameAndProjectDetails_ProjectId(
            String releaseName,
            Long projectId
    );


    boolean existsByReleaseNameAndProjectDetails_ProjectIdAndReleaseIdNot(
            String releaseName,
            Long projectId,
            Long releaseId
    );


    boolean existsByReleaseVersion(String releaseVersion);


    boolean existsByReleaseDate(LocalDate releaseDate);


    List<ReleaseView> findByProjectDetails_ProjectId(Long projectId);


    // Previous release date
    @Query("""
        SELECT MAX(r.releaseDate)
        FROM ReleaseView r
        WHERE r.projectDetails.projectId = :projectId
        AND r.releaseId < :releaseId
    """)
    Optional<LocalDate> findPreviousReleaseDate(
            @Param("projectId") Long projectId,
            @Param("releaseId") Long releaseId
    );


    // Next release date
    @Query("""
        SELECT MIN(r.releaseDate)
        FROM ReleaseView r
        WHERE r.projectDetails.projectId = :projectId
        AND r.releaseId > :releaseId
    """)
    Optional<LocalDate> findNextReleaseDate(
            @Param("projectId") Long projectId,
            @Param("releaseId") Long releaseId
    );
}