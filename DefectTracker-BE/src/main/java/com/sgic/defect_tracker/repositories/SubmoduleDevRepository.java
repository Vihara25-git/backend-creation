package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.entities.SubmoduleDev;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmoduleDevRepository extends JpaRepository <SubmoduleDev, Long> {

    List<SubmoduleDev> findBySubModule(SubModule subModule);

    boolean existsBySubModuleAndEmployee(
            SubModule subModule,
            Employee employee
    );

    void deleteBySubModuleAndEmployee(
            SubModule subModule,
            Employee employee
    );

    @org.springframework.data.jpa.repository.Query("""
    SELECT COUNT(sd) > 0
    FROM SubmoduleDev sd
    WHERE sd.employee.empId = :empId
      AND sd.subModule.module.project.projectId = :projectId
""")
    boolean existsByEmployee_EmpIdAndProjectId(
            @org.springframework.data.repository.query.Param("empId") Long empId,
            @org.springframework.data.repository.query.Param("projectId") Long projectId
    );

}
