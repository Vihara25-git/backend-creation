package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class UserBasedPerferencesRequestDTO {

    private Long empId;

    private Long templateId;

    private Boolean status;
}