package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.PrivilegeTemplateRequestDTO;
import com.sgic.defect_tracker.dtos.response.PrivilegeTemplateResponseDTO;

import java.util.List;

public interface PrivilegeTemplateService {

    List<PrivilegeTemplateResponseDTO> getAll();

    PrivilegeTemplateResponseDTO getById(Long id);

    PrivilegeTemplateResponseDTO create(PrivilegeTemplateRequestDTO request);

    PrivilegeTemplateResponseDTO update(Long id, PrivilegeTemplateRequestDTO request);

    void delete(Long id);
}
