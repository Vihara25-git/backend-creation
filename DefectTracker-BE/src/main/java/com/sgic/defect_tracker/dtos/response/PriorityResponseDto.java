package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class PriorityResponseDto {
    private Long priorityId;

    private String priorityName;

    private String colorCode;
}
