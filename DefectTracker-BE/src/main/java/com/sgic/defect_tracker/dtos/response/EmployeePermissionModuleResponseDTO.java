package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePermissionModuleResponseDTO {
    private String module;
    private List<EmployeePermissionItemResponseDTO> permissions;
}