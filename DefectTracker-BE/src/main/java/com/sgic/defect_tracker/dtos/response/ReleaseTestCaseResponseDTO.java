package com.sgic.defect_tracker.dtos.response;

import com.sgic.defect_tracker.utils.ReleaseStatus;
import lombok.Data;

@Data
public class ReleaseTestCaseResponseDTO {

    private Long releaseTestCaseId;

    // Test Case details
    private Long testCaseId;
    private String testCaseName;
    private String description;
    private String testSteps;
    private ReleaseStatus status;

    // Module details
    private Long moduleId;
    private String moduleName;

    // Sub Module details
    private Long subModuleId;
    private String subModuleName;

    // Severity
    private Long severityId;
    private String severityName;

    // Defect Type
    private Long defectTypeId;
    private String defectTypeName;

    // Priority
    private Long priorityId;
    private String priorityName;

    // Release Test Case
    private String passOrFail;

    // Bench allocation
    private Long benchAllocationId;
    private Long employeeId;
    private String employeeName;

    // Defect details
    private Long defectId;

    // Assigned employee
    private Long assignToId;
    private String assignToName;
}