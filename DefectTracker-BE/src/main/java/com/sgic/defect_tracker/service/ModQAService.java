package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModuleEmployeeResponseDTO;

import java.util.List;

public interface ModQAService {

    ModuleEmployeeResponseDTO assignEmployee(Long moduleId, Long employeeId);

    List<ModuleEmployeeResponseDTO> getAssignedEmployees(Long moduleId);

    void deallocateEmployee(Long moduleId, Long employeeId);

    List<BenchAllocationResponseDTO> getAvailableQAs(Long moduleId);
}