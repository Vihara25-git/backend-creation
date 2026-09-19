package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    private String day;
    private Long count;

    private long totalRemark;
    private long duplicateCount;
    private long rejectCount;
    private long totalDefects;
    private double ratio;

    private Long projectId;
    private Long criticalDefects;
    private Long highDefects;
    private Long mediumDefects;
    private Long lowDefects;
    private Double defectSeverityIndex;
    private String riskLevel;

    private long reopenedMultipleTimesCount;
    private long notReopenedMultipleTimesCount;

}