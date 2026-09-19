package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BenchAvailabilityViewFilterDTO {

    private String search;

    private String designationName;

    private Long availablePercentage;

    private LocalDate startDate;

    private LocalDate endDate;
}
