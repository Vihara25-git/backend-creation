package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.BenchAllocationRequestDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.entities.*;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.BenchAllocationMapper;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.service.BenchAllocationService;
import com.sgic.defect_tracker.service.EmailNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.Role;

@Service
@RequiredArgsConstructor
public class BenchAllocationServiceImpl implements BenchAllocationService {

    @Autowired
    private ReleaseTestCaseRepository releaseTestCaseRepository;
    @Autowired
    private BenchAllocationMapper banchAllocationMapper;
    @Autowired
    private ModQARepository modQARepository;
    @Autowired
    private BenchAllocationRepository benchAllocationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectDetailsRepository projectDetailsRepository;


    @Autowired
    private BenchAvailabilityViewRepository benchAvailabilityRepository;

    @Autowired
    private BenchAllocationMapper benchAllocationMapper;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private DefectRepository defectRepository;
    @Autowired
    private SubmoduleDevRepository submoduleDevRepository;
//
//    @Autowired
//    private BenchAllocationService benchAllocationService;


    @Override
    public BenchAllocationResponseDTO save(BenchAllocationRequestDTO requestDTO) {


        //validate dates
        if (requestDTO.getStartDate() == null || requestDTO.getEndDate() == null) {
            throw new RuntimeException(
                    "Start date and end date are required");
        }
        LocalDate today = LocalDate.now();
        if (!requestDTO.getStartDate().isEqual(today)) {
            throw new ResourceNotFoundException("start date must be today");
        }
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
            throw new ResourceNotFoundException("Start date be after and date");
        }

        LocalDateTime startDateTime = requestDTO.getStartDate().atStartOfDay();

