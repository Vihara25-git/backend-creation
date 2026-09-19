package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.util.List;

@Data
public class ReleaseTestCaseRequestDTO {

    private Long releaseId;

    private List<Long> testCaseIds;
}