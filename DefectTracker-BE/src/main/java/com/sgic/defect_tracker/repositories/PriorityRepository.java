package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {
    boolean existsByPriorityName(String priorityName);
    Priority findByPriorityNameIgnoreCase(String priorityName);
    Priority findByColorCodeIgnoreCase(String colorCode);
}
