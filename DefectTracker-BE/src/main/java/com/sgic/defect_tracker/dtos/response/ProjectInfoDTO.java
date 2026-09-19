package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfoDTO {

    private Long projectId;
    private String projectName;
    private String role;
}