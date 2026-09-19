package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.EmployeePermissionUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.request.PermissionAssignmentChangeDTO;
import com.sgic.defect_tracker.dtos.response.EmployeePermissionItemResponseDTO;
import com.sgic.defect_tracker.dtos.response.EmployeePermissionModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModulePermissionResponseDTO;
import com.sgic.defect_tracker.dtos.response.PermissionResponseDTO;
import com.sgic.defect_tracker.dtos.response.RolePermissionResponseDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.EmployeePrivilege;
import com.sgic.defect_tracker.entities.PrivilegeTemplate;
import com.sgic.defect_tracker.entities.Role;
import com.sgic.defect_tracker.entities.RolePrivilege;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.service.PrivilegeAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivilegeAssignmentServiceImpl implements PrivilegeAssignmentService {
    private final BenchAllocationRepository benchAllocationRepository;

    private final PrivilegeTemplateRepository privilegeTemplateRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;
    private final EmployeePrivilegeRepository employeePrivilegeRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    // ---------- GET /permission ----------
    // Shows the privilege template catalog, grouped by "type" (module).
    @Override
    public List<ModulePermissionResponseDTO> getAllPermissionsGroupedByModule() {

        List<PrivilegeTemplate> templates = privilegeTemplateRepository.findAll();

        return templates.stream()
                .collect(Collectors.groupingBy(PrivilegeTemplate::getType))
                .entrySet()
                .stream()
                .map(entry -> new ModulePermissionResponseDTO(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(template -> new PermissionResponseDTO(
                                        template.getId(),
                                        template.getSubType(),
                                        template.getDescription()
                                ))
                                .toList()
                ))
                .toList();
    }

    // ---------- GET /assign-permission/matrix/{roleId} ----------
    @Override
    public RolePermissionResponseDTO getRolePermissions(Long roleId) {

        List<Long> permissionIds = rolePrivilegeRepository
                .findByRole_RoleId(roleId)
                .stream()
                .map(rolePrivilege -> rolePrivilege.getTemplate().getId())
                .toList();

        return new RolePermissionResponseDTO(permissionIds, new ArrayList<>());
    }

    // ---------- POST /assign-permission/matrix/{roleId} (create/assign) ----------
    // Only assigned rows are ever saved in role_privilege.
    // isAssigned = true  -> insert a row (if not already there)
    // isAssigned = false -> delete the row (nothing "unassigned" is stored)
    @Override
    public RolePermissionResponseDTO assignRolePermissions(
            Long roleId,
            List<PermissionAssignmentChangeDTO> changes) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        List<String> messages = new ArrayList<>();

        for (PermissionAssignmentChangeDTO change : changes) {

            Long templateId = Long.valueOf(change.getPermissionId());

            boolean alreadyExists = rolePrivilegeRepository
                    .findByRole_RoleIdAndTemplate_Id(roleId, templateId)
                    .isPresent();

            if (Boolean.TRUE.equals(change.getIsAssigned())) {

                if (!alreadyExists) {
                    PrivilegeTemplate template = privilegeTemplateRepository
                            .findById(templateId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Privilege template not found: " + templateId
                            ));

                    RolePrivilege rolePrivilege = new RolePrivilege();
                    rolePrivilege.setRole(role);
                    rolePrivilege.setTemplate(template);

                    rolePrivilegeRepository.save(rolePrivilege);
                    messages.add("Assigned permission " + templateId + " to role " + roleId);
                }

            } else {
                rolePrivilegeRepository
                        .deleteByRole_RoleIdAndTemplate_Id(roleId, templateId);
                rolePrivilegeRepository.flush();
                messages.add("Removed permission " + templateId + " from role " + roleId);
            }
        }

        List<Long> updatedPermissionIds = rolePrivilegeRepository
                .findByRole_RoleId(roleId)
                .stream()
                .map(rolePrivilege -> rolePrivilege.getTemplate().getId())
                .toList();

        return new RolePermissionResponseDTO(updatedPermissionIds, messages);
    }

    // ---------- GET /employee/{employeeId}/permission ----------
    @Override
    public List<EmployeePermissionModuleResponseDTO> getEmployeePermissions(Long empId) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // templates directly assigned to this employee (employee_privilege rows)
        Set<Long> directIds = employeePrivilegeRepository
                .findByEmployee_EmpId(empId)
                .stream()
                .map(employeePrivilege -> employeePrivilege.getTemplate().getId())
                .collect(Collectors.toCollection(HashSet::new));

        // templates inherited via roles
        Set<Long> roleIds = new HashSet<>();
        if (employee.getRole() != null) {
            rolePrivilegeRepository.findByRole_RoleId(employee.getRole().getRoleId())
                    .forEach(rp -> roleIds.add(rp.getTemplate().getId()));
        }

        benchAllocationRepository
                .findCurrentAllocationsByEmployeeId(empId)
                .stream()
                .map(BenchAllocation::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleId)
                .distinct()
                .flatMap(roleId -> rolePrivilegeRepository.findByRole_RoleId(roleId).stream())
                .map(rolePrivilege -> rolePrivilege.getTemplate().getId())
                .forEach(roleIds::add);

        boolean hasCustom = Boolean.TRUE.equals(employee.getCustomPermissions());
        List<PrivilegeTemplate> allTemplates = privilegeTemplateRepository.findAll();

        return allTemplates.stream()
                .collect(Collectors.groupingBy(PrivilegeTemplate::getType))
                .entrySet()
                .stream()
                .map(entry -> new EmployeePermissionModuleResponseDTO(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(template -> {
                                    boolean isDirect = directIds.contains(template.getId());
                                    boolean isInherited = roleIds.contains(template.getId());
                                    boolean isChecked = hasCustom ? isDirect : (isDirect || isInherited);

                                    return new EmployeePermissionItemResponseDTO(
                                            template.getId(),
                                            template.getSubType(),
                                            template.getDescription(),
                                            isChecked,
                                            !hasCustom && isInherited && !isDirect
                                    );
                                })
                                .toList()
                ))
                .toList();
    }

    // ---------- POST /employee/{employeeId}/permission (create/assign) ----------
    // request.getPermissionIds() = the list of CHECKED (assigned) permission ids only.
    // Old direct rows are cleared, then ONLY the assigned ones are inserted back -
    // nothing "unassigned" is ever written to employee_privilege.
    @Override
    public List<EmployeePermissionModuleResponseDTO> assignEmployeePermissions(
            Long empId,
            EmployeePermissionUpdateRequestDTO request) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employeePrivilegeRepository.deleteByEmployee_EmpId(empId);
        employeePrivilegeRepository.flush();

        List<Long> assignedPermissionIds =
                request.getPermissionIds() == null
                        ? List.of()
                        : request.getPermissionIds();

        for (Long templateId : assignedPermissionIds) {

            PrivilegeTemplate template = privilegeTemplateRepository
                    .findById(templateId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Privilege template not found: " + templateId
                    ));

            EmployeePrivilege employeePrivilege = new EmployeePrivilege();
            employeePrivilege.setEmployee(employee);
            employeePrivilege.setTemplate(template);

            employeePrivilegeRepository.save(employeePrivilege);
        }

        employee.setCustomPermissions(true);
        employeeRepository.save(employee);

        return getEmployeePermissions(empId);
    }




    @Override
    public Set<String> getEffectivePermissionCodes(Long empId) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Set<Long> permissionIds = new HashSet<>();

        if (Boolean.TRUE.equals(employee.getCustomPermissions())) {
            employeePrivilegeRepository
                    .findByEmployee_EmpId(empId)
                    .stream()
                    .map(EmployeePrivilege::getTemplate)
                    .map(PrivilegeTemplate::getId)
                    .forEach(permissionIds::add);
        } else {
            if (employee.getRole() != null) {
                rolePrivilegeRepository.findByRole_RoleId(employee.getRole().getRoleId())
                        .stream()
                        .map(RolePrivilege::getTemplate)
                        .map(PrivilegeTemplate::getId)
                        .forEach(permissionIds::add);
            }
            benchAllocationRepository
                    .findCurrentAllocationsByEmployeeId(empId)
                    .stream()
                    .map(BenchAllocation::getRole)
                    .filter(Objects::nonNull)
                    .map(Role::getRoleId)
                    .distinct()
                    .flatMap(roleId ->
                            rolePrivilegeRepository.findByRole_RoleId(roleId).stream()
                    )
                    .map(RolePrivilege::getTemplate)
                    .map(PrivilegeTemplate::getId)
                    .forEach(permissionIds::add);
        }

        return privilegeTemplateRepository.findAll()
                .stream()
                .filter(template -> permissionIds.contains(template.getId()))
                .map(template -> {
                    String type = template.getType().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
                    return type + "_" + template.getSubType().toUpperCase();
                })
                .collect(Collectors.toCollection(HashSet::new));
    }



}