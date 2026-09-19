package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.entities.SubmoduleDev;

import java.util.List;

public interface SubmoduleDevService {

    SubmoduleDev assignDeveloper(
            Long subModuleId,
            Long employeeId
    );

    List<SubmoduleDev> getAssignedDevelopers(
            Long subModuleId
    );

    void deallocateDeveloper(
            Long subModuleId,
            Long employeeId
    );

    List<BenchAllocationResponseDTO> getAvailableDevelopers(
            Long subModuleId
    );



}