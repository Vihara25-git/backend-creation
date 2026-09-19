package com.sgic.defect_tracker.service.impl;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.entities.DefectHistory;
import com.sgic.defect_tracker.entities.ReleaseView;
import com.sgic.defect_tracker.repositories.DefectHistoryRepository;
import com.sgic.defect_tracker.repositories.DefectHistoryRepository;
import com.sgic.defect_tracker.dtos.response.DashboardResponseDto;
import com.sgic.defect_tracker.repositories.ReleaseViewRepository;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.service.DashboardService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.repositories.DashboardRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.sgic.defect_tracker.dtos.response.DefectTypeDashboardResponseDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Data
@AllArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final DefectRepository defectRepository;
    private final DefectHistoryRepository defectHistoryRepository;
    private final ReleaseViewRepository releaseViewRepository;

    // =========================================================
    // TIME TO FIND
    // =========================================================
    @Override
    public List<DashboardResponseDto> getTimeToFind(
            Long projectId,
            Long releaseId) {

        List<Defect> defects;

        if (releaseId == null) {
            defects = defectRepository.findByProjectDetailsProjectId(projectId);
        } else {
            defects = defectRepository
                    .findByProjectDetailsProjectIdAndReleaseViewsReleaseId(
                            projectId,
                            releaseId
                    );
        }

        if (defects.isEmpty()) {
            return new ArrayList<>();
        }

        // Find the first defect creation date.
        LocalDate firstDefectDate = defects.stream()
                .filter(defect -> defect.getCreatedAt() != null)
                .map(defect -> defect.getCreatedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(null);

        if (firstDefectDate == null) {
            return new ArrayList<>();
        }

        Map<Long, Long> dayCountMap = new TreeMap<>();

        for (Defect defect : defects) {

            if (defect.getCreatedAt() == null) {
                continue;
            }

            LocalDate createdDate = defect.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            long dayNumber = ChronoUnit.DAYS.between(
                    firstDefectDate,
                    createdDate
            ) + 1;

            dayCountMap.merge(
                    dayNumber,
                    1L,
                    Long::sum
            );
        }

        List<DashboardResponseDto> result = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : dayCountMap.entrySet()) {

            DashboardResponseDto dto =
                    new DashboardResponseDto();

            dto.setDay("Day " + entry.getKey());
            dto.setCount(entry.getValue());

            result.add(dto);
        }

        return result;
    }

    // =========================================================
    // TIME TO FIX
    // =========================================================
    @Override
    public List<DashboardResponseDto> getTimeToFix(
            Long projectId,
            Long releaseId) {

        List<Defect> defects;

        ReleaseView selectedRelease = null;

        if (releaseId == null) {

            // All Releases
            defects = defectRepository
                    .findByProjectDetailsProjectId(projectId);

        } else {

            // Specific Release
            selectedRelease =
                    releaseViewRepository.findById(releaseId)
                            .orElseThrow(() ->
                                    new RuntimeException("Release not found"));

            defects = defectRepository
                    .findByProjectDetailsProjectIdAndReleaseViewsReleaseId(
                            projectId,
                            releaseId
                    );
        }

        Map<Long, Long> dayCountMap = new TreeMap<>();

        for (Defect defect : defects) {

            List<DefectHistory> histories =
                    defectHistoryRepository
                            .findByDefect_DefectIdOrderByDefectDateDescDefectTimeDesc(
                                    defect.getDefectId()
                            );

            for (DefectHistory history : histories) {

                String status = history.getDefectStatus();

                if (status == null) {
                    continue;
                }

                String normalizedStatus = status.toLowerCase();

                boolean isFixedStatus =
                        normalizedStatus.contains("fixed")
                                || normalizedStatus.contains("closed")
                                || normalizedStatus.contains("resolved");

                if (!isFixedStatus) {
                    continue;
                }

                if (history.getDefectDate() == null) {
                    continue;
                }

                LocalDate releaseStartDate = null;

                if (releaseId != null) {

                    releaseStartDate =
                            selectedRelease.getReleaseDate();

                } else {

                    if (defect.getReleaseViews() != null
                            && !defect.getReleaseViews().isEmpty()) {

                        ReleaseView release =
                                defect.getReleaseViews().get(0);

                        if (release != null) {
                            releaseStartDate =
                                    release.getReleaseDate();
                        }
                    }
                }

                if (releaseStartDate == null) {
                    continue;
                }

                long dayNumber =
                        ChronoUnit.DAYS.between(
                                releaseStartDate,
                                history.getDefectDate()
                        ) + 1;

                if (dayNumber < 1) {
                    dayNumber = 1;
                }

                dayCountMap.merge(
                        dayNumber,
                        1L,
                        Long::sum
                );

                break;
            }
        }

        List<DashboardResponseDto> result =
                new ArrayList<>();

        for (Map.Entry<Long, Long> entry :
                dayCountMap.entrySet()) {

            DashboardResponseDto dto =
                    new DashboardResponseDto();

            dto.setDay("Day " + entry.getKey());
            dto.setCount(entry.getValue());

            result.add(dto);
        }

        return result;
    }
    // =========================================================
    // DEFECT TO REMARK RATIO
    // =========================================================
    @Override
    public DashboardResponseDto getDefectToRemarkRatio(Long projectId) {

        List<Object[]> results =
                defectRepository.countDefectsByProjectAndStatusType(projectId);

        long totalRemark = 0;
        long duplicateCount = 0;
        long rejectCount = 0;

        for (Object[] row : results) {

            String statusType = (String) row[0];
            long count = ((Number) row[1]).longValue();

            totalRemark += count;

            if ("DUPLICATE".equalsIgnoreCase(statusType)) {
                duplicateCount = count;
            }

            if ("REJECTED".equalsIgnoreCase(statusType)) {
                rejectCount = count;
            }
        }

        long totalDefects =
                totalRemark - duplicateCount - rejectCount;

        double ratio = 0.0;

        if (totalRemark > 0) {
            ratio = ((double) totalDefects / totalRemark) * 100;
        }

        DashboardResponseDto response =
                new DashboardResponseDto();

        response.setTotalDefects(totalDefects);
        response.setDuplicateCount(duplicateCount);
        response.setRejectCount(rejectCount);
        response.setTotalRemark(totalRemark);
        response.setRatio(ratio);
        return response;
    }

    // =========================================================
    // DEFECT SEVERITY INDEX
    // =========================================================
    @Override
    public DashboardResponseDto getDefectSeverityIndex(Long projectId) {

        List<Object[]> results =
                defectRepository.countDefectsByProjectAndSeverity(projectId);

        long criticalCount = 0;
        long highCount = 0;
        long mediumCount = 0;
        long lowCount = 0;

        for (Object[] row : results) {

            String severity = (String) row[0];
            long count = ((Number) row[1]).longValue();

            if ("CRITICAL".equalsIgnoreCase(severity)) {
                criticalCount = count;
            } else if ("HIGH".equalsIgnoreCase(severity)) {
                highCount = count;
            } else if ("MEDIUM".equalsIgnoreCase(severity)) {
                mediumCount = count;
            } else if ("LOW".equalsIgnoreCase(severity)) {
                lowCount = count;
            }
        }

        long totalDefects =
                criticalCount + highCount + mediumCount + lowCount;

        double severityIndex = 0.0;

        if (totalDefects > 0) {
            severityIndex =
                    ((criticalCount * 4.0)
                            + (highCount * 3.0)
                            + (mediumCount * 2.0)
                            + (lowCount * 1.0))
                            / totalDefects;
        }

        severityIndex =
                Math.round(severityIndex * 100.0) / 100.0;

        String riskLevel;

        if (severityIndex >= 3.0) {
            riskLevel = "High Risk";
        } else if (severityIndex >= 2.0) {
            riskLevel = "Medium Risk";
        } else {
            riskLevel = "Low Risk";
        }

        DashboardResponseDto response =
                new DashboardResponseDto();

        response.setCriticalDefects(criticalCount);
        response.setHighDefects(highCount);
        response.setMediumDefects(mediumCount);
        response.setLowDefects(lowCount);
        response.setTotalDefects(totalDefects);
        response.setDefectSeverityIndex(severityIndex);
        response.setRiskLevel(riskLevel);

        return response;
    }

    // =========================================================
    // DEFECTS REOPENED MULTIPLE TIMES
    // =========================================================

    @Override
    public List<Long> getDefectsReopenedMultipleTimes(Long projectId) {

        List<Object[]> results =
                defectHistoryRepository
                        .findDefectsReopenedMultipleTimes(projectId);

        List<Long> defectIds = new ArrayList<>();

        for (Object[] row : results) {

            defectIds.add(
                    ((Number) row[0]).longValue()
            );
        }

        return defectIds;
    }


    // =========================================================
    // REOPENED MULTIPLE TIMES PIE CHART SUMMARY
    // =========================================================

    @Override
    public DashboardResponseDto getReopenedMultipleTimesSummary(
            Long projectId) {

        // Total defects in the selected project
        long totalDefects =
                defectRepository.countDefectsByProject(projectId);

        // Defects which were reopened more than once
        List<Long> reopenedDefects =
                defectHistoryRepository
                        .countDefectsReopenedMultipleTimes(projectId);

        long reopenedMultipleTimesCount = reopenedDefects.size();

        // Remaining defects
        long notReopenedMultipleTimesCount = totalDefects - reopenedMultipleTimesCount;

        DashboardResponseDto response =
                new DashboardResponseDto();

        response.setTotalDefects(totalDefects);

        response.setReopenedMultipleTimesCount(
                reopenedMultipleTimesCount
        );

        response.setNotReopenedMultipleTimesCount(
                notReopenedMultipleTimesCount
        );

        return response;
    }

    @Override
    public DefectTypeDashboardResponseDto getDefectDistributionByType(Long projectId) {

        List<Object[]> results =
                defectRepository.getDefectCountByType(projectId);

        long totalDefectCount = 0;

        for (Object[] row : results) {
            Number count = (Number) row[1];
            totalDefectCount += count.longValue();
        }

        List<DefectTypeItemDto> defectTypes = new ArrayList<>();

        for (Object[] row : results) {

            String defectTypeName = (String) row[0];

            Number countValue = (Number) row[1];
            long defectCount = countValue.longValue();

            double percentage = totalDefectCount > 0
                    ? (defectCount * 100.0) / totalDefectCount
                    : 0.0;

            defectTypes.add(
                    new DefectTypeItemDto(
                            defectTypeName,
                            defectCount,
                            percentage
                    )
            );
        }

        String mostCommonDefectType = "";
        long mostCommonDefectCount = 0;

        if (!defectTypes.isEmpty()) {

            DefectTypeItemDto mostCommon = defectTypes.get(0);

            mostCommonDefectType = mostCommon.getDefectTypeName();
            mostCommonDefectCount = mostCommon.getDefectCount();
        }

        return new DefectTypeDashboardResponseDto(
                defectTypes,
                totalDefectCount,
                mostCommonDefectType,
                mostCommonDefectCount
        );
    }
}