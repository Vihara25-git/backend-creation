package com.sgic.defect_tracker.controllers;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModuleEmployeeResponseDTO;
import com.sgic.defect_tracker.service.ModQAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/module")
@RequiredArgsConstructor
public class ModQAController {

    private final ModQAService modQAService;

    @PostMapping("/{moduleId}/employee/{employeeId}")
    public ResponseEntity<ModuleEmployeeResponseDTO> assignEmployee(
            @PathVariable Long moduleId,
            @PathVariable Long employeeId) {

        ModuleEmployeeResponseDTO response = modQAService.assignEmployee(moduleId, employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{moduleId}/employee")
    public ResponseEntity<List<ModuleEmployeeResponseDTO>> getAssignedEmployees(@PathVariable Long moduleId) {
        return ResponseEntity.ok(modQAService.getAssignedEmployees(moduleId));
    }

    @DeleteMapping("/{moduleId}/employee/{employeeId}")
    public ResponseEntity<Void> deallocateEmployee(
            @PathVariable Long moduleId,
            @PathVariable Long employeeId) {

        modQAService.deallocateEmployee(moduleId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{moduleId}/available-qas")
    public ResponseEntity<List<BenchAllocationResponseDTO>> getAvailableQAs(
            @PathVariable Long moduleId
    ) {
        return ResponseEntity.ok(
                modQAService.getAvailableQAs(moduleId)
        );
    }

}