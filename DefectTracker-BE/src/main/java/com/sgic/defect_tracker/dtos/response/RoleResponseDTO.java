package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class RoleResponseDTO {

    private Long roleId;
    private String roleName;
    private String roleType;
}
