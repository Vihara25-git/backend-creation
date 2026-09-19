package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.SubModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmoduleRepository extends JpaRepository<SubModule, Long> {

    boolean existsBySubModuleName(String subModuleName);

    boolean existsBySubModuleIdAndSubModuleNameAndSubmoduleDevIdAndModule_ModuleId(
            Long subModuleId,
            String subModuleName,
            Long submoduleDevId,
            Long moduleId
    );

    Optional<SubModule> findBySubModuleName(String subModuleName);

    List<SubModule> findByModule_ModuleId(Long moduleId);

    boolean existsBySubModuleNameIgnoreCase(
            String subModuleName
    );

    boolean existsBySubModuleNameIgnoreCaseAndSubModuleIdNot(
            String subModuleName,
            Long subModuleId
    );
    boolean existsBySubModuleNameAndSubModuleIdNot(
            String subModuleName,
            Long subModuleId
    );
    boolean existsBySubModuleNameAndModuleModuleId(
            String subModuleName,
            Long moduleId
    );
    /// //////////////////////
    boolean existsBySubModuleNameIgnoreCaseAndModule_ModuleId(
            String subModuleName,
            Long moduleId
    );

    /// ///////////////////////

    //import defects
    Optional<SubModule> findBySubModuleNameIgnoreCaseAndModule_ModuleId(
            String subModuleName,
            Long moduleId
    );

    // For Excel Import testcase
    Optional<SubModule> findByModule_ModuleIdAndSubModuleNameIgnoreCase(
            Long moduleId,
            String subModuleName
    );
    boolean existsBySubModuleNameIgnoreCaseAndModule_ModuleIdAndSubModuleIdNot(
            String subModuleName,
            Long moduleId,
            Long subModuleId
    );
}