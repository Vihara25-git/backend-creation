package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.response.DashboardResponseDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeDashboardResponseDto;

import java.util.List;

public interface DashboardService {
    DashboardResponseDto getDefectToRemarkRatio(Long projectId);

    DashboardResponseDto getDefectSeverityIndex(Long projectId);

    List<Long> getDefectsReopenedMultipleTimes(Long projectId);

    DashboardResponseDto getReopenedMultipleTimesSummary(Long projectId);

    DefectTypeDashboardResponseDto getDefectDistributionByType(Long projectId);

    List<DashboardResponseDto> getTimeToFind(
            Long projectId,
            Long releaseId
    );

    List<DashboardResponseDto> getTimeToFix(
            Long projectId,
            Long releaseId
    );
}