        LocalDateTime endDateTime = requestDTO.getEndDate().atTime(23, 59, 59, 999999);
        //
        ProjectDetails project = projectDetailsRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!"ACTIVE".equalsIgnoreCase(project.getStatus())) {
            throw new IllegalStateException(
                    "Employee allocation is unavailable while this project is " +
                            project.getStatus().toLowerCase()
            );
        }


        //Validate available percentage
        if (requestDTO.getAvailability() == null || requestDTO.getAvailability() <= 0 || requestDTO.getAvailability() > 100) {
            throw new ResourceNotFoundException("Availability must be between 1 and 100");
        }

        System.out.println("empId = " + requestDTO.getEmpId());
        //log.info("REQUEST EMP ID = {}", requestDTO.getEmpId());

        // Employee availability FROM VIEW
        BenchAvailabilityView benchAvailability =
                benchAvailabilityRepository
                        .findByEmpId(requestDTO.getEmpId())
                        .orElseThrow(() -> new RuntimeException("Employee availability not found"));

        // Current available percentage
        Long currentAvailability = benchAvailability.getAvailablePercentage();


        // Requested allocation check
        if (requestDTO.getAvailability() > currentAvailability) {
            throw new RuntimeException("Employee has only " + currentAvailability + "% availability");
        }


        // Get Employee
        Employee employee = employeeRepository
                .findById(requestDTO.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Get Role

        Role role = roleRepository
                .findById(requestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Long existingAllocation =
                benchAllocationRepository.countEmployeeProjectOverlap(
                        requestDTO.getEmpId(),
                        requestDTO.getProjectId(),
                        startDateTime,
                        endDateTime
                );

        if (existingAllocation > 0) {
            throw new RuntimeException(
                    "Employee is already allocated to this project "
            );
        }

//role validation
        Long differentRoleCount =
                benchAllocationRepository.countEmployeeProjectOverlap(
                        requestDTO.getEmpId(),
                        requestDTO.getProjectId(),
                        // requestDTO.getRoleId(),
                        // requestDTO.getStartDate(),
                        //requestDTO.getEndDate()
                        //benchAllocationId
                        startDateTime,
                        endDateTime
                );

        //BenchAllocationResponseDTO responseDTO = new BenchAllocationResponseDTO();
        if (differentRoleCount > 0) {
            throw new RuntimeException(
                    "Employee is already allocated to this project "
                    // "Employee is already allocated to this project with "+ role.getRoleName()+" another role"
            );
        }

        // Project Manager unique check for the given project & period
        boolean isProjectManagerRole = (role.getRoleType() != null && "PROJECT_MANAGER".equalsIgnoreCase(role.getRoleType()))
                || (role.getRoleName() != null && ("Project Manager".equalsIgnoreCase(role.getRoleName()) || "PROJECT_MANAGER".equalsIgnoreCase(role.getRoleName())));

        if (isProjectManagerRole) {
            Long pmOverlapCount = benchAllocationRepository.countProjectManagerOverlap(
                    requestDTO.getProjectId(),
                    startDateTime,
                    endDateTime
            );
            if (pmOverlapCount > 0) {
                throw new IllegalArgumentException(
                        "Only one Project Manager can be allocated to a project at a particular period of time."
                );
            }
        }

        System.out.println("START BEFORE SAVE = " + startDateTime);
        System.out.println("END BEFORE SAVE = " + endDateTime);


        // Mapper
        BenchAllocation benchAllocation =
                benchAllocationMapper.toEntity(requestDTO);

        benchAllocation.setEmployee(employee);

        benchAllocation.setEmployeeName(
                employee.getFirstName() + " " + employee.getLastName()
        );

        benchAllocation.setEmployeeEmail(
                employee.getEmail()
        );
        benchAllocation.setStartDate(startDateTime);
        benchAllocation.setEndDate(endDateTime);

        benchAllocation.setRole(role);
        benchAllocation.setProjectDetails(project);

        System.out.println(
                "Current availability = " + currentAvailability
        );

        System.out.println(
                "Requested availability = " + requestDTO.getAvailability()
        );

        // Save

        System.out.println("BEFORE SAVE - startDate = "
                + benchAllocation.getStartDate());

        System.out.println("BEFORE SAVE - endDate = "
                + benchAllocation.getEndDate());

        BenchAllocation saved = benchAllocationRepository.save(benchAllocation);
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName() + " " + employee.getLastName(),

                "projectName",
                project.getProjectName(),

                "roleName",
                role.getRoleName(),

                "startDate",
                benchAllocation.getStartDate(),

                "endDate",
                benchAllocation.getEndDate()
        );

        emailNotificationService.sendEmail(
                "PROJECT_ALLOCATION",
                employee.getEmail(),
                variables
        );

        System.out.println("AFTER SAVE - startDate = "
                + benchAllocation.getStartDate());

        System.out.println("AFTER SAVE - endDate = "
                + benchAllocation.getEndDate());

        // Response
        return benchAllocationMapper.toResponse(saved);


    }

