package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.response.DefectDensityResponseDto;

public interface DefectDensityService {

    DefectDensityResponseDto getDefectDensity(Long projectId);
}