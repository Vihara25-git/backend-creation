package com.sgic.defect_tracker.dtos.response;

import com.sgic.defect_tracker.entities.ProjectDetails;
import lombok.Data;

@Data
public class DefectResponseDTO {

    private Long defectId;

    private String briefDescription;

    private String steps;

    private String attachmentImage;

    private Boolean testCaseRequired;

    private Long moduleId;
    private String moduleName;

    private Long subModuleId;

    private String subModuleName;

    private Long defectTypeId;
    private String defectTypeName;

    private Long releaseId;
    private String releaseName;

    private Long severityId;
    private String severityName;

    private Long priorityId;

    private String priorityName;

    private Long statusTypeId;

    private String statusName;

    private Long projectId;

    private String projectName;

    private Long assignToId;
    private String assignToName;

    private String enterBy;

    // ui ordered number for defect
    private Long projectDefectNumber;

    private Long testCaseId;
}
