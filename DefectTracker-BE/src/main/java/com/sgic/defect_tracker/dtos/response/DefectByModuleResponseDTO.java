package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefectByModuleResponseDTO {

    private String moduleName;
    private Long defectCount;
    private Double percentage;
}