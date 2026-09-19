package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.RoleBasedPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleBasedPreferenceRepository
        extends JpaRepository<RoleBasedPreference, Long> {

    Optional<RoleBasedPreference>
    findByRole_RoleIdAndEmailTemplate_TemplateId(
            Long roleId,
            Long templateId
    );

    boolean existsByRole_RoleIdAndEmailTemplate_TemplateId(
            Long roleId,
            Long templateId
    );

    List<RoleBasedPreference> findByRole_RoleId(Long roleId);

    List<RoleBasedPreference>
    findByRole_RoleIdAndStatusTrue(Long roleId);

    void deleteByRole_RoleId(Long roleId);

    @Query("""
        SELECT preference
        FROM RoleBasedPreference preference
        JOIN FETCH preference.emailTemplate template
        JOIN FETCH preference.role role
        WHERE preference.role.roleId = :roleId
          AND preference.status = true
          AND template.status = true
        """)
    List<RoleBasedPreference> findEnabledTemplatesByRoleId(
            @Param("roleId") Long roleId
    );

    // Corrected: traverse via emailTemplate.templateId, and only "enabled" preferences
    List<RoleBasedPreference> findByEmailTemplate_TemplateIdAndStatusTrue(Long templateId);
}