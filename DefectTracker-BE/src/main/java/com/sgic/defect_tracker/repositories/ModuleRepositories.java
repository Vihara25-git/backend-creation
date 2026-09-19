package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ModuleRepositories extends JpaRepository <Module,Long> {
    List<Module> findByModuleNameContainingIgnoreCase(String name);
    List<Module> findByProject_ProjectId(Long projectId);
    List<Module> findByProject_ProjectIdAndModuleNameContainingIgnoreCase(Long projectId, String name);
    boolean existsByProject_ProjectIdAndModuleNameIgnoreCase(Long projectId, String moduleName);
    boolean existsByProject_ProjectIdAndModuleNameIgnoreCaseAndModuleIdNot(
            Long projectId,
            String moduleName,
            Long moduleId
    );


    // For Excel import
    Optional<Module> findByProject_ProjectIdAndModuleNameIgnoreCase(
            Long projectId,
            String moduleName
    );

    ///  //////////////////////////////
        @Query("""
        SELECT COUNT(m) > 0
        FROM Module m
        WHERE m.project.projectId = :projectId
        AND LOWER(REPLACE(m.moduleName, ' ', '')) =
            LOWER(REPLACE(:moduleName, ' ', ''))
    """)
        boolean existsByProjectIdAndNormalizedModuleName(
                @Param("projectId") Long projectId,
                @Param("moduleName") String moduleName
        );


    @Query("""
    SELECT COUNT(m) > 0
    FROM Module m
    WHERE m.project.projectId = :projectId
    AND LOWER(REPLACE(m.moduleName, ' ', '')) =
        LOWER(REPLACE(:moduleName, ' ', ''))
    AND m.moduleId <> :moduleId
""")
    boolean existsByProjectIdAndNormalizedModuleNameAndModuleIdNot(
            @Param("projectId") Long projectId,
            @Param("moduleName") String moduleName,
            @Param("moduleId") Long moduleId
    );
    /// /////////////////////////////////

}
