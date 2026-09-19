package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserPermissionsDTO {

    private Long employeeId;
    private String email;
    private boolean admin;
    private Set<String> permissions;
}