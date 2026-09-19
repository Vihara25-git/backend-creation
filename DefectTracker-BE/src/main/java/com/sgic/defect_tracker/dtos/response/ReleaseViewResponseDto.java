package com.sgic.defect_tracker.dtos.response;


import lombok.Data;

import java.time.LocalDate;

@Data
public class ReleaseViewResponseDto {

    private Long releaseId;

    private String releaseName;

    private String releaseVersion;

    private LocalDate releaseDate;

    private Long releaseTypeId;

    private String status;

    private Long projectId;

}
