package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class WorkFlowResponseDTO {
    private Long workflowId;
    private Long statusTypeId1;
    private Long statusTypeId2;
}
