package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModulePermissionResponseDTO {
    private String module;                      // = privilege_template.privileges_type
    private List<PermissionResponseDTO> permissions;
}