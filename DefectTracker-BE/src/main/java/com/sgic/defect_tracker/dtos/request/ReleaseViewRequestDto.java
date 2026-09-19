package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReleaseViewRequestDto {


    private String releaseName;

    private String releaseVersion;

    private LocalDate releaseDate;

    private Long projectId;

    private Long releaseTypeId;

}
