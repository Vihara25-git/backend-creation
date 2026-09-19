package com.sgic.defect_tracker.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryRequestDto {

    private Long defectId;

    private Long assignedById;

    private Long assignedToId;

    private String previousStatus;

    private String defectStatus;

    private String name;

    private Long updatedById;
}