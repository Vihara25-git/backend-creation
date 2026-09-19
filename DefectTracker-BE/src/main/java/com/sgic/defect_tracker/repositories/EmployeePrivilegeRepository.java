package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.EmployeePrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePrivilegeRepository
        extends JpaRepository<EmployeePrivilege, Long> {

    // Privileges directly assigned to this employee.
    // Only "assigned" rows are ever saved here - unassigned = no row at all.
    List<EmployeePrivilege> findByEmployee_EmpId(Long employeeId);

    void deleteByEmployee_EmpId(Long employeeId);

    void deleteByTemplate_Id(Long templateId);
}