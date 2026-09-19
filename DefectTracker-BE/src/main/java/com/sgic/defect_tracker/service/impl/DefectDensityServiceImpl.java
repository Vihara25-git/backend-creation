package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.response.DefectDensityResponseDto;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.repositories.ProjectKlocRepository;
import com.sgic.defect_tracker.service.DefectDensityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefectDensityServiceImpl implements DefectDensityService {

    private final ProjectKlocRepository projectKlocRepository;
    private final DefectRepository defectRepository;

    @Override
    public DefectDensityResponseDto getDefectDensity(Long projectId) {

        Double kloc = projectKlocRepository
                .findByProjectDetails_ProjectId(projectId)
                .map(projectKloc -> projectKloc.getKiloOfCode())
                .orElse(0.1);

        Long totalDefects =
                defectRepository.countByProjectDetails_ProjectId(projectId);

        Double defectDensity = totalDefects / kloc;

        return new DefectDensityResponseDto(
                kloc,
                totalDefects,
                defectDensity
        );
    }
}