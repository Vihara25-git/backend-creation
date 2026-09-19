package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.WorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkFlow, Long> {

    List<WorkFlow> findByStatusTypeId1(Long statusTypeId1);

    boolean existsByStatusTypeId1AndStatusTypeId2(Long statusTypeId1, Long statusTypeId2);
}