//    @Transactional
//    @Override
//    public BenchAllocationResponseDTO update(Long benchAllocationId, BenchAllocationRequestDTO request) {
//
//       // LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
//        // Existing allocation start date must never change
//        LocalDateTime existingStartDateTime = existing.getStartDate();
//
//        if (existingStartDateTime == null) {
//            throw new IllegalStateException("Existing allocation start date is missing");
//        }
//        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59,59,999999);
//        // 1. Find existing allocation
//        BenchAllocation existing =
//                benchAllocationRepository
//                        .findById(benchAllocationId)
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Bench allocation not found"));
//
//        ProjectDetails project =
//                projectDetailsRepository
//                        .findById(request.getProjectId())
//                        .orElseThrow(() ->
//                                new RuntimeException("Project not found"));
//
//        if (!"ACTIVE".equalsIgnoreCase(project.getStatus())) {
//            throw new IllegalStateException(
//                    "Employee allocation is unavailable while this project is "
//                            + project.getStatus().toLowerCase()
//            );
//        }
//        // 2. Validate dates
//        if (request.getStartDate().isAfter(request.getEndDate())) {
//            throw new RuntimeException("Start date cannot be after end date");
//        }
//
//
//        // 3. Validate percentage
//        if (request.getAvailability() == null ||
//                request.getAvailability() <= 0 ||
//                request.getAvailability() > 100) {
//
//            throw new RuntimeException("Allocation percentage must be between 1 and 100");
//        }
//        // 8. Get role
//        Role role =
//                roleRepository
//                        .findById(request.getRoleId())
//                        .orElseThrow(() -> new RuntimeException("Role not found"));
//
//        // 4. Get current employee availability from VIEW
//        BenchAvailabilityView view =
//                benchAvailabilityRepository
//                        .findByEmpId(existing.getEmployee().getEmpId())
//                        .orElseThrow(() -> new RuntimeException("Employee availability not found"));
//
//
//        // 5. Restore old allocation
//        Long availableAfterRemovingOld = view.getAvailablePercentage() + existing.getAvailability();
//
//
//        // 6. Check new allocation
//        if (request.getAvailability() > availableAfterRemovingOld) {
//
//            throw new RuntimeException("Employee has only " + availableAfterRemovingOld + "% availability for this update");
//        }
//
//
//        // Project Manager unique check for the given project & period
//        boolean isProjectManagerRole = (role.getRoleType() != null && "PROJECT_MANAGER".equalsIgnoreCase(role.getRoleType()))
//                || (role.getRoleName() != null && ("Project Manager".equalsIgnoreCase(role.getRoleName()) || "PROJECT_MANAGER".equalsIgnoreCase(role.getRoleName())));
//
//        if (isProjectManagerRole) {
//            Long pmOverlapCount = benchAllocationRepository.countProjectManagerOverlapForUpdate(
//                    request.getProjectId(),
//                    startDateTime,
//                    endDateTime,
//                    benchAllocationId
//            );
//            if (pmOverlapCount > 0) {
//                throw new IllegalArgumentException(
//                        "Only one Project Manager can be allocated to a project at a particular period of time."
//                );
//            }
//        }
//        // Developer role validation if assigned to submodules in this project
//        Long empId = existing.getEmployee() != null ? existing.getEmployee().getEmpId() : null;
//        if (empId != null && submoduleDevRepository.existsByEmployee_EmpIdAndProjectId(empId, request.getProjectId())) {
//            boolean isDeveloperRole = (role.getRoleType() != null && (
//                    "DEVELOPER".equalsIgnoreCase(role.getRoleType()) ||
//                            "SENIOR_DEVELOPER".equalsIgnoreCase(role.getRoleType()) ||
//                            "JUNIOR_DEVELOPER".equalsIgnoreCase(role.getRoleType()) ||
//                            "DEV_LEAD".equalsIgnoreCase(role.getRoleType()) ||
//                            role.getRoleType().toUpperCase().contains("DEVELOPER") ||
//                            role.getRoleType().toUpperCase().contains("DEV")
//            )) || (role.getRoleName() != null && (
//                    role.getRoleName().toUpperCase().contains("DEVELOPER") ||
//                            role.getRoleName().toUpperCase().contains("DEV")
//            ));
//
//            if (!isDeveloperRole) {
//                throw new IllegalArgumentException(
//                        "Cannot change role to non-developer role '" + role.getRoleName() + "'. " +
//                                "This employee is currently assigned to submodules in this project. " +
//                                "They can only have developer-related roles (e.g., Developer, Senior Developer, Junior Developer, Dev Lead) " +
//                                "or must be deallocated from the submodules first."
//                );
//            }
//        }
//
//        // 10. Mapper update
//        benchAllocationMapper.updateEntity(request, existing);
//        existing.setStartDate(startDateTime);
//        existing.setEndDate(endDateTime);
//        //existing.setEmployee(employee);
//        existing.setRole(role);
//        existing.setProjectDetails(project);
//
//
//        // 11. Save
//        BenchAllocation updated = benchAllocationRepository.save(existing);
//

    /// / 12. Send allocation update email
