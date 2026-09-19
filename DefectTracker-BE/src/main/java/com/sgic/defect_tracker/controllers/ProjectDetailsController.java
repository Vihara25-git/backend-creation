package com.sgic.defect_tracker.controllers;


import com.sgic.defect_tracker.dtos.request.ProjectDetailsRequestDto;
import com.sgic.defect_tracker.dtos.response.ProjectDetailsResponseDto;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import com.sgic.defect_tracker.service.ProjectDetailsService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import jakarta.persistence.Id;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//mithun-
import com.sgic.defect_tracker.dtos.response.DefectSeverityBreakdownResponseDTO;
import com.sgic.defect_tracker.service.DefectService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(EndpointBundle.PROJECT)
public class ProjectDetailsController {

    @Autowired
    private DefectService defectService;

    @GetMapping("/{projectId}/defect/severity-breakdown")
    public ResponseEntity<ResponseWrapper<DefectSeverityBreakdownResponseDTO>> getDefectSeverityBreakdown(
            @PathVariable Long projectId) {
        DefectSeverityBreakdownResponseDTO data = defectService.getDefectSeverityBreakdown(projectId);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        data
                )
        );
    }

    @Autowired
    private ProjectDetailsService projectDetailsService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<ProjectDetailsResponseDto>> createProject(
             @Valid @RequestBody ProjectDetailsRequestDto projectDetailsRequestDto) {

        ProjectDetailsResponseDto responseDto = projectDetailsService.saveProject(projectDetailsRequestDto);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "Project created successfully",
                        responseDto
                )
        );
    }



    @PutMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<ProjectDetailsResponseDto>> updateProject(
            @PathVariable Long id,
             @Valid @RequestBody ProjectDetailsRequestDto projectDetailsRequestDto) {

        ProjectDetailsResponseDto result = projectDetailsService.updateProject(id, projectDetailsRequestDto);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        "Project updated successfully",
                        result
                )
        );
    }


    @DeleteMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<String>> deleteProject(@PathVariable Long id) {

        projectDetailsService.deleteProject(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.DELETED.getCode(),
                        "Project deleted successfully",
                        null
                )
        );
    }



    @GetMapping
    public List<ProjectDetailsResponseDto> getAllProjects(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        return projectDetailsService.filterProjects(
                status,
                search
        );
    }



    @GetMapping("/{id}")
    public ProjectDetailsResponseDto getProjectById(@PathVariable Long id) {
        return projectDetailsService.getProjectById(id);
    }
}


