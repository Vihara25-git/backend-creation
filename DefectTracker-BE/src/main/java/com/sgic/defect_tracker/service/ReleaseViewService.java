package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.ReleaseViewRequestDto;
import com.sgic.defect_tracker.dtos.response.ReleaseViewResponseDto;
import com.sgic.defect_tracker.utils.ReleaseStatus;


import java.util.List;

public interface ReleaseViewService {

    // POST
    ReleaseViewResponseDto saveReleaseView(
            ReleaseViewRequestDto releaseViewRequestDto
    );

    // DELETE
    void deleteReleaseView(Long releaseId);

    // GET all ReleaseViews
    List<ReleaseViewResponseDto> getAllReleaseView();

    // GET ReleaseView by ID
    ReleaseViewResponseDto getByReleaseId(Long releaseId);

    // UPDATE
    ReleaseViewResponseDto updateReleaseView(
            Long releaseId,
            ReleaseViewRequestDto releaseViewRequestDto
    );

    // GET ReleaseViews by Project ID
    List<ReleaseViewResponseDto> getReleaseViewsByProjectId(
            Long projectId
    );

    void updateReleaseStatus(
            Long releaseId,
            ReleaseStatus status
    );
}