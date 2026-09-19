package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefectDensityResponseDto {

    private Double kloc;

    private Long totalDefects;

    private Double defectDensity;
}