package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.WorkFlowPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkFlowPositionRepository
        extends JpaRepository<WorkFlowPosition, Long> {

    List<WorkFlowPosition> findByStatusTypeStatusTypeId(Long statusTypeId);
}