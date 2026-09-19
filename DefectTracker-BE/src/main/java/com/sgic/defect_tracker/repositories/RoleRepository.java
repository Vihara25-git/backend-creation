package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("""
        SELECT r FROM Role r
        WHERE UPPER(r.roleType) = 'PROJECT_MANAGER'
           OR UPPER(r.roleName) = 'PROJECT MANAGER'
           OR UPPER(r.roleName) = 'PROJECT_MANAGER'
    """)
    List<Role> findProjectManagerRoles();

    //create Role name
    boolean existsByRoleNameIgnoreCase(String roleName);

    //create Role Type
    boolean existsByRoleType(String roleType);

    //update Role name
    boolean existsByRoleNameIgnoreCaseAndRoleIdNot(
            String roleName,
            Long roleId
    );

    //update Role type
    boolean existsByRoleTypeAndRoleIdNot(
            String roleType,
            Long roleId
    );
}