package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.response.DashboardResponseDto;
import com.sgic.defect_tracker.dtos.response.DefectDailyCountResponseDTO;
import com.sgic.defect_tracker.service.DashboardService;
import com.sgic.defect_tracker.service.DefectService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping(EndpointBundle.dashboardByRelease)
@RequiredArgsConstructor
public class ReleaseDashboardController {

    private final DashboardService dashboardService;

    // Specific Release - Time to Find
    @GetMapping("/time-to-find")
    public List<DashboardResponseDto> getTimeToFind(
            @PathVariable Long projectId,
            @PathVariable Long releaseId) {

        return dashboardService.getTimeToFind(
                projectId,
                releaseId
        );
    }

    // Specific Release - Time to Fix
    @GetMapping("/time-to-fixed")
    public List<DashboardResponseDto> getTimeToFix(
            @PathVariable Long projectId,
            @PathVariable Long releaseId) {

        return dashboardService.getTimeToFix(
                projectId,
                releaseId
        );
    }
}