package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.ProjectKloc;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import com.sgic.defect_tracker.repositories.ProjectKlocRepository;
import com.sgic.defect_tracker.service.ProjectKlocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectKlocServiceImpl implements ProjectKlocService {

    private final ProjectKlocRepository projectKlocRepository;
    private final ProjectDetailsRepository projectDetailsRepository;

    @Override
    @Transactional(readOnly = true)
    public Double getKloc(Long projectId) {

        return projectKlocRepository
                .findByProjectDetails_ProjectId(projectId)
                .map(ProjectKloc::getKiloOfCode)
                .orElse(0.1);
    }
    @Override
    @Transactional
    public Double createKloc(Long projectId, Double kloc) {
        return updateKloc(projectId, kloc);
    }

    @Override
    @Transactional
    public Double updateKloc(Long projectId, Double kloc) {

        if (kloc == null || kloc < 0.1) {
            throw new IllegalArgumentException(
                    "KLOC must be greater than or equal to 0.1"
            );
        }

        ProjectDetails project = projectDetailsRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Project not found with id: " + projectId
                        ));

        ProjectKloc projectKloc = projectKlocRepository
                .findByProjectDetails_ProjectId(projectId)
                .orElseGet(ProjectKloc::new);

        projectKloc.setProjectDetails(project);
        projectKloc.setKiloOfCode(kloc);

        ProjectKloc saved = projectKlocRepository.save(projectKloc);

        return saved.getKiloOfCode();
    }

    @Override
    @Transactional
    public void deleteKloc(Long projectId) {
        projectKlocRepository
                .findByProjectDetails_ProjectId(projectId)
                .ifPresent(projectKlocRepository::delete);
    }
//    @Override
//    @Transactional
//    public Double updateKloc(Long projectId, Double kloc) {
//
//        if (kloc == null || kloc < 0) {
//            throw new IllegalArgumentException("KLOC must be greater than or equal to 0");
//        }
//
//        ProjectDetails project = projectDetailsRepository
//                .findById(projectId)
//                .orElseThrow(() ->
//                        new RuntimeException("Project not found with id: " + projectId));
//
//        ProjectKloc projectKloc = projectKlocRepository
//                .findByProjectDetails_ProjectId(projectId)
//                .orElseGet(ProjectKloc::new);
//
//        projectKloc.setProjectDetails(project);
//        projectKloc.setKiloOfCode(kloc);
//
//        ProjectKloc saved = projectKlocRepository.save(projectKloc);
//
//        return saved.getKiloOfCode();
//    }
}