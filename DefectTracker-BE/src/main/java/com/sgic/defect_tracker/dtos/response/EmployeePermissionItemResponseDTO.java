package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePermissionItemResponseDTO {
    private Long permissionId;
    private String action;
    private String description;
    private Boolean checked;
    private Boolean inheritedFromRole;
}