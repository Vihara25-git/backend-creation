package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class WorkFlowRequestDTO {

    private Long fromStatusId;
    private Long toStatusId;
}