package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class RoleBasedPreferenceRequestDTO {

    private Long roleId;

    private Long templateId;

    private Boolean status;

    private String channel;
}