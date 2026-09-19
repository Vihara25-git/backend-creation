package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.PrivilegeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeTemplateRepository
        extends JpaRepository<PrivilegeTemplate, Long> {
    boolean existsByTypeAndSubType(String type, String subType);

    boolean existsByTypeAndSubTypeAndIdNot(String type, String subType, Long id);
}