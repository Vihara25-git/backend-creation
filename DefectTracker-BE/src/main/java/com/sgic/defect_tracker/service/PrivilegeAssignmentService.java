package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.EmployeePermissionUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.request.PermissionAssignmentChangeDTO;
import com.sgic.defect_tracker.dtos.response.EmployeePermissionModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModulePermissionResponseDTO;
import com.sgic.defect_tracker.dtos.response.RolePermissionResponseDTO;

import java.util.List;
import java.util.Set;

public interface PrivilegeAssignmentService {

    // GET /permission - master catalog grouped by module
    List<ModulePermissionResponseDTO> getAllPermissionsGroupedByModule();

    // GET /assign-permission/matrix/{roleId}
    RolePermissionResponseDTO getRolePermissions(Long roleId);

    // POST /assign-permission/matrix/{roleId}  (create/assign)
    RolePermissionResponseDTO assignRolePermissions(
            Long roleId,
            List<PermissionAssignmentChangeDTO> changes
    );

    // GET /employee/{employeeId}/permission
    List<EmployeePermissionModuleResponseDTO> getEmployeePermissions(Long empId);

    // POST /employee/{employeeId}/permission  (create/assign)
    List<EmployeePermissionModuleResponseDTO> assignEmployeePermissions(
            Long empId,
            EmployeePermissionUpdateRequestDTO request
    );


    Set<String> getEffectivePermissionCodes(Long empId);
}