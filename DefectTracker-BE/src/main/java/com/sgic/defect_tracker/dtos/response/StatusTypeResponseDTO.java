package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data

public class StatusTypeResponseDTO {


    private Long statusTypeId;
    private String statusName;

    private String statusType;

    private String colorCode;


}
