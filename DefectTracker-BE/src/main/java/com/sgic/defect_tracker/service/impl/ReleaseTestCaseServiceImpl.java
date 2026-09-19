package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.DefectRequestDTO;
import com.sgic.defect_tracker.dtos.response.*;
import com.sgic.defect_tracker.entities.TestCase;
import com.sgic.defect_tracker.service.DefectService;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseEmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseStatusRequestDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ReleaseTestCase;
import com.sgic.defect_tracker.mapper.QAEmployeeMapper;
import com.sgic.defect_tracker.mapper.ReleaseTestCaseMapper;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.ReleaseTestCaseRepository;
import com.sgic.defect_tracker.repositories.ReleaseViewRepository;
import com.sgic.defect_tracker.repositories.TestCaseRepository;
import com.sgic.defect_tracker.service.ReleaseTestCaseService;
import com.sgic.defect_tracker.service.WorkFlowService;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sgic.defect_tracker.repositories.*;
import org.springframework.web.multipart.MultipartFile;
import com.sgic.defect_tracker.mapper.TestCaseMapper;

import java.time.LocalDateTime;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.service.EmailNotificationService;

@Service
@RequiredArgsConstructor
@Transactional
public class ReleaseTestCaseServiceImpl implements ReleaseTestCaseService {

    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final ReleaseViewRepository releaseViewRepository;
    private final TestCaseRepository testCaseRepository;
    private final ReleaseTestCaseMapper releaseTestCaseMapper;
    private final TestCaseMapper testCaseMapper;
    private final DefectService defectService;
    private final BenchAllocationRepository benchAllocationRepository;
    private final QAEmployeeMapper qaEmployeeMapper;
    private final EmployeeRepository employeeRepository;
    private final EmailNotificationService notificationService;
    //    private final DefectRequestDTO defectRequestDTO;
//    private final DefectService defectService;
    private final DefectRepository defectRepository;
    private final WorkFlowService workFlowService;
    private final SeverityRepository severityRepository;
    private final StatusTypeRepository statusTypeRepository;





