package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.entities.SubmoduleDev;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.service.SubmoduleDevService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.service.EmailNotificationService;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

@Service
public class SubmoduleDevServiceImpl implements SubmoduleDevService {

    private final SubmoduleDevRepository submoduleDevRepository;
    private final SubmoduleRepository submoduleRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailNotificationService notificationService;
    private final BenchAllocationRepository benchAllocationRepository;
    private final DefectRepository defectRepository;
    public SubmoduleDevServiceImpl(
            SubmoduleDevRepository submoduleDevRepository,
            SubmoduleRepository submoduleRepository,
            EmployeeRepository employeeRepository,
            EmailNotificationService notificationService,
                    BenchAllocationRepository benchAllocationRepository,
            DefectRepository defectRepository
    ) {
        this.submoduleDevRepository = submoduleDevRepository;
        this.submoduleRepository = submoduleRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.benchAllocationRepository = benchAllocationRepository;
        this.defectRepository = defectRepository;
    }

    @Override
    public SubmoduleDev assignDeveloper(
            Long subModuleId,
            Long employeeId
    ) {

        SubModule subModule = submoduleRepository
                .findById(subModuleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submodule not found with id: " + subModuleId
                        )
                );

        Long projectId =
                subModule.getModule()
                        .getProject()
                        .getProjectId();

        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findDeveloperAllocationsByProjectAndEmployee(
                                projectId,
                                employeeId,
                                LocalDateTime.now()
                        );

        if (allocations.isEmpty()) {
            throw new RuntimeException(
                    "Employee is not allocated to this project with a valid developer role"
            );
        }

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        boolean alreadyAssigned =
                submoduleDevRepository
                        .existsBySubModuleAndEmployee(
                                subModule,
                                employee
                        );

        if (alreadyAssigned) {
            throw new RuntimeException(
                    "Developer already assigned to this submodule"
            );
        }

        SubmoduleDev submoduleDev = new SubmoduleDev();

        submoduleDev.setSubModule(subModule);
        submoduleDev.setEmployee(employee);

        SubmoduleDev saved =
                submoduleDevRepository.save(submoduleDev);

        // Send submodule allocation email
        notificationService.sendEmail(
                "SUBMODULE_ALLOCATION",
                employee.getEmail(),
                Map.of(
                        "employeeName",
                        employee.getFirstName()
                                + " "
                                + employee.getLastName(),

                        "projectName",
                        subModule.getModule()
                                .getProject()
                                .getProjectName(),

                        "moduleName",
                        subModule.getModule()
                                .getModuleName(),

                        "subModuleName",
                        subModule.getSubModuleName()
                )
        );

        return saved;
    }

    @Override
    public List<SubmoduleDev> getAssignedDevelopers(
            Long subModuleId
    ) {

        SubModule subModule = submoduleRepository
                .findById(subModuleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submodule not found with id: " + subModuleId
                        )
                );

        return submoduleDevRepository
                .findBySubModule(subModule);
    }

    @Override
    @Transactional
    public void deallocateDeveloper(
            Long subModuleId,
            Long employeeId
    ) {

        SubModule subModule = submoduleRepository
                .findById(subModuleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submodule not found with id: " + subModuleId
                        )
                );

        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );
        boolean hasAssignedDefect =
                defectRepository.existsBySubModuleAndAssignTo(
                        subModule,
                        employee
                );

        if (hasAssignedDefect) {
            throw new IllegalArgumentException(
                    "Developer cannot be deallocated because defects are still assigned to this developer in this submodule"
            );
        }

        boolean exists =
                submoduleDevRepository
                        .existsBySubModuleAndEmployee(
                                subModule,
                                employee
                        );

        if (!exists) {
            throw new ResourceNotFoundException(
                    "Developer is not assigned to this submodule"
            );
        }

        // Check if developer has defects assigned in this submodule
        if (defectRepository.existsByAssignTo_EmpIdAndSubModule_SubModuleId(employeeId, subModuleId)) {
            throw new IllegalArgumentException(
                    "Cannot deallocate developer. Developer is assigned to defects in this submodule. Please reassign the defects before deallocating."
            );
        }

        submoduleDevRepository
                .deleteBySubModuleAndEmployee(
                        subModule,
                        employee
                );
        // Send submodule deallocation email
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName() + " " + employee.getLastName(),

                "projectName",
                subModule.getModule()
                        .getProject()
                        .getProjectName(),

                "moduleName",
                subModule.getModule()
                        .getModuleName(),

                "subModuleName",
                subModule.getSubModuleName()
        );

        notificationService.sendEmail(
                "SUBMODULE_DEALLOCATION",
                employee.getEmail(),
                variables
        );

    }

    @Override
    public List<BenchAllocationResponseDTO> getAvailableDevelopers(
            Long subModuleId
    ) {

        SubModule subModule = submoduleRepository
                .findById(subModuleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Submodule not found with id: " + subModuleId
                        )
                );

        Long projectId = subModule
                .getModule()
                .getProject()
                .getProjectId();

        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findDeveloperAllocationsByProject(
                                projectId,
                                LocalDateTime.now()
                        );

        return allocations.stream()
                .map(allocation -> {

                    BenchAllocationResponseDTO dto =
                            new BenchAllocationResponseDTO();

                    dto.setBenchAllocationId(
                            allocation.getBenchAllocationId()
                    );

                    dto.setEmpId(
                            allocation.getEmployee().getEmpId()
                    );

                    dto.setFirstName(
                            allocation.getEmployee().getFirstName()
                    );

                    dto.setLastName(
                            allocation.getEmployee().getLastName()
                    );

                    dto.setRoleType(
                            allocation.getRole().getRoleType()
                    );

                    return dto;
                })
                .toList();
    }
}