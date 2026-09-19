package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefectTypeDashboardResponseDto {

    private List<DefectTypeItemDto> defectTypes;

    private long totalDefectCount;

    private String mostCommonDefectType;

    private long mostCommonDefectCount;
}