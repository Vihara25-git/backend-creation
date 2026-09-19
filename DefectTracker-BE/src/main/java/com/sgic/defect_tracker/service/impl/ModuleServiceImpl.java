package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.ModuleRequestDto;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModuleResponseDto;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.mapper.ModuleMapper;
import com.sgic.defect_tracker.entities.Module;
//import com.sgic.defect_tracker.mapper.ModuleMapper;
import com.sgic.defect_tracker.repositories.ModuleRepositories;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import com.sgic.defect_tracker.service.ModuleService;
import com.sgic.defect_tracker.utils.ValidationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepositories moduleRepositories;

    @Autowired
    private ProjectDetailsRepository projectDetailsRepository;

    @Autowired
    private ModuleMapper moduleMapper;


    private void validateProjectStatus(ProjectDetails project) {
        String status = project.getStatus();

        if ("COMPLETED".equalsIgnoreCase(status)) {

            throw new IllegalStateException(
                    "Cannot modify modules because the project is " + status
            );
        }
    }

    @Override
    public ModuleResponseDto createModule(Long projectId, ModuleRequestDto moduleRequestDto) {

        ProjectDetails project = projectDetailsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ValidationMessages.PROJECTID_NOT));

        validateProjectStatus(project);

        if (moduleRepositories.existsByProjectIdAndNormalizedModuleName(
                projectId,
                moduleRequestDto.getModuleName().trim())) {
            throw new IllegalArgumentException(
                    ValidationMessages.DUPLICATE_ENTRY);
        }

        Module module = moduleMapper.toEntity(moduleRequestDto);
        module.setProject(project);

        Module savedModule = moduleRepositories.save(module);

        return moduleMapper.toDto(savedModule);
    }


    // PUT
    @Override
    public ModuleResponseDto updateModule(
            Long moduleId,
            ModuleRequestDto requestDto) {

        try {

            Module module = moduleRepositories.findById(moduleId)
                    .orElseThrow(() ->
                            new RuntimeException("Module Not Found"));

            Long projectId =
                    module.getProject().getProjectId();

            String name = requestDto.getModuleName();

            // Required validation
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException(
                        "Module name is required."
                );
            }

            // Check spaces at beginning/end
            if (!name.equals(name.trim())) {
                throw new IllegalArgumentException(
                        "Module name must not start or end with a space."
                );
            }

            // Check multiple spaces
            if (name.contains("  ")) {
                throw new IllegalArgumentException(
                        "Module name must contain only single spaces between words."
                );
            }

            // Only letters and single spaces
            if (!name.matches("[A-Za-z]+( [A-Za-z]+)*")) {
                throw new IllegalArgumentException(
                        "Module name must contain only letters and single spaces."
                );
            }

            // Duplicate check
            boolean duplicate =
                    moduleRepositories
                            .existsByProjectIdAndNormalizedModuleNameAndModuleIdNot(
                                    projectId,
                                    name,
                                    moduleId
                            );

            if (duplicate) {
                throw new IllegalArgumentException(
                        "Module name already exists in this project");
            }

            // Update
            moduleMapper.updateModuleFromDto(requestDto, module);

            Module updatedModule =
                    moduleRepositories.save(module);

            return moduleMapper.toDto(updatedModule);

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to Updated Module", e);
        }
    }

    //Delete
    @Override
    public void deleteModule(Long moduleId){
        Module module =moduleRepositories.findById(moduleId)
                .orElseThrow(()->new RuntimeException("Module not found:"+moduleId));

        moduleRepositories.delete(module);
    }

    @Override
    public List<ModuleResponseDto> getAllModule(Long projectId, String name) {
        List<Module> modules ;
        if (name != null && !name.isBlank()) {
            modules = moduleRepositories.findByProject_ProjectIdAndModuleNameContainingIgnoreCase(projectId, name);
        } else {
            modules = moduleRepositories.findByProject_ProjectId(projectId);
        }

        if (modules.isEmpty()) {
            throw new ResourceNotFoundException("No modules found");
        }

        return modules.stream()
                .map(moduleMapper::toDto)
                .toList();
    }

    @Override
    public ModuleResponseDto getModuleById(Long moduleId) {

        Module module = moduleRepositories.findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Module not found"));

        return moduleMapper.toDto(module);
    }


}
