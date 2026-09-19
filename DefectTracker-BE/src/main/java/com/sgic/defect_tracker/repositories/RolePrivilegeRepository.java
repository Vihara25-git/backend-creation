package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.RolePrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePrivilegeRepository
        extends JpaRepository<RolePrivilege, Long> {

    // All privileges currently assigned to a role.
    // Row exists = assigned, no row = not assigned (no status flag).
    List<RolePrivilege> findByRole_RoleId(Long roleId);

    Optional<RolePrivilege> findByRole_RoleIdAndTemplate_Id(
            Long roleId,
            Long templateId
    );

    void deleteByRole_RoleIdAndTemplate_Id(
            Long roleId,
            Long templateId
    );

    void deleteByRole_RoleId(Long roleId);

    void deleteByTemplate_Id(Long templateId);
}