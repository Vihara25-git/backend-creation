package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private Long permissionId;   // = privilege_template.template_id
    private String action;       // = privilege_template.subject
    private String description;
}