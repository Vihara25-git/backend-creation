package com.sgic.defect_tracker.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryResponseDto {

    private Long id;

    private Long defectId;

    private String assignedByName;

    private String assignedToName;

    private String defectDate;

    private String defectTime;

    private String previousStatus;

    private String defectStatus;

    private String name;

    private String updatedBy;

    private String releaseName;
}