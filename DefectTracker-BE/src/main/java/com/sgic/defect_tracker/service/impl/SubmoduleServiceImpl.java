package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.SubmoduleRequestDTO;
import com.sgic.defect_tracker.dtos.response.SubmoduleResponseDTO;
import com.sgic.defect_tracker.entities.Module;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.exceptionHandlers.IllegalArgumentException;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.SubmoduleMapper;
import com.sgic.defect_tracker.repositories.ModuleRepositories;
import com.sgic.defect_tracker.repositories.SubmoduleRepository;
import com.sgic.defect_tracker.service.SubmoduleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SubmoduleServiceImpl implements SubmoduleService {

    private final SubmoduleRepository submoduleRepository;
    private final SubmoduleMapper submoduleMapper;
    private final ModuleRepositories moduleRepository;

    public SubmoduleServiceImpl(
            SubmoduleRepository submoduleRepository,
            SubmoduleMapper submoduleMapper,
            ModuleRepositories moduleRepository
    ) {
        this.submoduleRepository = submoduleRepository;
        this.submoduleMapper = submoduleMapper;
        this.moduleRepository = moduleRepository;
    }
/// /////////////////
    @Override
    public SubmoduleResponseDTO createSubModule(
            SubmoduleRequestDTO requestDTO
    ) {

        if (requestDTO.getSubModuleName() == null ||
                requestDTO.getSubModuleName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Submodule name is required"
            );
        }

        String submoduleName =
                requestDTO.getSubModuleName().trim();

        if (!submoduleName.matches("^(?=.*[a-zA-Z])[a-zA-Z0-9 ]+$")) {
            throw new IllegalArgumentException(
                    "Submodule name must contain at least one alphabet and can contain numbers and spaces only"
            );
        }
/////////////////
        List<SubModule> existingSubmodules =
                submoduleRepository.findByModule_ModuleId(
                        requestDTO.getModuleId()
                );

        String normalizedName =
                submoduleName
                        .replaceAll("\\s+", "")
                        .toLowerCase(java.util.Locale.ROOT);

        boolean exists =
                existingSubmodules.stream()
                        .anyMatch(existing -> {

                            if (existing.getSubModuleName() == null) {
                                return false;
                            }

                            String existingNormalizedName =
                                    existing.getSubModuleName()
                                            .replaceAll("\\s+", "")
                                            .toLowerCase(java.util.Locale.ROOT);

                            return existingNormalizedName.equals(normalizedName);
                        });

        if (exists) {
            throw new IllegalArgumentException(
                    "Submodule already exists with given name"
            );
        }
/// ///////////////
        Module module =
                moduleRepository
                        .findById(requestDTO.getModuleId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Module not found"
                                )
                        );

        // Check project end date
        if (module.getProject() != null &&
                module.getProject().getEndDate() != null &&
                module.getProject().getEndDate()
                        .isBefore(java.time.LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Cannot create submodule. The project has already expired."
            );
        }

        SubModule subModule =
                submoduleMapper.toEntity(requestDTO);

        subModule.setSubModuleName(submoduleName);
        subModule.setModule(module);

        SubModule savedSubModule =
                submoduleRepository.save(subModule);

        return submoduleMapper.toDto(savedSubModule);
    }


    @Override
    public SubmoduleResponseDTO updateSubmodule(
            Long id,
            SubmoduleRequestDTO requestDTO
    ) {

        SubModule subModule =
                submoduleRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Submodule not found"
                                )
                        );

        if (requestDTO.getSubModuleName() == null ||
                requestDTO.getSubModuleName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Submodule name is required"
            );
        }

        String newName =
                requestDTO.getSubModuleName().trim();

        // Check duplicate name - CASE INSENSITIVE
        boolean duplicate =
                submoduleRepository
                        .existsBySubModuleNameIgnoreCaseAndModule_ModuleIdAndSubModuleIdNot(
                                newName,
                                requestDTO.getModuleId(),
                                id
                        );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "Submodule already exists with given name"
            );
        }

        // Compare name - CASE INSENSITIVE
        boolean sameName =
                subModule.getSubModuleName() != null &&
                        subModule.getSubModuleName()
                                .trim()
                                .equals(newName);

        boolean sameDeveloper =
                Objects.equals(
                        subModule.getSubmoduleDevId(),
                        requestDTO.getSubmoduleDevId()
                );

        if (sameName && sameDeveloper) {
            throw new IllegalArgumentException(
                    "No changes detected. " +
                            "Submodule is already up to date."
            );
        }

        requestDTO.setSubModuleName(newName);

        submoduleMapper.updateEntityFromDto(
                requestDTO,
                subModule
        );

        SubModule updatedSubModule =
                submoduleRepository.save(subModule);

        return submoduleMapper.toDto(updatedSubModule);
    }

    @Override
    public void deleteSubmodule(Long id) {

        if (!submoduleRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Submodule not found with id: " + id
            );
        }

        submoduleRepository.deleteById(id);
    }

    @Override
    public List<SubmoduleResponseDTO> getAllSubmodulesByModule(
            Long moduleId
    ) {

        List<SubModule> subModules =
                submoduleRepository.findByModule_ModuleId(moduleId);

        return subModules.stream()
                .map(submoduleMapper::toDto)
                .toList();
    }

    @Override
    public SubmoduleResponseDTO getbyID(Long id) {

        SubModule subModule =
                submoduleRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No Submodule Found"
                                )
                        );

        return submoduleMapper.toDto(subModule);
    }
}