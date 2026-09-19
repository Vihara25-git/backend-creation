package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReleaseTestCaseCountResponseDto {

    private Long releaseId;
    private Long testCaseCount;
}