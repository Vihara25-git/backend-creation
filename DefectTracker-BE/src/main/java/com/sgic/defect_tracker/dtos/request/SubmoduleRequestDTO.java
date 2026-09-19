package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class SubmoduleRequestDTO {

    private String subModuleName;
    private Long submoduleDevId;
    private Long moduleId;
    
}
