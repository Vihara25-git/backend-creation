package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BenchAvailabilityViewResponseDTO {
    private Long empId;
    private String employeeName;
    private String designationName;
    private String roleType;
//
//    private String whatsappNumber;
//    private String email;

    private Long totalAllocatedPercentage;
    private Long availablePercentage;
    private LocalDateTime availablePeriod;

    private String currentProjects;

}