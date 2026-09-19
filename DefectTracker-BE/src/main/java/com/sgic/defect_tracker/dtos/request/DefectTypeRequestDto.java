package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DefectTypeRequestDto {
    @Pattern
    (
            regexp="^[a-zA-Z ]+$",
            message="Only letters and spaces are allowed"
    )
    private String defectTypeName;

}