//        Map<String, Object> variables = Map.of(
//                "employeeName",
//                existing.getEmployee().getFirstName()
//                        + " "
//                        + existing.getEmployee().getLastName(),
//
//                "projectName",
//                project.getProjectName(),
//
//                "roleName",
//                role.getRoleName(),
//
//                "startDate",
//                updated.getStartDate(),
//
//                "endDate",
//                updated.getEndDate()
//        );
//
//        emailNotificationService.sendEmail(
//                "PROJECT_ALLOCATION_UPDATED",
//                existing.getEmployee().getEmail(),
//                variables
//        );
//
//
//        // 12. Response
//        return benchAllocationMapper.toResponse(updated);
//    }
    @Transactional
    @Override
    public BenchAllocationResponseDTO update(
            Long benchAllocationId,
            BenchAllocationRequestDTO request) {

//        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
//        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59,59,999999);
        // 1. Find existing allocation
        BenchAllocation existing =
                benchAllocationRepository
                        .findById(benchAllocationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bench allocation not found"));


        // =========================================================
        // 2. Validate required fields
        // =========================================================

        if (request.getEndDate() == null) {
            throw new RuntimeException("End date is required");
        }

        if (request.getAvailability() == null ||
                request.getAvailability() <= 0 ||
                request.getAvailability() > 100) {

            throw new RuntimeException(
                    "Allocation percentage must be between 1 and 100");
        }

        if (request.getRoleId() == null) {
            throw new RuntimeException("Role is required");
        }


        // =========================================================
        // 3. Existing Start Date
        //
        // Start date MUST NOT be changed during update.
        // =========================================================

        LocalDateTime existingStartDateTime =
                existing.getStartDate();

        if (existingStartDateTime == null) {
            throw new IllegalStateException(
                    "Existing allocation start date is missing");
        }


        // =========================================================
        // 4. If frontend sends startDate,
        //    make sure it matches the original start date.
        //
        // This protects the API even if someone modifies
        // the request manually.
        // =========================================================

        if (request.getStartDate() != null &&
                !request.getStartDate()
                        .isEqual(existingStartDateTime.toLocalDate())) {

            throw new IllegalArgumentException(
                    "Start date cannot be changed during update. " +
                            "Original start date is " +
                            existingStartDateTime.toLocalDate()
            );
        }


        // =========================================================
        // 5. New End Date
        // =========================================================

        //LocalDateTime endDateTime =
       //         request.getEndDate()
        //                .atTime(LocalTime.MAX);

        LocalDateTime endDateTime =
                request.getEndDate().atTime(23, 59,59,999999);

        // End date cannot be before original start date
        if (endDateTime.isBefore(existingStartDateTime)) {

            throw new RuntimeException(
                    "End date cannot be before allocation start date");
        }


        // =========================================================
        // 6. Get EXISTING project
        //
        // Project cannot be changed during update.
        // =========================================================

        ProjectDetails project =
                existing.getProjectDetails();

        if (project == null) {
            throw new RuntimeException(
                    "Project not found for this allocation");
        }


        // =========================================================
        // 7. Project must be ACTIVE
        // =========================================================

        if (!"ACTIVE".equalsIgnoreCase(project.getStatus())) {

            throw new IllegalStateException(
                    "Employee allocation is unavailable while this project is "
                            + project.getStatus().toLowerCase()
            );
        }


        // =========================================================
        // 8. End date cannot exceed project end date
        //
        // ProjectDetails.endDate = LocalDate
        // =========================================================

        LocalDate projectEndDate =
                project.getEndDate();

        if (projectEndDate == null) {
            throw new IllegalStateException(
                    "Project end date is missing");
        }

        if (request.getEndDate()
                .isAfter(projectEndDate)) {

            throw new IllegalArgumentException(
                    "End date cannot be after project end date "
                            + projectEndDate
            );
        }


        // =========================================================
        // 9. Get NEW role
        //
        // Role CAN be changed during update.
        // =========================================================

        Role role =
                roleRepository
                        .findById(request.getRoleId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role not found"));


        // =========================================================
        // 10. Get EXISTING employee
        //
        // Employee cannot be changed.
        // =========================================================

        Employee employee =
                existing.getEmployee();

        if (employee == null) {
            throw new RuntimeException(
                    "Employee not found for this allocation");
        }

        Long empId = employee.getEmpId();


        // =========================================================
        // 11. Get current employee availability
        // =========================================================

        BenchAvailabilityView view =
                benchAvailabilityRepository
                        .findByEmpId(empId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee availability not found"));


        // =========================================================
        // 12. Restore old allocation percentage
        // =========================================================

        Long availableAfterRemovingOld =
                view.getAvailablePercentage()
                        + existing.getAvailability();


        // =========================================================
        // 13. Validate new allocation percentage
        // =========================================================

        if (request.getAvailability()
                > availableAfterRemovingOld) {

            throw new RuntimeException(
                    "Employee has only "
                            + availableAfterRemovingOld
                            + "% availability for this update"
            );
        }


        // =========================================================
        // 14. Project Manager unique validation
        // =========================================================

        boolean isProjectManagerRole =
                (role.getRoleType() != null &&
                        "PROJECT_MANAGER".equalsIgnoreCase(
                                role.getRoleType()))

                        ||

                        (role.getRoleName() != null &&
                                (
                                        "Project Manager".equalsIgnoreCase(
                                                role.getRoleName())

                                                ||

                                                "PROJECT_MANAGER".equalsIgnoreCase(
                                                        role.getRoleName())
                                ));


        if (isProjectManagerRole) {

            Long pmOverlapCount =
                    benchAllocationRepository
                            .countProjectManagerOverlapForUpdate(
                                    project.getProjectId(),
                                    existingStartDateTime,
                                    endDateTime,
                                    benchAllocationId
                            );

            if (pmOverlapCount > 0) {

                throw new IllegalArgumentException(
                        "Only one Project Manager can be allocated " +
                                "to a project at a particular period of time."
                );
            }
        }


        // =========================================================
        // 15. Developer role validation
        // =========================================================

        if (submoduleDevRepository
                .existsByEmployee_EmpIdAndProjectId(
                        empId,
                        project.getProjectId())) {

            boolean isDeveloperRole =
                    (role.getRoleType() != null &&
                            (
                                    "DEVELOPER".equalsIgnoreCase(
                                            role.getRoleType())

                                            ||

                                            "SENIOR_DEVELOPER".equalsIgnoreCase(
                                                    role.getRoleType())

                                            ||

                                            "JUNIOR_DEVELOPER".equalsIgnoreCase(
                                                    role.getRoleType())

                                            ||

                                            "DEV_LEAD".equalsIgnoreCase(
                                                    role.getRoleType())

                                            ||

                                            role.getRoleType()
                                                    .toUpperCase()
                                                    .contains("DEVELOPER")

                                            ||

                                            role.getRoleType()
                                                    .toUpperCase()
                                                    .contains("DEV")
                            ))

                            ||

                            (role.getRoleName() != null &&
                                    (
                                            role.getRoleName()
                                                    .toUpperCase()
                                                    .contains("DEVELOPER")

                                                    ||

                                                    role.getRoleName()
                                                            .toUpperCase()
                                                            .contains("DEV")
                                    ));


            if (!isDeveloperRole) {

                throw new IllegalArgumentException(
                        "Cannot change role to non-developer role '"
                                + role.getRoleName()
                                + "'. This employee is currently assigned "
                                + "to submodules in this project. They can "
                                + "only have developer-related roles or must "
                                + "be deallocated from the submodules first."
                );
            }
        }


        // =========================================================
        // 16. Update allowed fields ONLY
        //
        // Start Date   -> KEEP
        // Employee     -> KEEP
        // Project      -> KEEP
        // Role         -> CHANGE
        // End Date     -> CHANGE
        // Availability -> CHANGE
        // =========================================================

        existing.setStartDate(existingStartDateTime);

        existing.setEndDate(endDateTime);

        existing.setAvailability(
                request.getAvailability());

        existing.setRole(role);


        // =========================================================
        // 17. Save
        // =========================================================

        BenchAllocation updated =
                benchAllocationRepository.save(existing);


        // =========================================================
        // 18. Send update email
        // =========================================================

        Map<String, Object> variables = Map.of(

                "employeeName",
                employee.getFirstName()
                        + " "
                        + employee.getLastName(),

                "projectName",
                project.getProjectName(),

                "roleName",
                role.getRoleName(),

                "startDate",
                updated.getStartDate(),

                "endDate",
                updated.getEndDate()
        );


        emailNotificationService.sendEmail(
                "PROJECT_ALLOCATION_UPDATED",
                employee.getEmail(),
                variables
        );


        // =========================================================
        // 19. Response
        // =========================================================

        return benchAllocationMapper.toResponse(updated);
    }

    @Transactional
    @Override
    public BenchAllocationResponseDTO deAllocate(Long benchAllocationId, BenchAllocationRequestDTO requestDTO) {
        // 1. Find existing allocation
        BenchAllocation allocation =
                benchAllocationRepository
                        .findById(benchAllocationId)
                        .orElseThrow(() -> new RuntimeException("Bench allocation not found"));

        if (allocation.getEmployee() != null && allocation.getProjectDetails() != null) {


            Long empId = allocation.getEmployee().getEmpId();
            Long projectId = allocation.getProjectDetails().getProjectId();


// 1. Check pending defects
            boolean hasPendingDefects =
                    defectRepository
                            .existsByAssignTo_EmpIdAndProjectDetails_ProjectIdAndStatusType_StatusNameNotIn(
                                    empId,
                                    projectId,
                                    List.of("CLOSED", "REJECTED", "DUPLICATED", "FIXED")
                            );

            if (hasPendingDefects) {
                throw new IllegalArgumentException(
                        "Cannot deallocate employee '" +
                                allocation.getEmployee().getFirstName() + " " +
                                allocation.getEmployee().getLastName() +
                                "'. This employee has pending defects in this project. " +
                                "All defects must be FIXED, CLOSED, REJECTED, or DUPLICATED before deallocation."
                );
            }


// 2. Check pending test cases
            boolean hasPendingTestCases =
                    releaseTestCaseRepository.existsPendingTestCases(
                            empId,
                            projectId
                    );

            if (hasPendingTestCases) {
                throw new IllegalArgumentException(
                        "Cannot deallocate employee '" +
                                allocation.getEmployee().getFirstName() + " " +
                                allocation.getEmployee().getLastName() +
                                "'. This employee has test cases that are not completed. " +
                                "All test cases must have PASS or FAIL before deallocation."
                );
            }
        }

//        if (allocation.getEmployee() != null && allocation.getProjectDetails() != null) {
//            Long empId = allocation.getEmployee().getEmpId();
//            Long projectId = allocation.getProjectDetails().getProjectId();
//            if (defectRepository.existsByAssignTo_EmpIdAndProjectDetails_ProjectId(empId, projectId)) {
//                throw new IllegalArgumentException(
//                        "Cannot deallocate employee '" + allocation.getEmployee().getFirstName() + " " + allocation.getEmployee().getLastName() +
//                                "'. This employee is assigned to defects in this project. Please reassign the defects before deallocating."
//                );
//            }
//        }

        //allocation.setEndDate(LocalDateTime.now());

        Employee employee = allocation.getEmployee();

        ProjectDetails projectDetails =
                allocation.getProjectDetails();

        Role role =
                allocation.getRole();

        LocalDateTime deallocationTime =
                LocalDateTime.now();

        allocation.setEndDate(deallocationTime);


        BenchAllocation updated = benchAllocationRepository.save(allocation);
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName()
                        + " "
                        + employee.getLastName(),

                "projectName",
                projectDetails != null
                        ? projectDetails.getProjectName()
                        : "",

                "roleName",
                role != null
                        ? role.getRoleName()
                        : "",

                "endDate",
                deallocationTime
        );

        // 6. Send deallocation email
        emailNotificationService.sendEmail(
                "PROJECT_DEALLOCATION",
                employee.getEmail(),
                variables
        );
        return benchAllocationMapper.toResponse(updated);
    }


    @Override
    public Page<BenchAllocationResponseDTO> Projectemployeeallocation(Pageable pageable) {

        return benchAllocationRepository.findAll(pageable)
                .map(banchAllocationMapper::toResponse);
    }


    private BenchAllocationResponseDTO toResponseWithDerivedFields(
            BenchAllocation entity) {

        BenchAllocationResponseDTO dto =
                benchAllocationMapper.toResponse(entity);

        if (entity.getStartDate() == null
                || !entity.getStartDate().isAfter(LocalDateTime.now())) {

            dto.setAvailablePeriod("Immediate");

        } else {
            dto.setAvailablePeriod(
                    entity.getStartDate().toLocalDate().toString());
        }

        if (entity.getProjectDetails() == null) {
            dto.setProjectName("No Projects");
        }

        return dto;
    }


    @Override
    public List<BenchAllocationResponseDTO> Getbyprojectid(Long projectId) {
        LocalDateTime now = LocalDateTime.now();
        List<BenchAllocation> allocations = benchAllocationRepository.findByProjectDetails_ProjectId(projectId, now);
        return allocations.stream()
                .map(benchAllocationMapper::toResponse)
                .toList();

    }


    public List<BenchAllocationResponseDTO> GetAllocatiotionHistorybyprojectid(Long projectId) {


        List<BenchAllocation> allocations = benchAllocationRepository.findAllAllocationsByProjectId(projectId);

        return allocations.stream()
                .map(benchAllocationMapper::toResponse)
                .toList();

    }


    @Override
    public BenchAllocationResponseDTO getById(Long benchAllocationId) {
        BenchAllocation entity = benchAllocationRepository
                .findById(benchAllocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bench allocation not found"));

        return benchAllocationMapper.toResponse(entity);

    }

    // filter for the bench-mithun
    @Override
    public Page<BenchAllocationResponseDTO> getFilteredBenchAllocations(
            Long projectId,
            String search,
            Long roleId,
            Long minAvailability,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            Pageable pageable) {

        Specification<BenchAllocation> spec =
                (root, query, cb) ->
                        cb.equal(
                                root.get("projectDetails")
                                        .get("projectId"),
                                projectId
                        );


        if (search != null && !search.isBlank()) {

            String like =
                    "%" + search.trim().toLowerCase() + "%";

            spec = spec.and((root, query, cb) ->
                    cb.or(

                            cb.like(
                                    cb.lower(
                                            root.get("employee")
                                                    .get("firstName")
                                    ),
                                    like
                            ),

                            cb.like(
                                    cb.lower(
                                            root.get("employee")
                                                    .get("lastName")
                                    ),
                                    like
                            )
                    )
            );
        }


        if (roleId != null) {

            spec = spec.and((root, query, cb) ->
                    cb.equal(
                            root.get("role")
                                    .get("roleId"),
                            roleId
                    )
            );
        }

        if (minAvailability != null) {

            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.get("availability"),
                            minAvailability
                    )
            );
        }

        if (startDateFrom != null) {

            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.get("startDate"),
                            startDateFrom.atStartOfDay()
                    )
            );
        }


        if (startDateTo != null) {

            spec = spec.and((root, query, cb) ->
                    cb.lessThan(
                            root.get("startDate"),
                            startDateTo
                                    .plusDays(1)
                                    .atStartOfDay()
                    )
            );
        }

        return benchAllocationRepository
                .findAll(spec, pageable)
                .map(this::toResponseWithDerivedFields);
    }


    @Override
    public List<BenchAllocationResponseDTO> getCurrentProjectDevelopers(
            Long projectId
    ) {

        LocalDateTime now = LocalDateTime.now();

        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findCurrentProjectDevelopers(
                                projectId,
                                now
                        );

        return allocations.stream()
                .map(benchAllocationMapper::toResponse)
                .toList();


    }

}



