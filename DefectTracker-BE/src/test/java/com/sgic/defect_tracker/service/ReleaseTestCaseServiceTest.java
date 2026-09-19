package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseEmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.QAEmployeeResponseDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTestCaseCountResponseDto;
import com.sgic.defect_tracker.dtos.response.ReleaseTestCaseResponseDTO;
import com.sgic.defect_tracker.entities.*;
import com.sgic.defect_tracker.mapper.QAEmployeeMapper;
import com.sgic.defect_tracker.mapper.ReleaseTestCaseMapper;
import com.sgic.defect_tracker.mapper.TestCaseMapper;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.service.impl.ReleaseTestCaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReleaseTestCaseServiceTest {

    @Mock
    private ReleaseTestCaseRepository releaseTestCaseRepository;
    @Mock
    private ReleaseViewRepository releaseViewRepository;
    @Mock
    private TestCaseRepository testCaseRepository;
    @Mock
    private ReleaseTestCaseMapper releaseTestCaseMapper;
    @Mock
    private TestCaseMapper testCaseMapper;
    @Mock
    private DefectService defectService;
    @Mock
    private BenchAllocationRepository benchAllocationRepository;
    @Mock
    private QAEmployeeMapper qaEmployeeMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmailNotificationService notificationService;
    @Mock
    private DefectRepository defectRepository;
    @Mock
    private WorkFlowService workFlowService;
    @Mock
    private SeverityRepository severityRepository;
    @Mock
    private StatusTypeRepository statusTypeRepository;

    @InjectMocks
    private ReleaseTestCaseServiceImpl releaseTestCaseService;

    private ProjectDetails project;
    private ReleaseView release;
    private TestCase testCase;
    private Employee qaLead;
    private Employee qaEngineer;
    private Role qaLeadRole;
    private Role qaEngineerRole;
    private BenchAllocation qaLeadAlloc;
    private BenchAllocation qaEngAlloc;
    private ReleaseTestCase releaseTestCase;

    @BeforeEach
    void setUp() {
        project = new ProjectDetails();
        project.setProjectId(1L);
        project.setProjectName("Defect Tracker Pro");

        release = new ReleaseView();
        release.setReleaseId(10L);
        release.setReleaseName("Release 1.0");
        release.setProjectDetails(project);

        testCase = new TestCase();
        testCase.setTestCaseId(100L);
        testCase.setTestCaseName("TC-001");
        testCase.setDescription("Test Login functionality");
        testCase.setProjectDetails(project);

        qaLead = new Employee();
        qaLead.setEmpId(1001L);
        qaLead.setFirstName("Sarah");
        qaLead.setLastName("Wilson");
        qaLead.setEmail("sarah.wilson@company.com");
        qaLead.setIsActive(true);

        qaEngineer = new Employee();
        qaEngineer.setEmpId(1002L);
        qaEngineer.setFirstName("David");
        qaEngineer.setLastName("Kim");
        qaEngineer.setEmail("david.kim@company.com");
        qaEngineer.setIsActive(true);

        qaLeadRole = new Role();
        qaLeadRole.setRoleId(2L);
        qaLeadRole.setRoleName("QA Lead");
        qaLeadRole.setRoleType("QA_LEAD");

        qaEngineerRole = new Role();
        qaEngineerRole.setRoleId(3L);
        qaEngineerRole.setRoleName("QA Engineer");
        qaEngineerRole.setRoleType("QA_ENGINEER");

        qaLeadAlloc = new BenchAllocation();
        qaLeadAlloc.setBenchAllocationId(501L);
        qaLeadAlloc.setEmployee(qaLead);
        qaLeadAlloc.setRole(qaLeadRole);
        qaLeadAlloc.setProjectDetails(project);

        qaEngAlloc = new BenchAllocation();
        qaEngAlloc.setBenchAllocationId(502L);
        qaEngAlloc.setEmployee(qaEngineer);
        qaEngAlloc.setRole(qaEngineerRole);
        qaEngAlloc.setProjectDetails(project);

        releaseTestCase = new ReleaseTestCase();
        releaseTestCase.setReleaseTestCaseId(1L);
        releaseTestCase.setReleaseView(release);
        releaseTestCase.setTestCase(testCase);
    }

    @Test
    @DisplayName("getAvailableQAEmployees should return only QA Lead and QA Engineer allocations")
    void testGetAvailableQAEmployees_ReturnsQaLeadAndQaEngineer() {
        when(releaseViewRepository.findById(10L)).thenReturn(Optional.of(release));
        when(testCaseRepository.findById(100L)).thenReturn(Optional.of(testCase));
        when(benchAllocationRepository.findQaAllocationsByProject(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(qaLeadAlloc, qaEngAlloc));

        List<QAEmployeeResponseDTO> result = releaseTestCaseService.getAvailableQAEmployees(10L, 100L);

        assertNotNull(result);
        assertEquals(2, result.size());

        List<String> roleTypes = result.stream().map(QAEmployeeResponseDTO::getRoleType).toList();
        assertTrue(roleTypes.contains("QA_LEAD"));
        assertTrue(roleTypes.contains("QA_ENGINEER"));
    }

    @Test
    @DisplayName("getAvailableQAEmployees should throw exception if test case does not belong to release project")
    void testGetAvailableQAEmployees_MismatchProject_ThrowsException() {
        ProjectDetails anotherProject = new ProjectDetails();
        anotherProject.setProjectId(999L);
        testCase.setProjectDetails(anotherProject);

        when(releaseViewRepository.findById(10L)).thenReturn(Optional.of(release));
        when(testCaseRepository.findById(100L)).thenReturn(Optional.of(testCase));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                releaseTestCaseService.getAvailableQAEmployees(10L, 100L)
        );
        assertEquals("Test Case does not belong to this project", exception.getMessage());
    }

    @Test
    @DisplayName("assignQAEmployee should successfully allocate QA member with active QA allocation")
    void testAssignQAEmployee_Success() {
        ReleaseTestCaseEmployeeRequestDTO request = new ReleaseTestCaseEmployeeRequestDTO();
        request.setEmployeeId(1001L);

        when(releaseTestCaseRepository.findByReleaseView_ReleaseIdAndTestCase_TestCaseId(10L, 100L))
                .thenReturn(Optional.of(releaseTestCase));
        when(employeeRepository.findById(1001L)).thenReturn(Optional.of(qaLead));
        when(benchAllocationRepository.findQaAllocationsByProject(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(qaLeadAlloc, qaEngAlloc));
        when(releaseTestCaseRepository.save(any(ReleaseTestCase.class))).thenReturn(releaseTestCase);

        ReleaseTestCaseResponseDTO expectedResponse = new ReleaseTestCaseResponseDTO();
        expectedResponse.setReleaseTestCaseId(1L);
        expectedResponse.setEmployeeName("Sarah Wilson");
        when(releaseTestCaseMapper.toResponse(any(ReleaseTestCase.class))).thenReturn(expectedResponse);

        ReleaseTestCaseResponseDTO response = releaseTestCaseService.assignQAEmployee(10L, 100L, request);

        assertNotNull(response);
        assertEquals(1L, response.getReleaseTestCaseId());
        assertEquals("Sarah Wilson", response.getEmployeeName());
        verify(releaseTestCaseRepository, times(1)).save(any(ReleaseTestCase.class));
    }

    @Test
    @DisplayName("assignQAEmployee should throw exception if employee is not allocated as QA Lead or QA Engineer")
    void testAssignQAEmployee_NotQAAllocation_ThrowsException() {
        Employee devEmployee = new Employee();
        devEmployee.setEmpId(2001L);
        devEmployee.setFirstName("John");
        devEmployee.setLastName("Developer");
        devEmployee.setIsActive(true);

        ReleaseTestCaseEmployeeRequestDTO request = new ReleaseTestCaseEmployeeRequestDTO();
        request.setEmployeeId(2001L);

        when(releaseTestCaseRepository.findByReleaseView_ReleaseIdAndTestCase_TestCaseId(10L, 100L))
                .thenReturn(Optional.of(releaseTestCase));
        when(employeeRepository.findById(2001L)).thenReturn(Optional.of(devEmployee));
        when(benchAllocationRepository.findQaAllocationsByProject(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(qaLeadAlloc, qaEngAlloc)); // Only Sarah and David are QA

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                releaseTestCaseService.assignQAEmployee(10L, 100L, request)
        );
        assertEquals("Employee is not an available QA Engineer or QA Lead for this project", exception.getMessage());
    }

    @Test
    @DisplayName("getTestCaseCount should return correct count for release")
    void testGetTestCaseCount_Success() {
        when(releaseViewRepository.findById(10L)).thenReturn(Optional.of(release));
        when(releaseTestCaseRepository.countByReleaseView_ReleaseId(10L)).thenReturn(5L);

        ReleaseTestCaseCountResponseDto countDto = releaseTestCaseService.getTestCaseCount(10L);

        assertNotNull(countDto);
        assertEquals(10L, countDto.getReleaseId());
        assertEquals(5L, countDto.getTestCaseCount());
    }
}
