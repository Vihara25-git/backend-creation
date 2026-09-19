package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTypeRepository
        extends JpaRepository<StatusType, Long> {

    // CREATE
    boolean existsByStatusNameIgnoreCase(String statusName);

    boolean existsByStatusTypeIgnoreCase(String statusType);

    boolean existsByColorCodeIgnoreCase(String colorCode);

    // UPDATE
    boolean existsByStatusNameIgnoreCaseAndStatusTypeIdNot(
            String statusName,
            Long statusTypeId
    );

    boolean existsByStatusTypeIgnoreCaseAndStatusTypeIdNot(
            String statusType,
            Long statusTypeId
    );

    boolean existsByColorCodeIgnoreCaseAndStatusTypeIdNot(
            String colorCode,
            Long statusTypeId
    );

    //for import defects
    //StatusType findByStatusNameIgnoreCase(String statusName);
    Optional<StatusType> findByStatusNameIgnoreCase(String statusName);
}