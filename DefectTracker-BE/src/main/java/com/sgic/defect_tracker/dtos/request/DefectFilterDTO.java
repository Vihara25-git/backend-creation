package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class DefectFilterDTO {

    private Long projectId;

    private Long statusTypeId;

    private Long severityId;

    private Long priorityId;

    private Long defectTypeId;

    private Long moduleId;

    private Long subModuleId;

    // Search text
    private String search;
}