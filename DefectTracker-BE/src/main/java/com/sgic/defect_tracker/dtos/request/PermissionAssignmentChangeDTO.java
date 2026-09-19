
package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class PermissionAssignmentChangeDTO {
    private String permissionId;   // template_id as string (frontend sends string)
    private Boolean isAssigned;
}