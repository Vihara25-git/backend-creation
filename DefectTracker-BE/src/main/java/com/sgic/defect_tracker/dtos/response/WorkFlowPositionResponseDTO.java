package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class WorkFlowPositionResponseDTO {

    private Long id;
    private Long statusTypeId;
    private Double positionX;
    private Double positionY;
}