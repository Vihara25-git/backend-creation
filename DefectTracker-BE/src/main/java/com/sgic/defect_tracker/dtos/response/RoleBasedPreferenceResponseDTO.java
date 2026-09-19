package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class RoleBasedPreferenceResponseDTO {

    private Long roleBasedId;

    private Long roleId;

    private String roleName;

    private Long templateId;

    private String emailNotificationType;

    private Boolean status;

    private String channel;
}