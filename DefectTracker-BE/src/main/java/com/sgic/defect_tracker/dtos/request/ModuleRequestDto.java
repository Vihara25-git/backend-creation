package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModuleRequestDto {
    @Size(max = 30, message = "Module name must be within 30 characters")
    private String moduleName;

    //private Long defectId;
    //private Long subModuleId;
    //private Long projectId;
    //private Long EmpId;

}
