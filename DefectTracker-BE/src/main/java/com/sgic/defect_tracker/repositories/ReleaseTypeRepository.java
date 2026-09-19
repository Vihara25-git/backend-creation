package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.ReleaseType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface ReleaseTypeRepository extends JpaRepository<ReleaseType, Long> {
    Optional<ReleaseType> findByReleaseTypeNameIgnoreCase(String releaseTypeName);

    boolean existsByReleaseTypeNameIgnoreCase(String releaseTypeName);
}