    @Override
    public List<ReleaseTestCaseResponseDTO> allocateTestCases(
            Long releaseId,
            ReleaseTestCaseRequestDTO request) {

        var releaseView = releaseViewRepository.findById(releaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release not found"));

        List<ReleaseTestCaseResponseDTO> responseList =
                new ArrayList<>();

        for (Long testCaseId : request.getTestCaseIds()) {

            var testCase = testCaseRepository.findById(testCaseId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Test Case not found: " + testCaseId));

            boolean alreadyAllocated =
                    releaseTestCaseRepository
                            .existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(
                                    releaseId,
                                    testCaseId
                            );

            if (alreadyAllocated) {
                continue;
            }

            ReleaseTestCase releaseTestCase =
                    new ReleaseTestCase();

            releaseTestCase.setReleaseView(releaseView);
            releaseTestCase.setTestCase(testCase);

            ReleaseTestCase saved =
                    releaseTestCaseRepository.save(releaseTestCase);

            responseList.add(
                    releaseTestCaseMapper.toResponse(saved)
            );
        }

        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public ReleaseTestCaseResponseDTO getReleaseTestCase(
            Long releaseId,
            Long releaseTestCaseId) {

        ReleaseTestCase releaseTestCase =
                releaseTestCaseRepository
                        .findByReleaseView_ReleaseIdAndReleaseTestCaseId(
                                releaseId,
                                releaseTestCaseId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Release Test Case not found"));

        ReleaseTestCaseResponseDTO dto =
                releaseTestCaseMapper.toResponse(releaseTestCase);

        var testCase = releaseTestCase.getTestCase();

        if (testCase != null) {

            dto.setTestCaseId(testCase.getTestCaseId());
            dto.setTestCaseName(testCase.getTestCaseName());
            dto.setDescription(testCase.getDescription());
            dto.setTestSteps(testCase.getTestSteps());

            if (testCase.getModule() != null) {
                dto.setModuleId(
                        testCase.getModule().getModuleId()
                );

                dto.setModuleName(
                        testCase.getModule().getModuleName()
                );
            }

            if (testCase.getSubModule() != null) {
                dto.setSubModuleId(
                        testCase.getSubModule().getSubModuleId()
                );

                dto.setSubModuleName(
                        testCase.getSubModule().getSubModuleName()
                );
            }
        }

        return dto;
    }



    @Override
    @Transactional(readOnly = true)
    public List<QAEmployeeResponseDTO> getAvailableQAEmployees(
            Long releaseId,
            Long testcaseId) {

        // 1. Get Release
        var releaseView =
                releaseViewRepository.findById(releaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Release not found"));

        // 2. Get Test Case
        var testCase =
                testCaseRepository.findById(testcaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Test Case not found: "
                                                + testcaseId));

        // 3. Get project ID from Release
        Long releaseProjectId =
                releaseView
                        .getProjectDetails()
                        .getProjectId();

        // 4. Get project ID from Test Case
        Long testCaseProjectId =
                testCase
                        .getProjectDetails()
                        .getProjectId();

        // 5. Make sure both belong to same project
        if (!releaseProjectId.equals(testCaseProjectId)) {

            throw new RuntimeException(
                    "Test Case does not belong to this project");
        }

        // 6. Current time
        LocalDateTime currentTime =
                LocalDateTime.now();

        // 7. Get QA allocations
        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findQaAllocationsByProject(
                                releaseProjectId,
                                currentTime
                        );

        // 8. Remove duplicate employees
        Map<Long, BenchAllocation> uniqueEmployees =
                allocations.stream()
                        .collect(Collectors.toMap(
                                allocation ->
                                        allocation
                                                .getEmployee()
                                                .getEmpId(),

                                allocation ->
                                        allocation,

                                (existing, duplicate) ->
                                        existing
                        ));

        // 9. Convert to response
        return uniqueEmployees.values()
                .stream()
                .map(allocation -> {

                    QAEmployeeResponseDTO dto =
                            new QAEmployeeResponseDTO();

                    dto.setBenchAllocationId(
                            allocation
                                    .getBenchAllocationId()
                    );

                    dto.setEmpId(
                            allocation
                                    .getEmployee()
                                    .getEmpId()
                    );

                    dto.setFirstName(
                            allocation
                                    .getEmployee()
                                    .getFirstName()
                    );

                    dto.setLastName(
                            allocation
                                    .getEmployee()
                                    .getLastName()
                    );

                    dto.setEmail(
                            allocation
                                    .getEmployee()
                                    .getEmail()
                    );

                    dto.setRoleType(
                            allocation
                                    .getRole()
                                    .getRoleType()
                    );

                    return dto;
                })
                .toList();
    }



//    @Override
//    @Transactional
//    public ReleaseTestCaseResponseDTO assignQAEmployee(
//            Long releaseId,
//            Long testcaseId,
//            ReleaseTestCaseEmployeeRequestDTO request) {
//
//        // 1. Get Release Test Case
//        ReleaseTestCase releaseTestCase =
//                releaseTestCaseRepository
//                        .findByReleaseView_ReleaseIdAndTestCase_TestCaseId(
//                                releaseId,
//                                testcaseId
//                        )
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Release Test Case not found"
//                                ));
//
//        // 2. Get Employee
//        Employee employee =
//                employeeRepository
//                        .findById(request.getEmployeeId())
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Employee not found"
//                                ));
//
//        // 3. Employee must be active
//        if (!Boolean.TRUE.equals(
//                employee.getIsActive())) {
//
//            throw new RuntimeException(
//                    "Employee is inactive"
//            );
//        }
//
//        // 4. Get project from Release
//        Long projectId =
//                releaseTestCase
//                        .getReleaseView()
//                        .getProjectDetails()
//                        .getProjectId();
//
//        // 5. Current time
//        LocalDateTime currentTime =
//                LocalDateTime.now();
//
//        // 6. Get current QA allocations
//        List<BenchAllocation> allocations =
//                benchAllocationRepository
//                        .findQaAllocationsByProject(
//                                projectId,
//                                currentTime
//                        );
//
//        // 7. Find selected employee allocation
//        BenchAllocation benchAllocation =
//                allocations.stream()
//                        .filter(allocation ->
//                                allocation
//                                        .getEmployee()
//                                        .getEmpId()
//                                        .equals(
//                                                employee.getEmpId()
//                                        )
//                        )
//                        .findFirst()
//                        .orElseThrow(() ->
//                                new RuntimeException(
//                                        "Employee is not an available QA Engineer or QA Lead for this project"
//                                )
//                        );
//
//        // 8. Assign Bench Allocation
//        releaseTestCase.setBenchAllocation(
//                benchAllocation
//        );
//
//        // 9. Save
//        ReleaseTestCase saved =
//                releaseTestCaseRepository.save(
//                        releaseTestCase
//                );
//        Map<String, Object> variables = Map.of(
//                "employeeName",
//                employee.getFirstName() + " " + employee.getLastName(),
//
//                "projectName",
//                releaseTestCase
//                        .getReleaseView()
//                        .getProjectDetails()
//                        .getProjectName(),
//
//                "moduleName",
//                releaseTestCase
//                        .getTestCase()
//                        .getModule()
//                        .getModuleName(),
//
//                "testCaseId",
//                releaseTestCase
//                        .getTestCase()
//                        .getTestCaseId()
//                        .toString(),
//
//                "testCaseName",
//                releaseTestCase
//                        .getTestCase()
//                        .getTestCaseName()
//        );
//
//        notificationService.sendEmail(
//                "QA_TESTCASE_ALLOCATION",
//                employee.getEmail(),
//                variables
//        );
//
//        // 10. Return complete response
//        return releaseTestCaseMapper.toResponse(
//                saved
//        );
//    }


    @Override
    @Transactional
    public ReleaseTestCaseResponseDTO assignQAEmployee(
            Long releaseId,
            Long testcaseId,
            ReleaseTestCaseEmployeeRequestDTO request) {

        // 1. Get Release Test Case
        ReleaseTestCase releaseTestCase =
                releaseTestCaseRepository
                        .findByReleaseView_ReleaseIdAndTestCase_TestCaseId(
                                releaseId,
                                testcaseId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Release Test Case not found for releaseId="
                                                + releaseId
                                                + ", testCaseId="
                                                + testcaseId
                                ));

        // 2. Get Employee
        Employee employee =
                employeeRepository
                        .findById(request.getEmployeeId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found: "
                                                + request.getEmployeeId()
                                ));

        // 3. Employee must be active
        if (!Boolean.TRUE.equals(employee.getIsActive())) {
            throw new RuntimeException(
                    "Employee is inactive: "
                            + employee.getEmpId()
            );
        }

        // 4. Get Project ID
        if (releaseTestCase.getReleaseView() == null
                || releaseTestCase.getReleaseView().getProjectDetails() == null) {

            throw new RuntimeException(
                    "Project information not found for Release Test Case"
            );
        }

        Long projectId =
                releaseTestCase
                        .getReleaseView()
                        .getProjectDetails()
                        .getProjectId();

        // 5. Current time
        LocalDateTime currentTime = LocalDateTime.now();

        // 6. Get current QA allocations
        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findQaAllocationsByProject(
                                projectId,
                                currentTime
                        );

        System.out.println("========== QA ALLOCATION DEBUG ==========");
        System.out.println("Release ID: " + releaseId);
        System.out.println("Test Case ID: " + testcaseId);
        System.out.println("Employee ID: " + employee.getEmpId());
        System.out.println("Project ID: " + projectId);
        System.out.println("Current Time: " + currentTime);
        System.out.println("Available allocations: " + allocations.size());

        allocations.forEach(allocation -> {
            if (allocation.getEmployee() != null) {
                System.out.println(
                        "BenchAllocation ID: "
                                + allocation.getBenchAllocationId()
                                + " | Employee ID: "
                                + allocation.getEmployee().getEmpId()
                                + " | Employee: "
                                + allocation.getEmployee().getFirstName()
                                + " "
                                + allocation.getEmployee().getLastName()
                );
            }
        });

        System.out.println("========================================");

        // 7. Find selected employee's current QA allocation
        BenchAllocation benchAllocation =
                allocations.stream()
                        .filter(allocation ->
                                allocation.getEmployee() != null
                                        && allocation.getEmployee()
                                        .getEmpId()
                                        .equals(employee.getEmpId())
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee is not an available QA Engineer or QA Lead for this project"
                                )
                        );

        System.out.println(
                "Selected Bench Allocation ID: "
                        + benchAllocation.getBenchAllocationId()
        );

        // 8. Assign Bench Allocation
        releaseTestCase.setBenchAllocation(
                benchAllocation
        );

        // 9. Save
        ReleaseTestCase saved =
                releaseTestCaseRepository.save(
                        releaseTestCase
                );

        System.out.println(
                "QA allocation saved successfully. "
                        + "ReleaseTestCase ID: "
                        + saved.getReleaseTestCaseId()
                        + " | BenchAllocation ID: "
                        + saved.getBenchAllocation().getBenchAllocationId()
        );

        // 10. Return response
        return releaseTestCaseMapper.toResponse(saved);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<ReleaseTestCaseResponseDTO> getTestCasesByFilter(
//            Long releaseId,
//            Long moduleId,
//            Long subModuleId) {
//
//        // Check release
//        releaseViewRepository.findById(releaseId)
//                .orElseThrow(() ->
//                        new RuntimeException("Release not found"));
//
//        List<ReleaseTestCase> releaseTestCases;
//
//        // Release + Module + Submodule
//        if (moduleId != null && subModuleId != null) {
//
//            releaseTestCases =
//                    releaseTestCaseRepository
//                            .findByReleaseView_ReleaseIdAndTestCase_Module_ModuleIdAndTestCase_SubModule_SubModuleId(
//                                    releaseId,
//                                    moduleId,
//                                    subModuleId
//                            );
//
//        }
//        // Release + Module
//        else if (moduleId != null) {
//
//            releaseTestCases =
//                    releaseTestCaseRepository
//                            .findByReleaseView_ReleaseIdAndTestCase_Module_ModuleId(
//                                    releaseId,
//                                    moduleId
//                            );
//
//        }
//        // Release only
//        else {
//
//            releaseTestCases =
//                    releaseTestCaseRepository
//                            .findByReleaseView_ReleaseId(releaseId);
//        }
//
//        return releaseTestCases.stream()
//                .map(entity -> {
//
//                    ReleaseTestCaseResponseDTO dto =
//                            releaseTestCaseMapper.toResponse(entity);
//
//                    // QA EXECUTER
//
//                    if (entity.getBenchAllocation() != null) {
//
//                        System.out.println("========== QA CHECK ==========");
//                        System.out.println(
//                                "ReleaseTestCase ID: "
//                                        + entity.getReleaseTestCaseId()
//                        );
//
//                        System.out.println(
//                                "Bench Allocation ID: "
//                                        + entity.getBenchAllocation()
//                                        .getBenchAllocationId()
//                        );
//
//                        Employee employee =
//                                entity.getBenchAllocation().getEmployee();
//
//                        System.out.println(
//                                "Employee ID: "
//                                        + (employee != null ? employee.getEmpId() : null)
//                        );;
//
//                        if (employee != null) {
//
//                            System.out.println(
//                                    "Employee ID: "
//                                            + employee.getEmpId()
//                            );
//
//                            System.out.println(
//                                    "Employee Name: "
//                                            + employee.getFirstName()
//                                            + " "
//                                            + employee.getLastName()
//                            );
//
//                            dto.setEmployeeId(
//                                    employee.getEmpId()
//                            );
//
//                            dto.setEmployeeName(
//                                    employee.getFirstName()
//                                            + " "
//                                            + employee.getLastName()
//                            );
//
//                        } else {
//
//                            System.out.println(
//                                    "❌ EMPLOYEE IS NULL"
//                            );
//                        }
//
//                    } else {
//
//                        System.out.println(
//                                "❌ BENCH ALLOCATION IS NULL"
//                        );
//                    }
//
//                    // DEFECT DETAILS
//
//                    if (entity.getTestCase() != null) {
//
//                        Long testCaseId =
//                                entity.getTestCase().getTestCaseId();
//
//                        System.out.println(
//                                "TestCase ID for defect lookup: "
//                                        + testCaseId
//                        );
//
//                        defectRepository
//                                .findByTestCase_TestCaseId(testCaseId)
//                                .ifPresent(defect -> {
//
//                                    // Defect ID
//                                    dto.setDefectId(
//                                            defect.getDefectId()
//                                    );
//
//                                    // EXECUTER (QA)
//                                    // Get from Defect.enterBy
//                                    if (defect.getEnterBy() != null &&
//                                            !defect.getEnterBy().isBlank()) {
//
//                                        dto.setEmployeeName(defect.getEnterBy());
//                                    }
//
//                                    // Assigned Developer
//                                    if (defect.getAssignTo() != null) {
//
//                                        Employee assignedEmployee =
//                                                defect.getAssignTo();
//
//                                        dto.setAssignToId(
//                                                assignedEmployee.getEmpId()
//                                        );
//
//                                        dto.setAssignToName(
//                                                assignedEmployee.getFirstName()
//                                                        + " "
//                                                        + assignedEmployee.getLastName()
//                                        );
//                                    }
//
//                                    // Priority
//                                    if (defect.getPriority() != null) {
//
//                                        dto.setPriorityId(
//                                                defect.getPriority()
//                                                        .getPriorityId()
//                                        );
//
//                                        dto.setPriorityName(
//                                                defect.getPriority()
//                                                        .getPriorityName()
//                                        );
//                                    }
//                                });
//                    }
//
//                    return dto;
//                })
//                .toList();
//    }

    @Override
    @Transactional
    public List<ReleaseTestCaseResponseDTO> getTestCasesByFilter(
            Long releaseId,
            Long moduleId,
            Long subModuleId) {

        // ============================================================
        // 1. CHECK RELEASE
        // ============================================================

        var releaseView = releaseViewRepository.findById(releaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release not found"));

        // ============================================================
        // Sync project test cases created in Test Case module
        // ============================================================
        if (releaseView.getProjectDetails() != null) {
            Long projectId = releaseView.getProjectDetails().getProjectId();
            List<TestCase> projectTestCases;
            if (moduleId != null && subModuleId != null) {
                projectTestCases = testCaseRepository
                        .findByProjectDetails_ProjectIdAndModule_ModuleIdAndSubModule_SubModuleIdOrderByTestCaseIdAsc(
                                projectId, moduleId, subModuleId);
            } else if (moduleId != null) {
                projectTestCases = testCaseRepository
                        .findByProjectDetails_ProjectIdAndModule_ModuleIdOrderBySubModule_SubModuleIdAscTestCaseIdAsc(
                                projectId, moduleId);
            } else {
                projectTestCases = testCaseRepository
                        .findByProjectDetails_ProjectIdOrderByModule_ModuleIdAscSubModule_SubModuleIdAscTestCaseIdAsc(
                                projectId);
            }

            for (TestCase tc : projectTestCases) {
                if (!releaseTestCaseRepository.existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(releaseId, tc.getTestCaseId())) {
                    ReleaseTestCase rtc = new ReleaseTestCase();
                    rtc.setReleaseView(releaseView);
                    rtc.setTestCase(tc);
                    releaseTestCaseRepository.save(rtc);
                }
            }
        }

        List<ReleaseTestCase> releaseTestCases;

        // Release + Module + Submodule
        if (moduleId != null && subModuleId != null) {

            releaseTestCases =
                    releaseTestCaseRepository
                            .findByReleaseView_ReleaseIdAndTestCase_Module_ModuleIdAndTestCase_SubModule_SubModuleId(
                                    releaseId,
                                    moduleId,
                                    subModuleId
                            );

        }

        // Release + Module
        else if (moduleId != null) {

            releaseTestCases =
                    releaseTestCaseRepository
                            .findByReleaseView_ReleaseIdAndTestCase_Module_ModuleId(
                                    releaseId,
                                    moduleId
                            );

        }

        // Release only
        else {

            releaseTestCases =
                    releaseTestCaseRepository
                            .findByReleaseView_ReleaseId(releaseId);
        }


        // ============================================================
        // 3. MAP RESPONSE
        // ============================================================

        return releaseTestCases.stream()
                .map(entity -> {

                    ReleaseTestCaseResponseDTO dto =
                            releaseTestCaseMapper.toResponse(entity);


                    // ====================================================
                    // 4. DEFAULT QA EXECUTER
                    // ====================================================
                    //
                    // Default:
                    // BenchAllocation -> Employee
                    //
                    // If Defect exists and defect.enterBy has value,
                    // it will override this value below.
                    //
                    // ====================================================

                    if (entity.getBenchAllocation() != null) {

                        Employee employee =
                                entity.getBenchAllocation().getEmployee();

                        if (employee != null) {

                            dto.setEmployeeId(
                                    employee.getEmpId()
                            );

                            dto.setEmployeeName(
                                    buildEmployeeName(employee)
                            );

                            System.out.println(
                                    "👤 Default Executor(QA): "
                                            + dto.getEmployeeName()
                            );

                            System.out.println(
                                    "👤 Default Executor ID: "
                                            + dto.getEmployeeId()
                            );
                        }

                    } else {

                        dto.setEmployeeId(null);
                        dto.setEmployeeName(null);

                        System.out.println(
                                "⚠️ BenchAllocation: NULL"
                        );
                    }


                    // ====================================================
                    // 5. TEST CASE CHECK
                    // ====================================================

                    if (entity.getTestCase() == null) {

                        System.out.println(
                                "❌ Test Case is NULL"
                        );

                        dto.setDefectId(null);
                        dto.setAssignToId(null);
                        dto.setAssignToName(null);

                        return dto;
                    }


                    // ====================================================
                    // 6. GET TEST CASE ID
                    // ====================================================

                    Long testCaseId =
                            entity.getTestCase().getTestCaseId();

                    System.out.println(
                            "\n========================================"
                    );

                    System.out.println(
                            "Release Test Case ID: "
                                    + entity.getReleaseTestCaseId()
                    );

                    System.out.println(
                            "Test Case ID: "
                                    + testCaseId
                    );


                    // ====================================================
                    // 7. FIND DEFECT
                    // ====================================================

                    defectRepository
                            .findByTestCase_TestCaseId(testCaseId)
                            .ifPresentOrElse(defect -> {

                                // ====================================================
                                // 7.1 DEFECT FOUND
                                // ====================================================

                                System.out.println(
                                        "✅ DEFECT FOUND"
                                );


                                // ====================================================
                                // 7.2 DEFECT ID
                                // ====================================================

                                dto.setDefectId(
                                        defect.getDefectId()
                                );

                                System.out.println(
                                        "🐞 Defect ID: "
                                                + defect.getDefectId()
                                );


                                // ====================================================
                                // 7.3 EXECUTER (QA)
                                // ====================================================
                                //
                                // Source:
                                // Defect.enterBy
                                //
                                // UI:
                                // EXECUTER(QA)
                                //
                                // ====================================================

                                if (defect.getEnterBy() != null
                                        && !defect.getEnterBy().isBlank()) {

                                    String executorName =
                                            defect.getEnterBy().trim();

                                    dto.setEmployeeName(
                                            executorName
                                    );

                                    System.out.println(
                                            "👤 Executor(QA) from Defect.enterBy: "
                                                    + executorName
                                    );

                                } else {

                                    System.out.println(
                                            "⚠️ Defect.enterBy is NULL/EMPTY"
                                    );

                                    // Keep BenchAllocation employee
                                    // as the default executor.
                                }


                                // ====================================================
                                // 7.4 ASSIGNED TO
                                // ====================================================
                                //
                                // Source:
                                // Defect.assignTo -> Employee
                                //
                                // ====================================================

                                if (defect.getAssignTo() != null) {

                                    Employee assignedEmployee =
                                            defect.getAssignTo();


                                    // -------------------------------
                                    // Assigned Employee ID
                                    // -------------------------------

                                    dto.setAssignToId(
                                            assignedEmployee.getEmpId()
                                    );


                                    // -------------------------------
                                    // Assigned Employee Name
                                    // -------------------------------

                                    String assignedEmployeeName =
                                            buildEmployeeName(
                                                    assignedEmployee
                                            );


                                    dto.setAssignToName(
                                            assignedEmployeeName
                                    );


                                    System.out.println(
                                            "👨‍💻 Assigned To ID: "
                                                    + assignedEmployee.getEmpId()
                                    );

                                    System.out.println(
                                            "👨‍💻 Assigned To Name: "
                                                    + assignedEmployeeName
                                    );

                                } else {

                                    dto.setAssignToId(null);
                                    dto.setAssignToName(null);

                                    System.out.println(
                                            "⚠️ Assigned To: NULL"
                                    );
                                }


                                // ====================================================
                                // 7.5 PRIORITY
                                // ====================================================

                                if (defect.getPriority() != null) {

                                    dto.setPriorityId(
                                            defect.getPriority()
                                                    .getPriorityId()
                                    );

                                    dto.setPriorityName(
                                            defect.getPriority()
                                                    .getPriorityName()
                                    );

                                    System.out.println(
                                            "🔥 Priority ID: "
                                                    + defect.getPriority()
                                                    .getPriorityId()
                                    );

                                    System.out.println(
                                            "🔥 Priority Name: "
                                                    + defect.getPriority()
                                                    .getPriorityName()
                                    );

                                } else {

                                    dto.setPriorityId(null);
                                    dto.setPriorityName("-");

                                    System.out.println(
                                            "⚠️ Priority: NULL"
                                    );
                                }


                            }, () -> {

                                // ====================================================
                                // 7.6 NO DEFECT FOUND
                                // ====================================================

                                System.out.println(
                                        "❌ NO DEFECT FOUND FOR TEST CASE ID: "
                                                + testCaseId
                                );


                                dto.setDefectId(null);

                                dto.setAssignToId(null);

                                dto.setAssignToName(null);

                                // IMPORTANT:
                                // Do not clear employeeName.
                                //
                                // BenchAllocation employee remains
                                // the default QA Executor.
                            });


                    // ====================================================
                    // 8. FINAL DTO DEBUG
                    // ====================================================

                    System.out.println(
                            "\n========== FINAL DTO DATA =========="
                    );

                    System.out.println(
                            "Release Test Case ID: "
                                    + dto.getReleaseTestCaseId()
                    );

                    System.out.println(
                            "Test Case ID: "
                                    + testCaseId
                    );

                    System.out.println(
                            "Defect ID: "
                                    + dto.getDefectId()
                    );

                    System.out.println(
                            "Executor(QA): "
                                    + dto.getEmployeeName()
                    );

                    System.out.println(
                            "Executor ID: "
                                    + dto.getEmployeeId()
                    );

                    System.out.println(
                            "Assigned To ID: "
                                    + dto.getAssignToId()
                    );

                    System.out.println(
                            "Assigned To Name: "
                                    + dto.getAssignToName()
                    );

                    System.out.println(
                            "Priority ID: "
                                    + dto.getPriorityId()
                    );

                    System.out.println(
                            "Priority Name: "
                                    + dto.getPriorityName()
                    );

                    System.out.println(
                            "========================================\n"
                    );


                    // ====================================================
                    // 9. RETURN DTO
                    // ====================================================

                    // ====================================================
// FINAL DTO CHECK
// ====================================================

                    System.out.println("🔥🔥 FINAL DTO JSON CHECK");
                    System.out.println("ReleaseTestCaseId = " + dto.getReleaseTestCaseId());
                    System.out.println("TestCaseId        = " + dto.getTestCaseId());
                    System.out.println("DefectId          = " + dto.getDefectId());
                    System.out.println("EmployeeId        = " + dto.getEmployeeId());
                    System.out.println("EmployeeName      = " + dto.getEmployeeName());
                    System.out.println("AssignToId        = " + dto.getAssignToId());
                    System.out.println("AssignToName      = " + dto.getAssignToName());
                    System.out.println("PriorityId        = " + dto.getPriorityId());
                    System.out.println("PriorityName      = " + dto.getPriorityName());
                    System.out.println("🔥🔥 END DTO CHECK");


                    return dto;

                })
                .toList();
    }


    /**
     * Build employee full name safely.
     */
    private String buildEmployeeName(Employee employee) {

        if (employee == null) {
            return null;
        }

        String firstName =
                employee.getFirstName() != null
                        ? employee.getFirstName().trim()
                        : "";

        String lastName =
                employee.getLastName() != null
                        ? employee.getLastName().trim()
                        : "";

        String fullName =
                (firstName + " " + lastName).trim();

        return fullName.isEmpty()
                ? null
                : fullName;
    }



    @Override
    @Transactional
    public ReleaseTestCaseResponseDTO updateStatus(
            Long releaseId,
            Long releaseTestCaseId,
            ReleaseTestCaseStatusRequestDTO request,
            MultipartFile attachmentFile) {

        ReleaseTestCase releaseTestCase =
                releaseTestCaseRepository
                        .findByReleaseView_ReleaseIdAndReleaseTestCaseId(
                                releaseId,
                                releaseTestCaseId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Release Test Case not found"
                                ));

        String status = request.getPassOrFail();

        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException(
                    "Pass or Fail status is required"
            );
        }

        status = status.trim().toUpperCase();

        if (!status.equals("PASS") && !status.equals("FAIL")) {
            throw new RuntimeException(
                    "Status must be PASS or FAIL"
            );
        }

        // Update test case status
        releaseTestCase.setPassOrFail(status);

        ReleaseTestCase saved =
                releaseTestCaseRepository.save(releaseTestCase);

        // No defect for PASS
        if ("PASS".equals(status)) {
            return releaseTestCaseMapper.toResponse(saved);
        }

        // Create defect
        DefectRequestDTO defectRequest =
                new DefectRequestDTO();

        var testCase = releaseTestCase.getTestCase();

        defectRequest.setTestCaseId(
                testCase.getTestCaseId()
        );

        String customBriefDesc = request.getBriefDescription();
        defectRequest.setBriefDescription(
                (customBriefDesc != null && !customBriefDesc.trim().isEmpty())
                        ? customBriefDesc.trim()
                        : testCase.getDescription()
        );

        String customSteps = request.getSteps();
        defectRequest.setSteps(
                (customSteps != null && !customSteps.trim().isEmpty())
                        ? customSteps.trim()
                        : testCase.getTestSteps()
        );
        if (testCase.getSeverity() != null) {
            defectRequest.setSeverityId(
                    testCase.getSeverity().getSeverityId()
            );
        }
     else {
        severityRepository.findAllByOrderByWeightAsc().stream()
                .findFirst()
                .ifPresent(sev -> defectRequest.setSeverityId(sev.getSeverityId()));
    }
        if (testCase.getDefectType() != null) {
            defectRequest.setDefectTypeId(
                    testCase.getDefectType().getDefectTypeId()
            );
        }

        // Set default status (New)
        statusTypeRepository.findByStatusNameIgnoreCase("New")
                .or(() -> statusTypeRepository.findAll().stream().findFirst())
                .ifPresent(st -> defectRequest.setStatusTypeId(st.getStatusTypeId()));



        defectRequest.setTestCaseRequired(true);

        // Release
        defectRequest.setReleaseIds(
                List.of(releaseId)
        );

        // Module
        if (releaseTestCase.getTestCase().getModule() != null) {
            defectRequest.setModuleId(
                    releaseTestCase
                            .getTestCase()
                            .getModule()
                            .getModuleId()
            );
        }

        // Sub module
        if (releaseTestCase.getTestCase().getSubModule() != null) {
            defectRequest.setSubModuleId(
                    releaseTestCase
                            .getTestCase()
                            .getSubModule()
                            .getSubModuleId()
            );
        }

        // Project
        if (releaseTestCase.getReleaseView() != null
                && releaseTestCase.getReleaseView().getProjectDetails() != null) {

            defectRequest.setProjectId(
                    releaseTestCase
                            .getReleaseView()
                            .getProjectDetails()
                            .getProjectId()
            );
        }

        // Priority selected from frontend
        defectRequest.setPriorityId(
                request.getPriorityId()
        );

        // Assigned user selected from frontend
        defectRequest.setAssignToId(
                request.getAssignedTo()
        );

        // Get Workflow START status
        Long startStatusId = workFlowService.getStartingStatusTypeId();

        defectRequest.setStatusTypeId(startStatusId);

        // Entered by
        if (releaseTestCase.getBenchAllocation() != null
                && releaseTestCase.getBenchAllocation().getEmployee() != null) {

            defectRequest.setEnterBy(
                    releaseTestCase
                            .getBenchAllocation()
                            .getEmployee()
                            .getFirstName()
            );
        }

        DefectResponseDTO createdDefect =
                defectService.createdefect(
                        defectRequest,
                        attachmentFile
                );

        ReleaseTestCaseResponseDTO response =
                releaseTestCaseMapper.toResponse(saved);

        response.setDefectId(createdDefect.getDefectId());
        response.setAssignToId(createdDefect.getAssignToId());
        response.setAssignToName(createdDefect.getAssignToName());
        response.setPriorityId(createdDefect.getPriorityId());
        response.setPriorityName(createdDefect.getPriorityName());

        return response;
    }

    @Override
    @Transactional
    public ReleaseTestCaseCountResponseDto getTestCaseCount(Long releaseId) {

        var releaseView = releaseViewRepository.findById(releaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release not found"));

        if (releaseView.getProjectDetails() != null) {
            Long projectId = releaseView.getProjectDetails().getProjectId();
            List<TestCase> projectTestCases = testCaseRepository
                    .findByProjectDetails_ProjectIdOrderByModule_ModuleIdAscSubModule_SubModuleIdAscTestCaseIdAsc(projectId);
            for (TestCase tc : projectTestCases) {
                if (!releaseTestCaseRepository.existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(releaseId, tc.getTestCaseId())) {
                    ReleaseTestCase rtc = new ReleaseTestCase();
                    rtc.setReleaseView(releaseView);
                    rtc.setTestCase(tc);
                    releaseTestCaseRepository.save(rtc);
                }
            }
        }

        long count = releaseTestCaseRepository
                .countByReleaseView_ReleaseId(releaseId);

        return new ReleaseTestCaseCountResponseDto(
                releaseId,
                count
        );
    }

    @Override
    @Transactional
    public void updateReleaseStatus(
            Long releaseId,
            ReleaseStatus status) {

        var release = releaseViewRepository.findById(releaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release not found"));

        release.setStatus(status);

        releaseViewRepository.save(release);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCaseResponseDTO> getAvailableTestCases(
            Long releaseId,
            Long projectId,
            Long moduleId,
            Long subModuleId) {

        // Check release
        releaseViewRepository.findById(releaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release not found"));

        // Get only test cases which are NOT already allocated
        List<TestCase> testCases =
                releaseTestCaseRepository.findAvailableTestCasesForRelease(
                        releaseId,
                        projectId,
                        moduleId,
                        subModuleId
                );

        return testCases.stream()
                .map(testCaseMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteReleaseTestCase(Long releaseId, Long releaseTestCaseId) {
        ReleaseTestCase releaseTestCase = releaseTestCaseRepository
                .findByReleaseView_ReleaseIdAndReleaseTestCaseId(releaseId, releaseTestCaseId)
                .orElseThrow(() ->
                        new RuntimeException("Release Test Case not found with ID: " + releaseTestCaseId));

        releaseTestCaseRepository.delete(releaseTestCase);
    }

}
