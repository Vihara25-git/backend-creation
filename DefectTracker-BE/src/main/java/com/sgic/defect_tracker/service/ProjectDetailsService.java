package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.ProjectDetailsRequestDto;
import com.sgic.defect_tracker.dtos.response.ProjectDetailsResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProjectDetailsService {

    ProjectDetailsResponseDto saveProject(
            ProjectDetailsRequestDto projectDetailsRequestDto
    );

    ProjectDetailsResponseDto getProjectById(Long id);

    ProjectDetailsResponseDto updateProject(
            Long projectId,
            ProjectDetailsRequestDto dto
    );

    void deleteProject(Long projectId);

    List<ProjectDetailsResponseDto> filterProjects(
            String status,
            String search
    );


}
