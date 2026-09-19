package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.response.CurrentUserPermissionsDTO;
import com.sgic.defect_tracker.dtos.response.ProjectInfoDTO;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.PrivilegeTemplate;
import com.sgic.defect_tracker.enums.LoginType;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmployeePrivilegeRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.RolePrivilegeRepository;
import com.sgic.defect_tracker.service.CurrentUserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CurrentUserPermissionServiceImpl
        implements CurrentUserPermissionService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePrivilegeRepository employeePrivilegeRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;
    private final BenchAllocationRepository benchAllocationRepository;


    @Override
    public CurrentUserPermissionsDTO getMyPermissions(String email) {

        Employee employee = getEmployeeByEmail(email);

        Set<String> permissions = new HashSet<>();

        if (Boolean.TRUE.equals(employee.getCustomPermissions())) {
            // User has custom permissions assigned: use ONLY direct permissions
            addDirectPermissions(employee.getEmpId(), permissions);
        } else {
            // Inherit permissions from assigned roles
            if (employee.getRole() != null) {
                addRolePermissions(
                        employee.getRole().getRoleId(),
                        permissions
                );
            }

            benchAllocationRepository
                    .findCurrentAllocationsByEmployeeId(employee.getEmpId())
                    .forEach(allocation -> {

                        if (allocation.getRole() != null) {

                            addRolePermissions(
                                    allocation.getRole().getRoleId(),
                                    permissions
                            );
                        }
                    });
        }

        return buildResponse(employee, permissions);
    }


    @Override
    public CurrentUserPermissionsDTO getMyProjectPermissions(
            String email,
            Long projectId
    ) {

        Employee employee = getEmployeeByEmail(email);

        Set<String> permissions = new HashSet<>();

        if (Boolean.TRUE.equals(employee.getCustomPermissions())) {
            // User has custom permissions assigned: use ONLY direct permissions
            addDirectPermissions(employee.getEmpId(), permissions);
        } else {
            // Inherit permissions from assigned roles
            if (employee.getRole() != null) {
                addRolePermissions(
                        employee.getRole().getRoleId(),
                        permissions
                );
            }

            benchAllocationRepository
                    .findCurrentAllocationsByEmployeeAndProject(
                            employee.getEmpId(),
                            projectId
                    )
                    .forEach(allocation -> {

                        if (allocation.getRole() != null) {

                            addRolePermissions(
                                    allocation.getRole().getRoleId(),
                                    permissions
                            );
                        }
                    });
        }

        return buildResponse(employee, permissions);
    }


    private Employee getEmployeeByEmail(String email) {

        return employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );
    }


    private void addDirectPermissions(
            Long empId,
            Set<String> permissions
    ) {

        employeePrivilegeRepository
                .findByEmployee_EmpId(empId)
                .forEach(employeePrivilege ->
                        permissions.add(
                                buildPermissionCode(
                                        employeePrivilege.getTemplate()
                                )
                        )
                );
    }


    private void addRolePermissions(
            Long roleId,
            Set<String> permissions
    ) {

        rolePrivilegeRepository
                .findByRole_RoleId(roleId)
                .forEach(rolePrivilege ->
                        permissions.add(
                                buildPermissionCode(
                                        rolePrivilege.getTemplate()
                                )
                        )
                );
    }


    private CurrentUserPermissionsDTO buildResponse(
            Employee employee,
            Set<String> permissions
    ) {

        boolean isAdmin =
                employee.getLoginType() == LoginType.ADMIN && !Boolean.TRUE.equals(employee.getCustomPermissions());

        return new CurrentUserPermissionsDTO(
                employee.getEmpId(),
                employee.getEmail(),
                isAdmin,
                permissions
        );
    }


    private String buildPermissionCode(PrivilegeTemplate t) {

        String type = t.getType()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();

        return type + "_" + t.getSubType().toUpperCase();
    }


    @Override
    public List<ProjectInfoDTO> getMyProjects(String email) {

        Employee employee = getEmployeeByEmail(email);

        return benchAllocationRepository
                .findCurrentAllocationsByEmployeeId(employee.getEmpId())
                .stream()
                .filter(allocation -> allocation.getProjectDetails() != null)
                .map(allocation -> new ProjectInfoDTO(
                        allocation.getProjectDetails().getProjectId(),
                        allocation.getProjectDetails().getProjectName(),
                        allocation.getRole() != null
                                ? allocation.getRole().getRoleName()
                                : null
                ))
                .toList();
    }
}