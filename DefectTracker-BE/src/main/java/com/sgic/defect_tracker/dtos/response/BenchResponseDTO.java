package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
public class BenchResponseDTO {

    private Long empId;
    private String employeeName;
    private String designationName;

    private Long totalAllocatedPercentage;
    private Long availablePercentage;

    private LocalDateTime availablePeriod;

    private String currentProjects;

}
