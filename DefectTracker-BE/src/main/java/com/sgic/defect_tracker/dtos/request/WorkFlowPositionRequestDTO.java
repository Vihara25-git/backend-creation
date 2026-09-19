package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class WorkFlowPositionRequestDTO {

    private Long statusTypeId;
    private Double positionX;
    private Double positionY;
}