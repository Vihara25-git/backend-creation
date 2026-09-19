package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.EmployeePermissionUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.request.PermissionAssignmentChangeDTO;
import com.sgic.defect_tracker.dtos.response.EmployeePermissionModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModulePermissionResponseDTO;
import com.sgic.defect_tracker.dtos.response.RolePermissionResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.PrivilegeAssignmentService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivilegeAssignmentController {


    private final PrivilegeAssignmentService privilegeAssignmentService;

    // GET /api/v1/permission
    @GetMapping(EndpointBundle.PERMISSION)
    public ResponseEntity<ResponseWrapper<List<ModulePermissionResponseDTO>>> getAllPermissions() {

        List<ModulePermissionResponseDTO> response =
                privilegeAssignmentService.getAllPermissionsGroupedByModule();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Retrieved Successfully.",
                        response
                )
        );
    }

    // GET /api/v1/assign-permission/matrix/{roleId}
    @GetMapping(EndpointBundle.ASSIGN_PERMISSION_MATRIX_ID)
    public ResponseEntity<ResponseWrapper<RolePermissionResponseDTO>> getRolePermissions(
            @PathVariable Long roleId) {

        RolePermissionResponseDTO response =
                privilegeAssignmentService.getRolePermissions(roleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Retrieved Sucessfully",
                        response
                )
        );
    }

    // POST /api/v1/assign-permission/matrix/{roleId}  (create/assign)
    @PostMapping(EndpointBundle.ASSIGN_PERMISSION_MATRIX_ID)
    public ResponseEntity<ResponseWrapper<RolePermissionResponseDTO>> assignRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<PermissionAssignmentChangeDTO> changes) {

        RolePermissionResponseDTO response =
                privilegeAssignmentService.assignRolePermissions(roleId, changes);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "Role permissions assigned successfully.",
                        response
                )
        );
    }

    // GET /api/v1/employee/{employeeId}/permission
    @GetMapping(EndpointBundle.EMPLOYEE_PERMISSION)
    public ResponseEntity<ResponseWrapper<List<EmployeePermissionModuleResponseDTO>>> getEmployeePermissions(
            @PathVariable Long employeeId) {

        List<EmployeePermissionModuleResponseDTO> response =
                privilegeAssignmentService.getEmployeePermissions(employeeId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Retrieved Successfully.",
                        response
                )
        );
    }

    // POST /api/v1/employee/{employeeId}/permission  (create/assign)
    @PostMapping(EndpointBundle.EMPLOYEE_PERMISSION)
    public ResponseEntity<ResponseWrapper<List<EmployeePermissionModuleResponseDTO>>> assignEmployeePermissions(
            @PathVariable Long employeeId,
            @RequestBody EmployeePermissionUpdateRequestDTO request) {

        List<EmployeePermissionModuleResponseDTO> response =
                privilegeAssignmentService.assignEmployeePermissions(employeeId, request);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "Permissions assigned successfully.",
                        response
                )
        );
    }
}