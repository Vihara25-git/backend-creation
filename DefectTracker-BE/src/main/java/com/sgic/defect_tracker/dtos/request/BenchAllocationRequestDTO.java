package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

@Data
public class BenchAllocationRequestDTO {


    private Long empId;
    private Long roleId;
    private Long projectId;

   // private Long benchAllocationId;
    private LocalDate startDate;
    private LocalDate endDate;

    // LocalDateTime
    private Long availability;
}
