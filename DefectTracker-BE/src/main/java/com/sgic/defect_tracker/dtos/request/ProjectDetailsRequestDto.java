package com.sgic.defect_tracker.dtos.request;


import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import java.time.LocalDate;

@Data
public class ProjectDetailsRequestDto {
    private  String projectName;
    private String projectDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long projectManagerId;
    private Long designationId;
    private Integer managerAllocation;

    @Valid
    private ClientDetailsRequestDto clientDetails;


    public Object getDescription() {
        return null;
    }
}
