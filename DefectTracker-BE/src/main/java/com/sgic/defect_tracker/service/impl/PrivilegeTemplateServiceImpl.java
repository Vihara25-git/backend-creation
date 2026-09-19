package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.PrivilegeTemplateRequestDTO;
import com.sgic.defect_tracker.dtos.response.PrivilegeTemplateResponseDTO;
import com.sgic.defect_tracker.entities.PrivilegeTemplate;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.EmployeePrivilegeRepository;
import com.sgic.defect_tracker.repositories.PrivilegeTemplateRepository;
import com.sgic.defect_tracker.repositories.RolePrivilegeRepository;
import com.sgic.defect_tracker.service.PrivilegeTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivilegeTemplateServiceImpl implements PrivilegeTemplateService {

    private final PrivilegeTemplateRepository privilegeTemplateRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;
    private final EmployeePrivilegeRepository employeePrivilegeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PrivilegeTemplateResponseDTO> getAll() {
        return privilegeTemplateRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrivilegeTemplateResponseDTO getById(Long id) {
        PrivilegeTemplate template = privilegeTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege template not found with id: " + id));
        return mapToResponse(template);
    }

    @Override
    public PrivilegeTemplateResponseDTO create(PrivilegeTemplateRequestDTO request) {
        String type = request.getType().trim();
        String subType = request.getSubType().trim().toUpperCase();

        if (privilegeTemplateRepository.existsByTypeAndSubType(type, subType)) {
            throw new RuntimeException("Privilege with module '" + type + "' and action '" + subType + "' already exists");
        }

        PrivilegeTemplate template = new PrivilegeTemplate();
        template.setType(type);
        template.setSubType(subType);
        template.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        PrivilegeTemplate saved = privilegeTemplateRepository.save(template);
        return mapToResponse(saved);
    }

    @Override
    public PrivilegeTemplateResponseDTO update(Long id, PrivilegeTemplateRequestDTO request) {
        PrivilegeTemplate template = privilegeTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege template not found with id: " + id));

        String type = request.getType().trim();
        String subType = request.getSubType().trim().toUpperCase();

        if (privilegeTemplateRepository.existsByTypeAndSubTypeAndIdNot(type, subType, id)) {
            throw new RuntimeException("Privilege with module '" + type + "' and action '" + subType + "' already exists");
        }

        template.setType(type);
        template.setSubType(subType);
        template.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        PrivilegeTemplate updated = privilegeTemplateRepository.save(template);
        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        PrivilegeTemplate template = privilegeTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege template not found with id: " + id));

        // Delete any role privilege assignments
        rolePrivilegeRepository.deleteByTemplate_Id(id);

        // Delete any employee privilege assignments
        employeePrivilegeRepository.deleteByTemplate_Id(id);

        // Delete template
        privilegeTemplateRepository.delete(template);
    }

    private PrivilegeTemplateResponseDTO mapToResponse(PrivilegeTemplate template) {
        return PrivilegeTemplateResponseDTO.builder()
                .id(template.getId())
                .type(template.getType())
                .subType(template.getSubType())
                .description(template.getDescription())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
