package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.response.DefectDensityResponseDto;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.DefectDensityService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointBundle.PROJECT)
public class DefectDensityController {

    @Autowired
    private DefectDensityService defectDensityService;

    @GetMapping("/{id}/defect-density")
    public ResponseEntity<ResponseWrapper<DefectDensityResponseDto>> getDefectDensity(
            @PathVariable Long id) {

        DefectDensityResponseDto result =
                defectDensityService.getDefectDensity(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Defect density retrieved successfully",
                        result
                )
        );
    }
}