package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ModQA;
import com.sgic.defect_tracker.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModQARepository extends JpaRepository<ModQA, Long> {

    List<ModQA> findByModule(Module module);

    boolean existsByModuleAndEmployee(
            Module module,
            Employee employee
    );

    void deleteByModuleAndEmployee(
            Module module,
            Employee employee
    );

// check condition for deallocate bench allocation
    boolean existsByEmployee_EmpIdAndModule_Project_ProjectId(
            Long empId,
            Long projectId
    );
}