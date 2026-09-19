package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefectSeverityBreakdownResponseDTO {
    private Long totalRemark;
    private Long totalDefects;
    private List<SeverityBreakdownItemDTO> severities;
}
