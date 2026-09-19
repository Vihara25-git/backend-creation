package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.util.List;

@Data
public class SubmoduleResponseDTO {
    private Long subModuleId;
    private String subModuleName;
    private Long submoduleDevId;
    private Long moduleId;
    private List<Long> developerIds;
}
