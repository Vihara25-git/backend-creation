package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.ModuleEmployeeResponseDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ModQA;
import com.sgic.defect_tracker.entities.Module;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.ModQARepository;
import com.sgic.defect_tracker.repositories.ModuleRepositories;
import com.sgic.defect_tracker.service.EmailNotificationService;
import com.sgic.defect_tracker.service.ModQAService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgic.defect_tracker.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import com.sgic.defect_tracker.service.EmailNotificationService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModQAServiceImpl implements ModQAService {

    private final ModQARepository modQARepository;
    private final ModuleRepositories moduleRepositories;
    private final EmployeeRepository employeeRepository;
    private final BenchAllocationRepository benchAllocationRepository;
    private final EmailNotificationService notificationService;

    @Autowired
    private DefectRepository defectRepository;

    @Override
    public ModuleEmployeeResponseDTO assignEmployee(Long moduleId, Long employeeId) {

        Module module = moduleRepositories.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Long projectId = module.getProject().getProjectId();

        // Rule 1 + Rule 2 combined: fetch this employee's allocation to this specific project
        BenchAllocation benchAllocation = benchAllocationRepository
                .findByEmployee_EmpIdAndProjectDetails_ProjectId(employeeId, projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "This employee is not currently allocated to this project"));


        if (benchAllocation.getRole() == null ||
                !( "QA_ENGINEER".equalsIgnoreCase(benchAllocation.getRole().getRoleType())
                || "QA_LEAD".equalsIgnoreCase(benchAllocation.getRole().getRoleType()) )) {

            throw new IllegalArgumentException(
                    "Only employees allocated to this project with role type 'QA Engineer' or 'QA Lead' can be assigned"
            );
        }




        boolean alreadyAssigned = modQARepository.existsByModuleAndEmployee(module, employee);
        if (alreadyAssigned) {
            throw new RuntimeException("Employee already assigned to this module");
        }

        ModQA modQA = new ModQA();
        modQA.setModule(module);
        modQA.setEmployee(employee);
        modQA.setIsLeader(true);


        ModQA saved = modQARepository.save(modQA);

// Send module QA allocation email
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName() + " " + employee.getLastName(),

                "projectName",
                module.getProject().getProjectName(),

                "moduleName",
                module.getModuleName()
        );


        return new ModuleEmployeeResponseDTO(
                saved.getModQaId(),
                moduleId,
                employee.getEmpId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                saved.getIsLeader()
        );
    }

    @Override
    public List<ModuleEmployeeResponseDTO> getAssignedEmployees(Long moduleId) {

        Module module = moduleRepositories.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        List<ModQA> assignments = modQARepository.findByModule(module);

        return assignments.stream()
                .filter(modQA -> modQA.getEmployee() != null)
                .map(modQA -> new ModuleEmployeeResponseDTO(
                        modQA.getModQaId(),
                        moduleId,
                        modQA.getEmployee().getEmpId(),
                        modQA.getEmployee().getFirstName(),
                        modQA.getEmployee().getLastName(),
                        modQA.getEmployee().getEmail(),
                        modQA.getIsLeader()
                ))
                .toList();
    }

//    @Override
//    public void deallocateEmployee(Long moduleId, Long employeeId) {
//
//        Module module = moduleRepositories.findById(moduleId)
//                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
//
//        Employee employee = employeeRepository.findById(employeeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
//
//        boolean exists = modQARepository.existsByModuleAndEmployee(module, employee);
//        if (!exists) {
//            throw new ResourceNotFoundException("Employee is not assigned to this module");
//        }
//
//        modQARepository.deleteByModuleAndEmployee(module, employee);
//    }

    @Override
    @Transactional
    public void deallocateEmployee(Long moduleId, Long employeeId) {
        Module module = moduleRepositories.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        boolean exists = modQARepository.existsByModuleAndEmployee(module, employee);
        if (!exists) {
            throw new ResourceNotFoundException("Employee is not assigned to this module");
        }


        if (defectRepository.existsByAssignTo_EmpIdAndModule_ModuleId(employeeId, moduleId)) {
            throw new IllegalArgumentException(
                    "Cannot deallocate employee. Employee is assigned to defects in this module. Please reassign the defects before deallocating."
            );
        }

        modQARepository.deleteByModuleAndEmployee(module, employee);
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName() + " " + employee.getLastName(),

                "projectName",
                module.getProject().getProjectName(),

                "moduleName",
                module.getModuleName()
        );

        notificationService.sendEmail(
                "MODULE_DEALLOCATION",
                employee.getEmail(),
                variables
        );
    }

    @Override
    public List<BenchAllocationResponseDTO> getAvailableQAs(Long moduleId) {

        Module module = moduleRepositories
                .findById(moduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with id: " + moduleId
                        )
                );
        Long projectId = module
                .getProject()
                .getProjectId();

        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findQaAllocationsByProject(    // matches the existing @Query method
                                projectId,
                                LocalDateTime.now()
                        );

        return allocations.stream()
                .map(allocation -> {
                    BenchAllocationResponseDTO dto = new BenchAllocationResponseDTO();

                    dto.setBenchAllocationId(allocation.getBenchAllocationId());
                    dto.setEmpId(allocation.getEmployee().getEmpId());
                    dto.setFirstName(allocation.getEmployee().getFirstName());
                    dto.setLastName(allocation.getEmployee().getLastName());
                    dto.setRoleType(allocation.getRole().getRoleType());

                    return dto;
                })
                .toList();
    }



}