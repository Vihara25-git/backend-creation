package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.response.DashboardResponseDto;
import com.sgic.defect_tracker.service.DashboardService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.sgic.defect_tracker.dtos.response.DefectTypeDashboardResponseDto;

import java.util.List;

@Controller
@RequestMapping(EndpointBundle.dashboard)
@Data
@AllArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    //defect to remark ratio
    @GetMapping("/remark-ratio")
    public ResponseEntity<DashboardResponseDto> getDefectToRemarkRatio(@PathVariable("projectId")  Long projectId){
        return ResponseEntity.ok(dashboardService.getDefectToRemarkRatio(projectId));
    }

    //Defect Severity Index
    @GetMapping("/severity-index")
    public ResponseEntity<DashboardResponseDto> getDefectSeverityIndex(
            @PathVariable("projectId")  Long projectId
    ){
        return ResponseEntity.ok(dashboardService.getDefectSeverityIndex(projectId));
    }
    @GetMapping("/reopened")
    public ResponseEntity<List<Long>> getDefectsReopenedMultipleTimes(
            @PathVariable ("projectId")  Long projectId) {

        return ResponseEntity.ok(
                dashboardService.getDefectsReopenedMultipleTimes(projectId)
        );

    }
    @GetMapping("/reopened-summary")
    public ResponseEntity<DashboardResponseDto> getReopenedMultipleTimesSummary(
            @PathVariable ("projectId")  Long projectId) {

        return ResponseEntity.ok(
                dashboardService.getReopenedMultipleTimesSummary(projectId)
        );
    }
    // Defect Distribution by Type
    @GetMapping("/defect-type")
    public ResponseEntity<DefectTypeDashboardResponseDto> getDefectDistributionByType(
            @PathVariable("projectId") Long projectId) {

        return ResponseEntity.ok(
                dashboardService.getDefectDistributionByType(projectId)
        );
    }
    // All Releases - Time to Find
    @GetMapping("/time-to-find")
    public ResponseEntity<List<DashboardResponseDto>> getTimeToFindAll(
            @PathVariable("projectId") Long projectId) {

        return ResponseEntity.ok(
                dashboardService.getTimeToFind(
                        projectId,
                        null
                )
        );
    }

    // All Releases - Time to Fix
    @GetMapping("/time-to-fixed")
    public ResponseEntity<List<DashboardResponseDto>> getTimeToFixAll(
            @PathVariable("projectId") Long projectId) {

        return ResponseEntity.ok(
                dashboardService.getTimeToFix(
                        projectId,
                        null
                )
        );
    }

    }

