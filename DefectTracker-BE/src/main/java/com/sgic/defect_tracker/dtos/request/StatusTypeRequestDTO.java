package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StatusTypeRequestDTO {

    @NotBlank(message = "Status Name is required")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Status Name can contain only alphabets and spaces"
    )
    private String statusName;

    @NotBlank(message = "Status Type is required")
    private String statusType;

    @NotBlank(message = "Color Code is required")
    private String colorCode;

}
