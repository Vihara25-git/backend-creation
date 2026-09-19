package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.util.List;

@Data
public class ModuleResponseDto {

    private Long moduleId;
    private String moduleName;


    private Long subModuleId;
    private String subModuleName;

    private Long projectId;
    private String projectName;

    private List<SubmoduleResponseDTO> subModules;
}
