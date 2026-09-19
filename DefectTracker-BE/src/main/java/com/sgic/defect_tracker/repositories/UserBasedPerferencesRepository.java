package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.UserBasedPerferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBasedPerferencesRepository
        extends JpaRepository<UserBasedPerferences, Long> {

    List<UserBasedPerferences> findByEmployee_EmpId(Long empId);
    void deleteByEmployee_EmpId(Long empId);

    Optional<UserBasedPerferences>
    findByEmployee_EmpIdAndEmailTemplate_TemplateId(
            Long empId,
            Long templateId
    );


//    Optional<UserBasedPerferences>
//    existsByEmployee_EmpIdAndEmailTemplate_TemplateId(
//            Long empId,
//            Long templateId
//    );
    boolean existsByEmployee_EmpIdAndEmailTemplate_TemplateId(
            Long empId,
            Long templateId
    );
}