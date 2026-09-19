package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.SubmoduleDevRequestDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.entities.SubmoduleDev;
import com.sgic.defect_tracker.service.SubmoduleDevService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class SubmoduleDevController {

    private final SubmoduleDevService submoduleDevService;

    // Assign developer
    @PostMapping("module/{moduleId}/sub-module/{subModuleId}/employee")
    public ResponseEntity<SubmoduleDev> assignDeveloper(
            @PathVariable Long moduleId,
            @PathVariable Long subModuleId,
            @RequestBody SubmoduleDevRequestDTO requestDTO
    ) {

        SubmoduleDev response =
                submoduleDevService.assignDeveloper(
                        subModuleId,
                        requestDTO.getEmployeeId()
                );

        return ResponseEntity.ok(response);
    }

    // Get assigned developers
    @GetMapping("module/{moduleId}/sub-module/{subModuleId}/employee")
    public ResponseEntity<List<SubmoduleDev>> getAssignedDevelopers(
            @PathVariable Long moduleId,
            @PathVariable Long subModuleId
    ) {

        return ResponseEntity.ok(
                submoduleDevService.getAssignedDevelopers(
                        subModuleId
                )
        );
    }

    // Deallocate developer
    @DeleteMapping("module/{moduleId}/sub-module/{subModuleId}/employee/{employeeId}")
    public ResponseEntity<Void> deallocateDeveloper(
            @PathVariable Long moduleId,
            @PathVariable Long subModuleId,
            @PathVariable Long employeeId
    ) {

        submoduleDevService.deallocateDeveloper(
                subModuleId,
                employeeId
        );

        return ResponseEntity.noContent().build();
    }
    @GetMapping("module/{moduleId}/sub-module/{subModuleId}/available-employees")
    public ResponseEntity<List<BenchAllocationResponseDTO>> getAvailableDevelopers(
            @PathVariable Long moduleId,
            @PathVariable Long subModuleId
    ) {
        return ResponseEntity.ok(
                submoduleDevService.getAvailableDevelopers(subModuleId)
        );
    }
}