package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.util.List;

@Data
public class EmployeePermissionUpdateRequestDTO {
    private List<Long> permissionIds;
}