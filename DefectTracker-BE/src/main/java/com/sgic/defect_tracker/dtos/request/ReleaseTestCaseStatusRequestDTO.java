package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class ReleaseTestCaseStatusRequestDTO {

    private String passOrFail;

    private Long priorityId;

    private Long assignedTo;

    private String briefDescription;

    private String steps;
}