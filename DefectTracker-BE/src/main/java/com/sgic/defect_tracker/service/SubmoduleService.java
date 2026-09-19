package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.SubmoduleRequestDTO;
import com.sgic.defect_tracker.dtos.response.SubmoduleResponseDTO;

import java.util.List;

public interface SubmoduleService {

    SubmoduleResponseDTO createSubModule(
            SubmoduleRequestDTO requestDTO
    );

    SubmoduleResponseDTO updateSubmodule(
            Long id,
            SubmoduleRequestDTO requestDTO
    );

    void deleteSubmodule(Long id);

    List<SubmoduleResponseDTO> getAllSubmodulesByModule(
            Long moduleId
    );

    SubmoduleResponseDTO getbyID(Long id);
}