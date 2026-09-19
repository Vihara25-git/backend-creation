package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectDetailsResponseDto {

    private Long projectId;
    private String projectName;
    private  String projectDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private  Long projectManagerId;
    private String projectManagerName;
    private  String designationName;
    private Long projectManagerDesignationId;
    private Integer managerAllocation;


    private ClientDetailsResponseDto clientDetails;

}
