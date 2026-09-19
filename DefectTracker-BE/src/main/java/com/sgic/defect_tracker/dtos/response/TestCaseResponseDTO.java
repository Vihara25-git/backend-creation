package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class TestCaseResponseDTO {

    private Long testCaseId;
    private Long testCaseNumber;

    private String testCaseName;

    private String description;

    private String testSteps;

    private Long defectTypeId;
    private String defectTypeName;

    private Long projectId;
    private String projectName;

    private Long moduleId;
    private String moduleName;

    private Long subModuleId;
    private String subModuleName;

    private Long severityId;
    private String severityName;


}
