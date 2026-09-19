package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeverityBreakdownItemDTO {
    private Long severityId;
    private String severityName;
    private String severityColor;
    private Long totalDefects;
    private Map<String, Long> statusCounts;
}
